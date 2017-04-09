package sasreporting.redcap.mail;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.mail.ByteArrayDataSource;
import org.apache.commons.mail.EmailAttachment;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;
import org.apache.commons.validator.routines.EmailValidator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class REDCapMailClient {

	private static final Logger logger = LogManager.getLogger(REDCapMailClient.class);
	
	public static String PROPERTIES_FILE = "/config/mail.properties";
	public static String MAIL_SERVER = "mail_server";
	public static String MAIL_SENDER = "mail_sender";
	public static String MAIL_USER = "mail_user";
	public static String MAIL_PASSWORD = "mail_password";
	public static String MAIL_TLS = "mail_tls";
	public static String MAIL_SSL = "mail_ssl";
	public static String MAIL_CHARSET = "mail_charset";
	public static String MAIL_CC = "mail_cc";
	
	private final static String MSG_INVALID_TO = "No valid email address specified ! Email was not sent.";
	
	private Properties mailConf;
	
	
	public REDCapMailClient() throws FileNotFoundException, IOException {
		
		mailConf = new Properties();
		FileReader fr = new FileReader(PROPERTIES_FILE);
		mailConf.load(fr);
		fr.close();
		
	}

	
	public String sendMailWithREDCapRecordAttachementFile(String to, File attachmentFile, String subject, String message) throws EmailException, FileNotFoundException, IOException {
		
		HtmlEmail email = new HtmlEmail();
		email.setHostName(mailConf.getProperty(MAIL_SERVER));
		email.setFrom(mailConf.getProperty(MAIL_SENDER));
		email.addTo(to);
		
		EmailValidator ev = EmailValidator.getInstance();
		
		if(to == null || "".equals(to) || !ev.isValid(to)) {
			
			return MSG_INVALID_TO;
		}
		
		email.setAuthentication(mailConf.getProperty(MAIL_USER), mailConf.getProperty(MAIL_PASSWORD));
		email.setSubject(subject);
		email.setTextMsg(message);
		email.setSSL(true);
		email.setCharset(mailConf.getProperty(MAIL_CHARSET));
		
		FileInputStream fis = new FileInputStream(attachmentFile);
		
		email.attach(new ByteArrayDataSource(fis, "application/pdf"),
			      "record.pdf", "Auto Generated Report",
			       EmailAttachment.ATTACHMENT);
		
		String sent = email.send();
		fis.close();
		
	    logger.info("PDF report sent to: " + to);
		
		return sent;
	}
	
	
	public String sendMailWithREDCapRecordAttachement(String to, String cc, InputStream attachment, String attachmentName, String subject, String message) throws EmailException, FileNotFoundException, IOException {
		
		HtmlEmail email = new HtmlEmail();
		email.setHostName(mailConf.getProperty(MAIL_SERVER));
		email.setFrom(mailConf.getProperty(MAIL_SENDER));
		email.addTo(to);
		
		EmailValidator ev = EmailValidator.getInstance();
		
		if(to == null || "".equals(to) || !ev.isValid(to)) {
			
			return MSG_INVALID_TO;
		}
		
		if(cc != null && !("".equals(cc)) && ev.isValid(cc)) {
			
			email.addCc(cc);
		}
		
		email.setAuthentication(mailConf.getProperty(MAIL_USER), mailConf.getProperty(MAIL_PASSWORD));
		email.setSubject(subject);
		email.setTextMsg(message);
		email.setSSL(Boolean.parseBoolean(mailConf.getProperty(MAIL_SSL)));
		email.setCharset(mailConf.getProperty(MAIL_CHARSET));
		email.attach(new ByteArrayDataSource(attachment, "application/pdf"),
				attachmentName + ".pdf", attachmentName,
			       EmailAttachment.ATTACHMENT);
		
		String sent = email.send();
		attachment.close();
		
	    logger.info("PDF report sent to: " + to);
		
		return sent;
	}
}
