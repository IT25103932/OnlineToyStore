package com.OnlineToyStore.Sllit.service;

import com.OnlineToyStore.Sllit.model.Order;
import com.OnlineToyStore.Sllit.model.Review;
import com.OnlineToyStore.Sllit.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;

@Service
public class EmailService {

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${toymart.mail.enabled:true}")
    private boolean mailEnabled;

    @Value("${spring.mail.username:}")
    private String username;

    @Value("${spring.mail.password:}")
    private String password;

    @Value("${toymart.mail.from:toymart.web@gmail.com}")
    private String fromAddress;

    @Value("${toymart.admin.email:toymart.web@gmail.com}")
    private String adminEmail;

    @Value("${brevo.api.key:}")
    private String brevoApiKey;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    public void sendWelcomeEmail(User user, String verificationCode) {
        String body = "Welcome to ToyMart, " + user.getUsername() + "!\n\n"
                + "Your account has been successfully created.\n"
                + (verificationCode != null && !verificationCode.isBlank()
                ? "Email verification code: " + verificationCode + "\n\n"
                : "")
                + "Thank you for joining ToyMart.";
        sendEmail(user.getEmail(), "Welcome to ToyMart - Verify Your Email", body);
    }

    public void sendPasswordResetOtp(User user, String otp) {
        String body = "Hello " + user.getUsername() + ",\n\n"
                + "Your ToyMart password reset OTP is: " + otp + "\n"
                + "If you did not request this, please ignore this email.\n\n"
                + "ToyMart Support";
        sendEmail(user.getEmail(), "ToyMart Password Reset OTP", body);
    }

    public void sendOrderConfirmation(User user, Order order) {
        String body = "Hello " + user.getUsername() + ",\n\n"
                + "Your ToyMart order has been confirmed.\n\n"
                + "Order ID: " + order.getOrderId() + "\n"
                + "Toy: " + order.getToyName() + "\n"
                + "Quantity: " + order.getQuantity() + "\n"
                + "Total: Rs. " + String.format("%.2f", order.getTotalAmount()) + "\n"
                + "Payment: " + order.getPaymentMethod() + " (" + order.getPaymentStatus() + ")\n\n"
                + "You can view and save your bill from your ToyMart order history.";
        sendEmail(user.getEmail(), "ToyMart Order Confirmed - " + order.getOrderId(), body);
    }

    public void sendBillReceipt(User user, Order order) {
        String unitPrice = order.getQuantity() > 0
                ? String.format("%.2f", order.getTotalAmount() / order.getQuantity())
                : "0.00";
        String body = "ToyMart Invoice / Receipt\n\n"
                + "Customer: " + user.getUsername() + "\n"
                + "Order ID: " + order.getOrderId() + "\n"
                + "Date: " + order.getOrderDate() + "\n"
                + "Toy: " + order.getToyName() + "\n"
                + "Quantity: " + order.getQuantity() + "\n"
                + "Unit Price: Rs. " + unitPrice + "\n"
                + "Grand Total: Rs. " + String.format("%.2f", order.getTotalAmount()) + "\n"
                + "Payment: " + order.getPaymentMethod() + " (" + order.getPaymentStatus() + ")\n"
                + "Status: " + order.getStatus() + "\n\n"
                + "You can also open your ToyMart bill page and use Download / Save PDF.";
        sendEmail(user.getEmail(), "ToyMart Receipt - " + order.getOrderId(), body);
    }

    public void sendOrderStatusUpdate(User user, Order order) {
        String body = "Hello " + user.getUsername() + ",\n\n"
                + "Your ToyMart order status has been updated.\n\n"
                + "Order ID: " + order.getOrderId() + "\n"
                + "Toy: " + order.getToyName() + "\n"
                + "Status: " + order.getStatus() + "\n"
                + "Payment Status: " + order.getPaymentStatus() + "\n\n"
                + "Thank you for shopping with ToyMart.";
        sendEmail(user.getEmail(), "ToyMart Order Update - " + order.getStatus(), body);
    }

