package com.g47.cem.cemdevice.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.g47.cem.cemdevice.dto.request.CreateCustomerFeedbackRequest;
import com.g47.cem.cemdevice.dto.response.ApiResponse;
import com.g47.cem.cemdevice.dto.response.CustomerFeedbackResponse;
import com.g47.cem.cemdevice.entity.CustomerFeedback;
import com.g47.cem.cemdevice.service.CustomerFeedbackService;
import com.g47.cem.cemdevice.service.ExternalCustomerService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/customer-feedbacks")
@RequiredArgsConstructor
public class CustomerFeedbackController {

    private final CustomerFeedbackService customerFeedbackService;
    private final ExternalCustomerService externalCustomerService;

    @PostMapping
    @PreAuthorize("hasAuthority('CUSTOMER')")
    public ResponseEntity<ApiResponse<CustomerFeedbackResponse>> submitFeedback(
            @Validated @RequestBody CreateCustomerFeedbackRequest request,
            Authentication authentication) {
        Long customerId = extractCustomerId(authentication);
        CustomerFeedbackResponse response = customerFeedbackService.submitFeedback(customerId, authentication.getName(), request);
        return ResponseEntity.ok(ApiResponse.success(response, "Feedback submitted successfully"));
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('SUPPORT_TEAM','MANAGER')")
    public ResponseEntity<ApiResponse<Page<CustomerFeedbackResponse>>> searchFeedbacks(
            @RequestParam(required = false) Integer starRating,
            @RequestParam(required = false) String serviceType,
            @RequestParam(required = false) Long customerId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String fromDate,
            @RequestParam(required = false) String toDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "submittedAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("desc".equalsIgnoreCase(sortDir) ? Sort.Direction.DESC : Sort.Direction.ASC, sortBy));

        // Simple filtering via Specification if needed later; for now use repository directly
        org.springframework.data.jpa.domain.Specification<CustomerFeedback> spec = (root, query, cb) -> {
            java.util.List<jakarta.persistence.criteria.Predicate> predicates = new java.util.ArrayList<>();
            if (starRating != null) {
                predicates.add(cb.equal(root.get("starRating"), starRating));
            }
            if (serviceType != null && !serviceType.isBlank()) {
                predicates.add(cb.equal(root.get("serviceType"), serviceType));
            }
            if (fromDate != null && !fromDate.isBlank()) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("submittedAt"), java.time.LocalDate.parse(fromDate).atStartOfDay()));
            }
            if (toDate != null && !toDate.isBlank()) {
                predicates.add(cb.lessThan(root.get("submittedAt"), java.time.LocalDate.parse(toDate).plusDays(1).atStartOfDay()));
            }
            if (customerId != null) {
                predicates.add(cb.equal(root.get("customerId"), customerId));
            }
            if (keyword != null && !keyword.isBlank()) {
                String like = "%" + keyword.toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("comment")), like)
                ));
            }
            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };

        Page<CustomerFeedbackResponse> pageResult = customerFeedbackService.searchFeedbacks(spec, pageable);
        return ResponseEntity.ok(ApiResponse.success(pageResult, "Feedback list retrieved successfully"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('SUPPORT_TEAM','MANAGER')")
    public ResponseEntity<ApiResponse<CustomerFeedbackResponse>> getDetail(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(customerFeedbackService.getById(id), "Feedback detail retrieved successfully"));
    }

    @GetMapping("/me")
    @PreAuthorize("hasAuthority('CUSTOMER')")
    public ResponseEntity<ApiResponse<Page<CustomerFeedbackResponse>>> myFeedbacks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "submittedAt"));
        Long customerId = extractCustomerId(authentication);
        Page<CustomerFeedbackResponse> result = customerFeedbackService.getFeedbacksForCustomer(customerId, pageable);
        return ResponseEntity.ok(ApiResponse.success(result, "My feedbacks retrieved successfully"));
    }

    @GetMapping("/me/by-service-request/{serviceRequestId}")
    @PreAuthorize("hasAuthority('CUSTOMER')")
    public ResponseEntity<ApiResponse<CustomerFeedbackResponse>> myFeedbackByServiceRequest(
            @PathVariable Long serviceRequestId,
            Authentication authentication) {
        Long customerId = extractCustomerId(authentication);
        CustomerFeedbackResponse res = customerFeedbackService.getMyFeedbackForServiceRequest(serviceRequestId, customerId);
        return ResponseEntity.ok(ApiResponse.success(res, "Feedback retrieved"));
    }

    private Long extractCustomerId(Authentication authentication) {
        String userEmail = authentication.getName();
        var customerInfo = externalCustomerService.getCustomerByEmail(userEmail);
        if (customerInfo == null || customerInfo.getId() == null) {
            throw new IllegalStateException("Customer not found for current user");
        }
        return customerInfo.getId();
    }
}


