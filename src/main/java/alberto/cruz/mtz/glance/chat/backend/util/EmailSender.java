package alberto.cruz.mtz.glance.chat.backend.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmailSender {

    private final JavaMailSender javaMailSender;
    private final String mailSender;

    @Async
    public void sendEmailWithOtpCode(String recipientEmail, String otpCode) {
        SimpleMailMessage message = new SimpleMailMessage();

        message.setFrom(this.mailSender);
        message.setTo(recipientEmail);
        message.setSubject("Your OTP Code for Glance Chat Authentication");
        message.setText("Your OTP code is: " + otpCode + "\nThis code will expire in 5 minutes.");

        javaMailSender.send(message);
        log.info("OTP email sent to {} with code {}", recipientEmail, otpCode);
    }
}
