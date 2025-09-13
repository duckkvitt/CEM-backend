package com.g47.cem.cemdevice.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.g47.cem.cemdevice.entity.ServiceRequest;
import com.g47.cem.cemdevice.entity.ServiceRequestHistory;
import com.g47.cem.cemdevice.enums.ServiceRequestStatus;
import com.g47.cem.cemdevice.events.ServiceRequestRejectedEvent;
import com.g47.cem.cemdevice.repository.ServiceRequestHistoryRepository;
import com.g47.cem.cemdevice.repository.ServiceRequestRepository;

@Service
public class ServiceRequestHistoryService {

    private static final Logger log = LoggerFactory.getLogger(ServiceRequestHistoryService.class);

    private final ServiceRequestHistoryRepository serviceRequestHistoryRepository;
    private final ServiceRequestRepository serviceRequestRepository;

    public ServiceRequestHistoryService(ServiceRequestHistoryRepository serviceRequestHistoryRepository,
            ServiceRequestRepository serviceRequestRepository) {
        this.serviceRequestHistoryRepository = serviceRequestHistoryRepository;
        this.serviceRequestRepository = serviceRequestRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void tryWriteHistory(ServiceRequest serviceRequest, ServiceRequestStatus status, String updatedBy, String comment) {
        try {
            ServiceRequestHistory history = ServiceRequestHistory.builder()
                    .serviceRequest(serviceRequest)
                    .status(status)
                    .comment(comment)
                    .updatedBy(updatedBy)
                    .build();
            serviceRequestHistoryRepository.saveAndFlush(history);
        } catch (DataIntegrityViolationException ex) {
            // Fallback with current status if specific status violates a DB CHECK constraint
            try {
                ServiceRequestHistory fallback = ServiceRequestHistory.builder()
                        .serviceRequest(serviceRequest)
                        .status(serviceRequest.getStatus())
                        .comment(comment)
                        .updatedBy(updatedBy)
                        .build();
                serviceRequestHistoryRepository.saveAndFlush(fallback);
                log.warn("Saved service request history with fallback status due to constraint: {}", ex.getMessage());
            } catch (Exception inner) {
                // Swallow to avoid impacting caller; log for operators
                log.warn("Skipping history entry due to persistent constraint violations: {}", inner.getMessage());
            }
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onServiceRequestRejected(ServiceRequestRejectedEvent event) {
        ServiceRequest serviceRequest = serviceRequestRepository.findById(event.getServiceRequestId()).orElse(null);
        if (serviceRequest == null) {
            log.warn("ServiceRequest not found for history write: {}", event.getServiceRequestId());
            return;
        }
        tryWriteHistory(serviceRequest, event.getStatus(), event.getUpdatedBy(), event.getComment());
    }
}


