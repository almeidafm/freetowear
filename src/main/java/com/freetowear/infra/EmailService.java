package com.freetowear.infra;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendVerificationEmail(String email, String code) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");

            helper.setFrom("swiftgrove@msgwing.com", "FreeToWear");
            helper.setTo(email);
            helper.setSubject("Verificação de e-mail - FreeToWear");

            helper.setText("""
                    Olá!

                    Seu código de verificação da FreeToWear é:

                    %s

                    Este código expira em 10 minutos.

                    Se você não solicitou esta verificação, ignore este e-mail.
                    
                    Não responda a este e-mail. Esta é uma mensagem automática.
                    """.formatted(code));

            mailSender.send(message);

        } catch (MessagingException | UnsupportedEncodingException e) {
            throw new IllegalStateException(
                    "Não foi possível enviar o e-mail de verificação",
                    e
            );
        }
    }

    public void sendPasswordResetEmail(String email, String code) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");

            helper.setFrom("swiftgrove@msgwing.com", "FreeToWear");
            helper.setTo(email);
            helper.setSubject("Recuperação de senha - FreeToWear");

            helper.setText("""
                    Olá!

                    Recebemos uma solicitação para redefinir a senha da sua conta FreeToWear.

                    Seu código de recuperação é:

                    %s

                    Este código expira em 10 minutos.

                    Se você não solicitou esta alteração, ignore este e-mail e sua senha permanecerá inalterada.
                    
                    Não responda a este e-mail. Esta é uma mensagem automática.
                    """.formatted(code));

            mailSender.send(message);

        } catch (MessagingException | UnsupportedEncodingException e) {
            throw new IllegalStateException(
                    "Não foi possível enviar o e-mail de recuperação de senha",
                    e
            );
        }
    }
}