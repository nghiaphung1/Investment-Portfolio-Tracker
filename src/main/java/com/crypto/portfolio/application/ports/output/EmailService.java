package com.crypto.portfolio.application.ports.output;

import java.util.Map;

public interface EmailService {
    void sendHtmlEmail(String to, String subject, String templateName, Map<String, Object> variables);
}
