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
    
    @Async("taskExecutor")
    public void sendAccountCreationEmail(String toEmail, String firstName, String lastName, String temporaryPassword) {
        try {
            log.info("Sending account creation email to: {}", toEmail);
            
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Welcome to CEM - Your Account Has Been Created");
            
            String emailBody = buildAccountCreationEmailBody(firstName, lastName, toEmail, temporaryPassword);
            message.setText(emailBody);
            
            mailSender.send(message);
            
            log.info("Account creation email sent successfully to: {}", toEmail);
            
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
    
    @Async("taskExecutor")
    public void sendPasswordResetEmail(String toEmail, String firstName, String lastName, String resetToken) {
        try {
            log.info("Sending password reset email to: {}", toEmail);
            
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("CEM - Password Reset Request");
            
            String emailBody = buildPasswordResetEmailBody(firstName, lastName, resetToken);
            message.setText(emailBody);
            
            mailSender.send(message);
            
            log.info("Password reset email sent successfully to: {}", toEmail);
            
        } catch (Exception e) {
            log.error("Failed to send password reset email to: {}. Error: {}", toEmail, e.getMessage(), e);
        }
    }
    
    private String buildPasswordResetEmailBody(String firstName, String lastName, String resetToken) {
        return String.format("""
            Dear %s %s,
            
            We received a request to reset your password for your CEM (Construction Equipment Management) System account.
            
            Your password reset token is: %s
            
            IMPORTANT SECURITY NOTICE:
            - This token is valid for 15 minutes from the time of this email
            - Use this token to reset your password through the CEM system
            - If you did not request a password reset, please ignore this email
            - Do not share this token with anyone
            
            To reset your password:
            1. Go to the CEM password reset page
            2. Enter your email address
            3. Enter the reset token provided above
            4. Create a new secure password
            
            If you have any questions or need assistance, please contact your system administrator.
            
            Best regards,
            CEM System Team
            
            ---
            This is an automated message. Please do not reply to this email.
            """, firstName, lastName, resetToken);
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