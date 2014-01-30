package nl.amnesty.crm.mail.bean;

import java.util.List;

/**
 *
 * @author root
 */
public interface MessageFacadeLocal {

    public void sendEmail(String smtphost, String debug, String sender, List<String> recipientlist, String subject, String text);
}