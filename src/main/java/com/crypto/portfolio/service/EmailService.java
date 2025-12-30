package com.crypto.portfolio.service;

import com.crypto.portfolio.entity.User;

import java.util.Map;

public interface EmailService {
    void sendHtmlEmail(String to, String subject, String templateName, Map<String, Object> variables);
}
