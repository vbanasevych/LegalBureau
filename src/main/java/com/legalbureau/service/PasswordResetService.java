package com.legalbureau.service;

import com.legalbureau.entity.PasswordResetToken;
import com.legalbureau.entity.User;
import com.legalbureau.repository.PasswordResetTokenRepository;
import com.legalbureau.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private final PasswordResetTokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final JavaMailSender mailSender;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void createTokenAndSendEmail(String email, String appUrl) {
        userRepository.findByEmail(email).ifPresent(user -> {
            tokenRepository.deleteByUser_Id(user.getId());

            String token = UUID.randomUUID().toString();
            PasswordResetToken resetToken = new PasswordResetToken();
            resetToken.setToken(token);
            resetToken.setUser(user);
            resetToken.setExpiryDate(LocalDateTime.now().plusMinutes(10));
            tokenRepository.save(resetToken);

            String resetUrl = appUrl + "/reset-password?token=" + token;

            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(user.getEmail());
            message.setSubject("Відновлення пароля - Юридичне Бюро");
            message.setText("Ви подали запит на скидання пароля.\n" +
                    "Для встановлення нового пароля перейдіть за посиланням (діє 15 хвилин):\n\n" + resetUrl);
            mailSender.send(message);
        });
    }

    public boolean validateToken(String token) {
        return tokenRepository.findByToken(token)
                .map(t -> t.getExpiryDate().isAfter(LocalDateTime.now()))
                .orElse(false);
    }

    @Transactional
    public void updatePassword(String token, String newPassword) {
        PasswordResetToken resetToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Недійсний або прострочений токен"));

        User user = resetToken.getUser();
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        tokenRepository.delete(resetToken);
    }
}