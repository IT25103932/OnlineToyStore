package com.OnlineToyStore.Sllit.service;

import com.OnlineToyStore.Sllit.model.ContactMessage;
import com.OnlineToyStore.Sllit.util.FileStorageUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class MessageService {

    @Value("${data.file.path}")
    private String dataFilePath;

    private String getFilePath() {
        return FileStorageUtil.ensureDataFilePath(dataFilePath, "messages.txt");
    }

    public List<ContactMessage> getAllMessages() {
        List<ContactMessage> messages = new ArrayList<>();
        File file = new File(getFilePath());
        if (!file.exists()) {
            return messages;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    messages.add(ContactMessage.fromFileString(line));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return messages;
    }

    public List<ContactMessage> getMessagesByUser(String userId) {
        return getAllMessages().stream()
                .filter(message -> userId.equals(message.getUserId()))
                .collect(Collectors.toList());
    }

    public ContactMessage getMessageById(String messageId) {
        return getAllMessages().stream()
                .filter(message -> message.getMessageId().equals(messageId))
                .findFirst()
                .orElse(null);
    }

    public String submitMessage(ContactMessage message) {
        if (message.getSubject() == null || message.getSubject().trim().isEmpty()) {
            return "Subject is required.";
        }
        if (message.getComment() == null || message.getComment().trim().isEmpty()) {
            return "Message is required.";
        }

        message.setMessageId("MSG-" + UUID.randomUUID().toString()
                .substring(0, 8).toUpperCase());
        message.setStatus("OPEN");

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(getFilePath(), true))) {
            writer.write(message.toFileString());
            writer.newLine();
        } catch (IOException e) {
            e.printStackTrace();
            return "Error saving message.";
        }
        return "success";
    }

    public String replyToMessage(String messageId, String reply) {
        if (reply == null || reply.trim().isEmpty()) {
            return "Reply cannot be empty.";
        }

        List<ContactMessage> messages = getAllMessages();
        boolean updated = false;
        for (ContactMessage message : messages) {
            if (message.getMessageId().equals(messageId)) {
                message.setReply(reply.trim());
                message.setStatus("REPLIED");
                updated = true;
                break;
            }
        }

        if (!updated) {
            return "Message not found.";
        }
        saveAll(messages);
        return "success";
    }

    public long countOpenMessages() {
        return getAllMessages().stream()
                .filter(message -> "OPEN".equalsIgnoreCase(message.getStatus()))
                .count();
    }

    public long countTotalMessages() {
        return getAllMessages().size();
    }

    private void saveAll(List<ContactMessage> messages) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(getFilePath(), false))) {
            for (ContactMessage message : messages) {
                writer.write(message.toFileString());
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