    public void notifyAdminNewOrder(Order order) {
        String body = "A new order was placed in ToyMart.\n\n"
                + "Order ID: " + order.getOrderId() + "\n"
                + "Customer: " + order.getUsername() + "\n"
                + "Toy: " + order.getToyName() + "\n"
                + "Quantity: " + order.getQuantity() + "\n"
                + "Total: Rs. " + String.format("%.2f", order.getTotalAmount());
        sendEmail(adminEmail, "ToyMart Admin Alert - New Order", body);
    }

    public void notifyAdminLowStock(String toyName, int stockQuantity) {
        String body = "Low stock alert from ToyMart.\n\n"
                + "Toy: " + toyName + "\n"
                + "Current stock: " + stockQuantity + "\n\n"
                + "Please restock this item soon.";
        sendEmail(adminEmail, "ToyMart Low Stock Alert", body);
    }

    public void notifyAdminNewReview(Review review) {
        String body = "A new customer review was submitted.\n\n"
                + "Customer: " + review.getUsername() + "\n"
                + "Toy: " + review.getToyName() + "\n"
                + "Rating: " + review.getRating() + "/5\n"
                + "Comment: " + review.getComment() + "\n\n"
                + "Please moderate it from the admin reviews page.";
        sendEmail(adminEmail, "ToyMart Admin Alert - New Review", body);
    }

    public void notifyAdminNewSupplier(String supplierName, String category, String email) {
        String body = "A new supplier was added to ToyMart.\n\n"
                + "Supplier: " + supplierName + "\n"
                + "Category: " + category + "\n"
                + "Email: " + email;
        sendEmail(adminEmail, "ToyMart Admin Alert - New Supplier", body);
    }

    public void notifyAdminContactMessage(String customerName, String customerEmail,
                                          String subject, String message) {
        String body = "A customer submitted a ToyMart contact message.\n\n"
                + "Customer: " + customerName + "\n"
                + "Email: " + customerEmail + "\n"
                + "Subject: " + subject + "\n\n"
                + message;
        sendEmail(adminEmail, "ToyMart Contact Message - " + subject, body);
    }

    public int sendPromotionalEmail(List<User> recipients, String subject, String message) {
        int sent = 0;
        for (User user : recipients) {
            if (user.getEmail() != null && !user.getEmail().trim().isEmpty()) {
                sendEmail(user.getEmail(), subject, message);
                sent++;
            }
        }
        return sent;
    }

    public void sendEmail(String to, String subject, String body) {
        if (!isMailReady()) {
            System.out.println("[ToyMart email skipped] To: " + to + " | Subject: " + subject);
            return;
        }
        try {
            if (isBrevoReady()) {
                sendEmailWithBrevo(to, subject, body);
                return;
            }

            SimpleMailMessage mail = new SimpleMailMessage();
            mail.setFrom(fromAddress);
            mail.setTo(to);
            mail.setSubject(subject);
            mail.setText(body);
            mailSender.send(mail);
        } catch (RuntimeException ex) {
            System.err.println("ToyMart email failed: " + ex.getMessage());
        }
    }

    private boolean isMailReady() {
        return mailEnabled && (isBrevoReady()
                || (mailSender != null
                && username != null
                && !username.trim().isEmpty()
                && password != null
                && !password.trim().isEmpty()));
    }

    private boolean isBrevoReady() {
        return brevoApiKey != null && !brevoApiKey.trim().isEmpty();
    }

    private void sendEmailWithBrevo(String to, String subject, String body) {
        try {
            String payload = "{"
                    + "\"sender\":{\"name\":\"ToyMart\",\"email\":\"" + escapeJson(fromAddress) + "\"},"
                    + "\"to\":[{\"email\":\"" + escapeJson(to) + "\"}],"
                    + "\"subject\":\"" + escapeJson(subject) + "\","
                    + "\"textContent\":\"" + escapeJson(body) + "\""
                    + "}";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.brevo.com/v3/smtp/email"))
                    .timeout(Duration.ofSeconds(20))
                    .header("api-key", brevoApiKey)
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(payload))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                System.err.println("ToyMart Brevo email failed: HTTP " + response.statusCode()
                        + " - " + response.body());
            }
        } catch (Exception ex) {
            System.err.println("ToyMart Brevo email failed: " + ex.getMessage());
        }
    }

    private String escapeJson(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
