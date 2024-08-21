package com.refycap.email_receiver.service;

import com.refycap.email_receiver.config.KeepAliveRunnable;
import com.sun.mail.imap.IMAPFolder;
import jakarta.mail.BodyPart;
import jakarta.mail.Part;
import jakarta.mail.internet.MimeMultipart;
import lombok.RequiredArgsConstructor;


import javax.mail.Address;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.event.MessageCountAdapter;
import javax.mail.event.MessageCountEvent;
import java.io.IOException;
import java.util.Arrays;

@RequiredArgsConstructor
public class EmailListener extends MessageCountAdapter {

    private final Session session;
    private final String username;
    private final String password;


    public void startListening() throws MessagingException {
        Store store = session.getStore("imaps");
        store.connect(username, password);


        IMAPFolder inbox = (IMAPFolder) store.getFolder("INBOX");
        inbox.open(Folder.READ_ONLY);

        // Create a new thread to keep the connection alive
        Thread keepAliveThread = new Thread(new KeepAliveRunnable(inbox), "IdleConnectionKeepAlive");
        keepAliveThread.start();

        inbox.addMessageCountListener(new MessageCountAdapter() {
            @Override
            public void messagesAdded(MessageCountEvent event) {
                Message[] messages = event.getMessages();
                for (Message message : messages) {
                    try {
                        System.out.println("New email received: From:" + Arrays.toString(message.getFrom()));
                        printAllCCMailIDS(messages);
                        System.out.println("Subject: " + message.getSubject());
                        String body = getTextFromMessage(message);
                        System.out.println("Body: " + body);
                        printAttachments(message);
                    } catch (MessagingException e) {
                        e.printStackTrace();
                    } catch (jakarta.mail.MessagingException e) {
                        throw new RuntimeException(e);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });
// Start the IDLE Loop
        while (!Thread.interrupted()) {
            try {
                System.out.println("Starting IDLE");
                inbox.idle();
            } catch (MessagingException e) {
                System.out.println("Messaging exception during IDLE");
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }

        // Interrupt and shutdown the keep-alive thread
        if (keepAliveThread.isAlive()) {
            keepAliveThread.interrupt();
        }
    }

    private String getTextFromMessage(Message message) throws MessagingException, IOException, jakarta.mail.MessagingException {
        if (message.isMimeType("text/plain")) {
            return message.getContent().toString();
        } else if (message.isMimeType("multipart/*")) {
            MimeMultipart mimeMultipart = (MimeMultipart) message.getContent();
            return getTextFromMimeMultipart(mimeMultipart);
        }
        return "";
    }

    private void printAttachments(Message message) throws MessagingException, IOException, jakarta.mail.MessagingException {
        if (message.isMimeType("multipart/*")) {
            MimeMultipart mimeMultipart = (MimeMultipart) message.getContent();
            int count = mimeMultipart.getCount();
            for (int i = 0; i < count; i++) {
                BodyPart bodyPart = mimeMultipart.getBodyPart(i);
                if (Part.ATTACHMENT.equalsIgnoreCase(bodyPart.getDisposition())) {
                    System.out.println("Attachment: " + bodyPart.getFileName());
                }
            }
        } else if (message.isMimeType("message/rfc822")) {
            // Handle attached messages
            printAttachments((Message) message.getContent());
        }
    }
    private String getTextFromMimeMultipart(MimeMultipart mimeMultipart) throws MessagingException, IOException, jakarta.mail.MessagingException {
        StringBuilder result = new StringBuilder();
        int count = mimeMultipart.getCount();
        for (int i = 0; i < count; i++) {
            BodyPart bodyPart = mimeMultipart.getBodyPart(i);
            if (bodyPart.isMimeType("text/plain")) {
                result.append(bodyPart.getContent());
            } else if (bodyPart.isMimeType("text/html")) {
                // If you prefer HTML content over plain text
                result.append(bodyPart.getContent());
            } else if (bodyPart.getContent() instanceof MimeMultipart) {
                result.append(getTextFromMimeMultipart((MimeMultipart) bodyPart.getContent()));
            }
        }
        return result.toString();
    }

    private void printAllCCMailIDS(Message[] messages) throws MessagingException {

        for (Message message : messages) {
            Address[] ccRecipients = null;
            try {
                ccRecipients = message.getRecipients(Message.RecipientType.CC);
            } catch (MessagingException e) {
                throw new MessagingException("Error ");
            }
            if (ccRecipients != null) {
                System.out.println("CC: " + Arrays.toString(ccRecipients));
            } else {
                System.out.println("CC: None");
            }
        }


    }
}
