package com.refycap.email_receiver.config;

import com.sun.mail.imap.IMAPFolder;

import javax.mail.MessagingException;

public class KeepAliveRunnable implements Runnable {
    private static final long KEEP_ALIVE_FREQ = 300000; // 5 minutes
    private IMAPFolder folder;

    public KeepAliveRunnable(IMAPFolder folder) {
        this.folder = folder;
    }


    @Override
    public void run() {
        while (!Thread.interrupted()) {
            try {
                Thread.sleep(KEEP_ALIVE_FREQ);
                System.out.println("Performing a NOOP to keep the connection alive");
                folder.doCommand(protocol -> {
                    protocol.simpleCommand("NOOP", null);
                    return null;
                });
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (MessagingException e) {
                System.out.println("Unexpected exception while keeping alive the IDLE connection");
                e.printStackTrace();
            }
        }
    }
}
