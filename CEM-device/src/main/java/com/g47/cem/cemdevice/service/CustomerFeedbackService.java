package com.g47.cem.cemdevice.service;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.g47.cem.cemdevice.dto.request.CreateCustomerFeedbackRequest;
import com.g47.cem.cemdevice.dto.response.CustomerFeedbackResponse;
import com.g47.cem.cemdevice.entity.CustomerDevice;
import com.g47.cem.cemdevice.entity.CustomerFeedback;
import com.g47.cem.cemdevice.entity.ServiceRequest;
import com.g47.cem.cemdevice.entity.Task;
import com.g47.cem.cemdevice.enums.ServiceRequestStatus;
import com.g47.cem.cemdevice.exception.BusinessException;
import com.g47.cem.cemdevice.exception.ResourceNotFoundException;
import com.g47.cem.cemdevice.integration.UserIntegrationService;
import com.g47.cem.cemdevice.repository.CustomerFeedbackRepository;
import com.g47.cem.cemdevice.repository.ServiceRequestRepository;
import com.g47.cem.cemdevice.repository.TaskRepository;
import com.g47.cem.cemdevice.repository.TechnicianProfileRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CustomerFeedbackService {

    private final CustomerFeedbackRepository customerFeedbackRepository;
    private final ServiceRequestRepository serviceRequestRepository;
    private final TaskRepository taskRepository;
    @SuppressWarnings("unused")
    private final TechnicianProfileRepository technicianProfileRepository; // reserved for potential future use
    private final ExternalCustomerService externalCustomerService;
    private final UserIntegrationService userIntegrationService;

    @Transactional
    public CustomerFeedbackResponse submitFeedback(Long customerId, String username, CreateCustomerFeedbackRequest request) {
        ServiceRequest serviceRequest = serviceRequestRepository.findById(request.getServiceRequestId())
                .orElseThrow(() -> new ResourceNotFoundException("ServiceRequest", "id", request.getServiceRequestId()));

        if (!serviceRequest.getCustomerId().equals(customerId)) {
            throw new BusinessException("Service Request does not belong to the current customer");
        }
        if (serviceRequest.getStatus() != ServiceRequestStatus.COMPLETED) {
            throw new BusinessException("Feedback can only be submitted for completed service requests");
        }
        if (customerFeedbackRepository.existsByServiceRequestIdAndCustomerId(serviceRequest.getId(), customerId)) {
            throw new BusinessException("Feedback already submitted for this service request");
        }

        Long technicianId = null;
        if (serviceRequest.getId() != null) {
            Task task = taskRepository.findFirstByServiceRequestIdOrderByCreatedAtDesc(serviceRequest.getId()).orElse(null);
            if (task != null) {
                technicianId = task.getAssignedTechnicianId();
            }
        }

        CustomerDevice device = serviceRequest.getDevice();

        CustomerFeedback feedback = CustomerFeedback.builder()
                .serviceRequestId(serviceRequest.getId())
                .customerId(customerId)
                .deviceId(device.getId())
                .serviceType(serviceRequest.getType().name())
                .starRating(request.getStarRating())
                .comment(request.getComment())
                .technicianId(technicianId)
                .submittedAt(LocalDateTime.now())
                .createdBy(username)
                .build();

        feedback = customerFeedbackRepository.save(feedback);
        return mapToResponse(feedback, serviceRequest);
    }

    public Page<CustomerFeedbackResponse> getFeedbacksForCustomer(Long customerId, Pageable pageable) {
        return customerFeedbackRepository.findByCustomerId(customerId, pageable)
                .map(fb -> {
                    ServiceRequest sr = serviceRequestRepository.findById(fb.getServiceRequestId())
                            .orElse(null);
                    return mapToResponse(fb, sr);
                });
    }

    public Page<CustomerFeedbackResponse> searchFeedbacks(org.springframework.data.jpa.domain.Specification<CustomerFeedback> spec, Pageable pageable) {
        return customerFeedbackRepository.findAll(spec, pageable)
                .map(fb -> {
                    ServiceRequest sr = serviceRequestRepository.findByIdWithDevice(fb.getServiceRequestId())
                            .orElse(null);
                    return mapToResponse(fb, sr);
                });
    }

    public CustomerFeedbackResponse getById(Long id) {
        CustomerFeedback fb = customerFeedbackRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CustomerFeedback", "id", id));
        ServiceRequest sr = serviceRequestRepository.findByIdWithDevice(fb.getServiceRequestId())
                .orElse(null);
        return mapToResponse(fb, sr);
    }

    public CustomerFeedbackResponse getMyFeedbackForServiceRequest(Long serviceRequestId, Long customerId) {
        var fb = customerFeedbackRepository.findByServiceRequestIdAndCustomerId(serviceRequestId, customerId)
                .orElse(null);
        if (fb == null) return null;
        ServiceRequest sr = serviceRequestRepository.findByIdWithDevice(serviceRequestId).orElse(null);
        return mapToResponse(fb, sr);
    }

    private CustomerFeedbackResponse mapToResponse(CustomerFeedback fb, ServiceRequest sr) {
        String deviceName = null;
        String deviceType = null;
        String technicianName = null;
        String customerName = null;
        String serviceRequestCode = null;
        if (sr != null) {
            serviceRequestCode = sr.getRequestId();
            if (sr.getDevice() != null && sr.getDevice().getDevice() != null) {
                deviceName = sr.getDevice().getDevice().getName();
                deviceType = sr.getDevice().getDevice().getModel();
            }
        }
        // Resolve customer name from Customer service
        var customerInfo = externalCustomerService.getCustomerById(fb.getCustomerId());
        if (customerInfo != null) {
            customerName = customerInfo.getCompanyName() != null && !customerInfo.getCompanyName().isBlank()
                    ? customerInfo.getCompanyName()
                    : customerInfo.getContactName();
        }
        // Resolve technician full name from Auth service using current bearer token
        if (fb.getTechnicianId() != null) {
            String bearer = extractBearerToken();
            technicianName = userIntegrationService.getUserById(fb.getTechnicianId(), bearer)
                    .map(u -> {
                        String full = u.getFullName();
                        if (full != null && !full.trim().isEmpty()) return full.trim();
                        return (u.getEmail() != null && !u.getEmail().isBlank()) ? u.getEmail() : null;
                    })
                    .orElse(null);
        }
        return CustomerFeedbackResponse.builder()
                .id(fb.getId())
                .serviceRequestId(fb.getServiceRequestId())
                .serviceRequestCode(serviceRequestCode)
                .customerId(fb.getCustomerId())
                .customerName(customerName)
                .deviceId(fb.getDeviceId())
                .deviceName(deviceName)
                .deviceType(deviceType)
                .serviceType(fb.getServiceType())
                .starRating(fb.getStarRating())
                .comment(fb.getComment())
                .technicianId(fb.getTechnicianId())
                .technicianName(technicianName)
                .submittedAt(fb.getSubmittedAt())
                .build();
    }

    private String extractBearerToken() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs != null && attrs.getRequest() != null) {
            String header = attrs.getRequest().getHeader("Authorization");
            if (header != null && !header.isBlank()) {
                return header.startsWith("Bearer ") ? header.substring(7) : header;
            }
        }
        return null;
    }
}


