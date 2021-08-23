package sasreporting.redcap.mail;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Properties;

public class MailTest {

    private static final Logger logger = LogManager.getLogger(MailTest.class);

    public static String PROPERTIES_FILE = "/config/mail.properties";
    public static String MAIL_SERVER = "mail_server";
    public static String MAIL_PORT_SSL = "mail_port_ssl";
    public static String MAIL_SENDER = "mail_sender";
    public static String MAIL_USER = "mail_user";
    public static String MAIL_PASSWORD = "mail_password";
    public static String MAIL_TLS = "mail_tls";
    public static String MAIL_SSL = "mail_ssl";
    public static String MAIL_CHARSET = "mail_charset";
    public static String MAIL_CC = "mail_cc";

    private final static String MSG_INVALID_TO = "No valid email address specified ! Email was not sent.";

    private Properties mailConf;

    public String sendMailTest2(String to, String cc, InputStream attachment, String attachmentName, String subject, String message) {
        try {
            Properties prop = new Properties();
            prop.put("mail.smtp.auth", true);
            prop.put("mail.smtp.starttls.enable", "true");
            prop.put("mail.smtp.starttls.required", "true");
            prop.put("mail.smtp.ssl.protocols", "TLSv1.2");
            prop.put("mail.smtp.host", mailConf.getProperty(MAIL_SERVER));
            prop.put("mail.smtp.port", mailConf.getProperty(MAIL_PORT_SSL));
            prop.put("mail.smtp.ssl.trust", mailConf.getProperty(MAIL_SERVER));

            Session session = Session.getInstance(prop, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(mailConf.getProperty(MAIL_USER), mailConf.getProperty(MAIL_PASSWORD));
                }
            });

            Message message1 = new MimeMessage(session);
            message1.setFrom(createFromAddress());
            message1.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            message1.setSubject("other mailer test" + subject);

            MimeBodyPart mimeBodyPart = new MimeBodyPart();
            mimeBodyPart.setContent(message, "text/html; charset=utf-8");

            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(mimeBodyPart);

            message1.setContent(multipart);
            Transport.send(message1);
        } catch (Exception ex) {
            logger.error("943b287c1bb8-NotificationMail ERROR-NG-0000013: Error sending mail.");
            logger.error("context", ex);
            return "error";
        }

        return "send";
    }

    private InternetAddress createFromAddress() {
        try {
            return new InternetAddress(
                    mailConf.getProperty(MAIL_SENDER),
                    mailConf.getProperty(MAIL_SENDER),
                    "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            logger.error("943b287c1bb8-NotificationMail ERROR-NG-0000014: Error creating From mail sender.");
            logger.error("context", ex);
        }
        return null;
    }
}
