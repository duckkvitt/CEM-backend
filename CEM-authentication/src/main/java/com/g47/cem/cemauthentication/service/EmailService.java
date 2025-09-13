package com.g47.cem.cemauthentication.service;

import java.security.SecureRandom;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    
    @Value("${spring.mail.username}")
    private String fromEmail;
    
    @Value("${app.reset-password.url:http://localhost:3000/reset-password}")
    private String resetPasswordBaseUrl;
    
    @Async("taskExecutor")
    public void sendAccountCreationEmail(String toEmail, String firstName, String lastName, String temporaryPassword, String roleName) {
        try {
            log.info("Sending account creation email to: {} with role: {}", toEmail, roleName);
            
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            
            String emailBody;
            if ("CUSTOMER".equals(roleName)) {
                message.setSubject("Hợp đồng đã được ký - Contract Signed");
                emailBody = buildCustomerAccountCreationEmailBody(firstName, lastName, toEmail, temporaryPassword);
            } else {
                message.setSubject("Welcome to CEM - Your Account Has Been Created");
                emailBody = buildAccountCreationEmailBody(firstName, lastName, toEmail, temporaryPassword);
            }
            
            message.setText(emailBody);
            mailSender.send(message);
            
            log.info("Account creation email sent successfully to: {} with role: {}", toEmail, roleName);
            
        } catch (Exception e) {
            log.error("Failed to send account creation email to: {}. Error: {}", toEmail, e.getMessage(), e);
        }
    }
    
    private String buildAccountCreationEmailBody(String firstName, String lastName, String email, String temporaryPassword) {
        return String.format("""
            Dear %s %s,
            
            Welcome to CEM (Construction Equipment Management) System!
            
            Your account has been successfully created by an administrator. Please find your login credentials below:
            
            Email: %s
            Temporary Password: %s
            
            IMPORTANT SECURITY NOTICE:
            - This is a temporary password generated for your account
            - Please change your password after your first login for security purposes
            - Do not share these credentials with anyone
            
            You can now access the CEM system using these credentials.
            
            If you have any questions or need assistance, please contact your system administrator.
            
            Best regards,
            CEM System Team
            
            ---
            This is an automated message. Please do not reply to this email.
            """, firstName, lastName, email, temporaryPassword);
    }
    
    private String buildCustomerAccountCreationEmailBody(String firstName, String lastName, String email, String temporaryPassword) {
        return String.format("""
            Xin chào %s %s,
            
            Hợp đồng đã được ký thành công.
            
            Để truy cập hệ thống quản lý hợp đồng, vui lòng sử dụng thông tin đăng nhập sau:
            - Email: %s
            - Mật khẩu tạm thời: %s
            
            Vui lòng đổi mật khẩu sau lần đăng nhập đầu tiên.
            
            Truy cập hệ thống tại: https://cem.vercel.app
            
            Cảm ơn bạn đã tin tưởng dịch vụ của chúng tôi.
            
            ---
            
            Dear %s %s,
            
            Contract has been successfully signed.
            
            To access the contract management system, please use the following credentials:
            - Email: %s
            - Temporary Password: %s
            
            Please change your password after your first login.
            
            Access the system at: https://cem.vercel.app
            
            Thank you for trusting our services.
            
            Trân trọng,
            Công ty TNHH Thương mại và Sản xuất Thành Đạt
            """, firstName, lastName, email, temporaryPassword, firstName, lastName, email, temporaryPassword);
    }
    
    @Async("taskExecutor")
    public void sendPasswordResetEmail(String toEmail, String firstName, String lastName, String resetToken) {
        try {
            log.info("Sending password reset email to: {}", toEmail);
            
            String resetLink = String.format("%s?token=%s&email=%s", resetPasswordBaseUrl, resetToken, toEmail);
            
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("CEM - Password Reset Request");
            
            String emailBody = buildPasswordResetEmailBody(firstName, lastName, resetLink);
            message.setText(emailBody);
            
            mailSender.send(message);
            
            log.info("Password reset email sent successfully to: {}", toEmail);
            
        } catch (Exception e) {
            log.error("Failed to send password reset email to: {}. Error: {}", toEmail, e.getMessage(), e);
        }
    }
    
    private String buildPasswordResetEmailBody(String firstName, String lastName, String resetLink) {
        return String.format("""
            Dear %s %s,
            
            We received a request to reset your password for your CEM (Construction Equipment Management) System account.
            
            Please click the link below (or copy & paste it into your browser) to set a new password. The link is valid for 15 minutes.
            
            %s
            
            IMPORTANT SECURITY NOTICE:
            - If you did not request a password reset, please ignore this email.
            - Do not share this link with anyone.
            
            If you have any questions or need assistance, please contact your system administrator.
            
            Best regards,
            CEM System Team
            
            ---
            This is an automated message. Please do not reply to this email.
            """, firstName, lastName, resetLink);
    }

    /**
     * Generate a secure reset token
     */
    public String generateResetToken() {
        String upperCase = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String lowerCase = "abcdefghijklmnopqrstuvwxyz";
        String numbers = "0123456789";
        
        String allChars = upperCase + lowerCase + numbers;
        SecureRandom random = new SecureRandom();
        StringBuilder token = new StringBuilder();
        
        // Generate 8 character token
        for (int i = 0; i < 8; i++) {
            token.append(allChars.charAt(random.nextInt(allChars.length())));
        }
        
        return token.toString();
    }

    /**
     * Generate a secure temporary password
     */
    public String generateTemporaryPassword() {
        String upperCase = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String lowerCase = "abcdefghijklmnopqrstuvwxyz";
        String numbers = "0123456789";
        String specialChars = "!@#$%&*";
        
        String allChars = upperCase + lowerCase + numbers + specialChars;
        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder();
        
        // Ensure at least one character from each category
        password.append(upperCase.charAt(random.nextInt(upperCase.length())));
        password.append(lowerCase.charAt(random.nextInt(lowerCase.length())));
        password.append(numbers.charAt(random.nextInt(numbers.length())));
        password.append(specialChars.charAt(random.nextInt(specialChars.length())));
        
        // Fill the rest randomly (minimum 8 characters total)
        for (int i = 4; i < 12; i++) {
            password.append(allChars.charAt(random.nextInt(allChars.length())));
        }
        
        // Shuffle the password
        return shuffleString(password.toString());
    }
    
    private String shuffleString(String string) {
        char[] chars = string.toCharArray();
        SecureRandom random = new SecureRandom();
        
        for (int i = chars.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            char temp = chars[i];
            chars[i] = chars[j];
            chars[j] = temp;
        }
        
        return new String(chars);
    }
} 