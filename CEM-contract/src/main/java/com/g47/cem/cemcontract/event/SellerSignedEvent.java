package com.g47.cem.cemcontract.event;

import com.g47.cem.cemcontract.entity.Contract;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class SellerSignedEvent extends ApplicationEvent {
    private final Contract contract;

    public SellerSignedEvent(Object source, Contract contract) {
        super(source);
        this.contract = contract;
    }
} 