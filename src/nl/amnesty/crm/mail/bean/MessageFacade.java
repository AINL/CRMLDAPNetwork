package nl.amnesty.crm.mail.bean;

import java.util.List;
import java.util.Properties;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/**
 *
 * @author root
 */
public class MessageFacade implements MessageFacadeLocal {

    /**
     *
     * @param recipient
     * @param subject
     * @param text
     */
    public void sendEmail(String smtphost, String debug, String sender, List<String> recipientlist, String subject, String text) {
        try {

            Properties properties = new Properties();

            properties.setProperty("mail.host", smtphost);
            properties.setProperty("mail.from", sender);
            properties.setProperty("mail.debug", debug);

            Session session = Session.getDefaultInstance(properties);
            MimeMessage mimemessage = new MimeMessage(session);
            for (String recipient : recipientlist) {
                mimemessage.addRecipient(Message.RecipientType.TO, new InternetAddress(recipient));
            }
            mimemessage.setSubject(subject);
            mimemessage.setText(text);
            Transport.send(mimemessage);
        } catch (MessagingException me) {
            me.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
