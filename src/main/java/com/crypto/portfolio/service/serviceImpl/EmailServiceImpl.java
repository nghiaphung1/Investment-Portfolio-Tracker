package com.crypto.portfolio.service.serviceImpl;

import com.crypto.portfolio.exception.AppException;
import com.crypto.portfolio.exception.ErrorCode;
import com.crypto.portfolio.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {
    private final JavaMailSender javaMailSender;
    private final TemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.mail.sender-name:Crypto Portfolio}") // Config tên người gửi, default nếu thiếu
    private String senderName;

    @Async("mailTaskExecutor")
    @Retryable(
            retryFor = { MailException.class, MessagingException.class }, // Chỉ retry lỗi mạng/kết nối mail
            maxAttempts = 3,
            backoff = @Backoff(delay = 2000, multiplier = 2.0)
    )
    @Override
    public void sendHtmlEmail(String to, String subject, String templateName, Map<String, Object> variables) {
        log.info("--- [Email] Start sending to: {} | Subject: {}", to, subject);

        try {
            Context context = new Context();
            context.setVariables(variables);
            // Quy ước template luôn nằm trong folder 'email/'
            String htmlContent = templateEngine.process("email/" + templateName, context);

            MimeMessage message = javaMailSender.createMimeMessage();
            // Multipart = true để support ảnh hoặc đính kèm nếu cần sau này
            MimeMessageHelper helper = new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());

            helper.setFrom(fromEmail, senderName); // Hiển thị tên đẹp thay vì chỉ hiện email
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            javaMailSender.send(message);
            log.info("--- [Email] Sent successfully to: {}", to);

        } catch (MessagingException | UnsupportedEncodingException | MailException e) {
            log.warn("--- [Email] Failed attempt. Error: {}", e.getMessage());
            throw new AppException(ErrorCode.EMAIL_SENDING_FAILED, e);
        } catch (Exception e) {
            // Các lỗi khác (ví dụ sai tên template, null pointer) thường không retry được -> log error luôn
            log.error("--- [Email] Fatal error (No Retry): {}", e.getMessage(), e);
        }
    }

    @Recover
    public void recover(AppException e, String to, String subject, String templateName, Map<String, Object> variables) {
        // Đây là nơi xử lý khi retry 3 lần vẫn thất bại (Dead Letter)
        log.error("### [Email] GIVE UP sending to: {}. Error: {}", to, e.getMessage());

        // TODO 1: Update status trong Database (ví dụ table EmailLog -> status = FAILED)
        // emailLogRepository.save(new EmailLog(to, subject, "FAILED"));

        // TODO 2: Bắn alert notification cho Admin nếu cần thiết
    }
}