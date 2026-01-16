package com.crypto.portfolio.event;

import com.crypto.portfolio.application.ports.output.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotificationListener {

    private final EmailService emailService;

    @EventListener
    public void handleEmailNotification(OnNotificationEvent event) {
        // Chuyển dữ liệu từ Event sang EmailService
        emailService.sendHtmlEmail(
                event.getEmail(),
                event.getSubject(),
                event.getTemplateName(),
                event.getData()
        );
    }
}
