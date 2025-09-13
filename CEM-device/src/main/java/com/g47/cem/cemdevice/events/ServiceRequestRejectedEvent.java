package com.g47.cem.cemdevice.events;

import com.g47.cem.cemdevice.enums.ServiceRequestStatus;

public class ServiceRequestRejectedEvent {
    private final Long serviceRequestId;
    private final ServiceRequestStatus status;
    private final String updatedBy;
    private final String comment;

    public ServiceRequestRejectedEvent(Long serviceRequestId, ServiceRequestStatus status, String updatedBy, String comment) {
        this.serviceRequestId = serviceRequestId;
        this.status = status;
        this.updatedBy = updatedBy;
        this.comment = comment;
    }

    public Long getServiceRequestId() { return serviceRequestId; }
    public ServiceRequestStatus getStatus() { return status; }
    public String getUpdatedBy() { return updatedBy; }
    public String getComment() { return comment; }
}



