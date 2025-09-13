package com.g47.cem.cemauthentication.event;

public record UserCreatedEvent(String email, String firstName, String lastName, String temporaryPassword, String roleName) { } 