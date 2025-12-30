package com.crypto.portfolio.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
public class OnNotificationEvent {
    private String email;
    private String fullName;
    private String subject;
    private String templateName;
    private Map<String, Object> data;
}
