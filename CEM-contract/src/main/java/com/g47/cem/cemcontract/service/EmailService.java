package com.g47.cem.cemcontract.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service for sending email notifications related to contracts
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {
    
    private final JavaMailSender mailSender;
    
    /**
     * Send contract signed notification to customer with account credentials
     */
    public void sendContractSignedNotification(
            String customerEmail, 
            String customerName,
            String contractNumber,
            String tempPassword) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(customerEmail);
            message.setSubject("Hợp đồng đã được ký - Contract Signed");
            
            String content = buildContractSignedEmailContent(
                customerName, 
                contractNumber, 
                customerEmail, 
                tempPassword
            );
            
            message.setText(content);
            mailSender.send(message);
            
            log.info("Contract signed notification sent to: {}", customerEmail);
            
        } catch (Exception e) {
            log.error("Failed to send contract signed notification to: {}", customerEmail, e);
            throw new RuntimeException("Failed to send email notification", e);
        }
    }
    
    /**
     * Send contract created notification to staff
     */
    public void sendContractCreatedNotification(
            String staffEmail, 
            String staffName,
            String contractNumber,
            String customerName) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(staffEmail);
            message.setSubject("Hợp đồng mới được tạo - New Contract Created");
            
            String content = buildContractCreatedEmailContent(
                staffName, 
                contractNumber, 
                customerName
            );
            
            message.setText(content);
            mailSender.send(message);
            
            log.info("Contract created notification sent to staff: {}", staffEmail);
            
        } catch (Exception e) {
            log.error("Failed to send contract created notification to: {}", staffEmail, e);
            // Don't throw exception for staff notifications
        }
    }
    
    /**
     * Send contract expiry reminder
     */
    public void sendContractExpiryReminder(
            String customerEmail,
            String customerName,
            String contractNumber,
            int daysUntilExpiry) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(customerEmail);
            message.setSubject("Nhắc nhở hết hạn hợp đồng - Contract Expiry Reminder");
            
            String content = buildContractExpiryReminderContent(
                customerName, 
                contractNumber, 
                daysUntilExpiry
            );
            
            message.setText(content);
            mailSender.send(message);
            
            log.info("Contract expiry reminder sent to: {}", customerEmail);
            
        } catch (Exception e) {
            log.error("Failed to send contract expiry reminder to: {}", customerEmail, e);
        }
    }
    
    private String buildContractSignedEmailContent(
            String customerName, 
            String contractNumber, 
            String email, 
            String tempPassword) {
        
        return String.format("""
            Xin chào %s,
            
            Hợp đồng số %s đã được ký thành công.
            
            Để truy cập hệ thống quản lý hợp đồng, vui lòng sử dụng thông tin đăng nhập sau:
            - Email: %s
            - Mật khẩu tạm thời: %s
            
            Vui lòng đổi mật khẩu sau lần đăng nhập đầu tiên.
            
            Truy cập hệ thống tại: https://cem.vercel.app
            
            Cảm ơn bạn đã tin tưởng dịch vụ của chúng tôi.
            
            ---
            
            Dear %s,
            
            Contract %s has been successfully signed.
            
            To access the contract management system, please use the following credentials:
            - Email: %s
            - Temporary Password: %s
            
            Please change your password after your first login.
            
            Access the system at: https://cem.vercel.app
            
            Thank you for trusting our services.
            
            Trân trọng,
            Công ty TNHH Thương mại và Sản xuất Thành Đạt
            """, 
            customerName, contractNumber, email, tempPassword,
            customerName, contractNumber, email, tempPassword);
    }
    
    private String buildContractCreatedEmailContent(
            String staffName, 
            String contractNumber, 
            String customerName) {
        
        return String.format("""
            Xin chào %s,
            
            Hợp đồng mới đã được tạo:
            - Số hợp đồng: %s
            - Khách hàng: %s
            
            Vui lòng kiểm tra và xử lý hợp đồng trong hệ thống.
            
            Truy cập hệ thống tại: https://cem.vercel.app
            
            ---
            
            Dear %s,
            
            A new contract has been created:
            - Contract Number: %s
            - Customer: %s
            
            Please review and process the contract in the system.
            
            Access the system at: https://cem.vercel.app
            """, 
            staffName, contractNumber, customerName,
            staffName, contractNumber, customerName);
    }
    
    private String buildContractExpiryReminderContent(
            String customerName, 
            String contractNumber, 
            int daysUntilExpiry) {
        
        return String.format("""
            Xin chào %s,
            
            Hợp đồng số %s sẽ hết hạn trong %d ngày.
            
            Vui lòng liên hệ với chúng tôi để gia hạn hoặc thảo luận về hợp đồng mới.
            
            Hotline: 0123-456-789
            Email: support@thanhdat.com
            
            ---
            
            Dear %s,
            
            Contract %s will expire in %d days.
            
            Please contact us to renew or discuss a new contract.
            
            Hotline: 0123-456-789
            Email: support@thanhdat.com
            
            Trân trọng,
            Công ty TNHH Thương mại và Sản xuất Thành Đạt
            """, 
            customerName, contractNumber, daysUntilExpiry,
            customerName, contractNumber, daysUntilExpiry);
    }
} 