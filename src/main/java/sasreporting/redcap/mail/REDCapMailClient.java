package sasreporting.redcap.mail;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.*;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.mail.smtp.SMTPTransport;
import org.apache.commons.mail.ByteArrayDataSource;
import org.apache.commons.mail.EmailAttachment;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;
import org.apache.commons.validator.routines.EmailValidator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.activation.DataHandler;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;



import java.util.Properties;

import javax.mail.Message;

import javax.mail.Session;
import javax.mail.Transport;

import java.io.File;
import java.io.IOException;



public class REDCapMailClient {

	private static final Logger logger = LogManager.getLogger(REDCapMailClient.class);
	
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

	public static String OAUTH_CLIENT_ID = "oauth_client_id";
	public static String OAUTH_CLIENT_SECRET = "oauth_secret";
	public static String OAUTH_REFRESH_TOKEN = "refresh_token";
	public static String OAUTH_TOKEN_URL = "token_url";

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
		email.setSslSmtpPort(mailConf.getProperty(MAIL_PORT_SSL));
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
		// Follow instruction in video: https://www.youtube.com/watch?v=-rcRf7yswfM to enable token generation for scope gmail
		String accessToken = "";
		EmailValidator ev = EmailValidator.getInstance();
		if(to == null || "".equals(to) || !ev.isValid(to)) {
			return MSG_INVALID_TO;
		}

		String request = "client_id=" + URLEncoder.encode(mailConf.getProperty(OAUTH_CLIENT_ID), "UTF-8")
				+ "&client_secret=" + URLEncoder.encode(mailConf.getProperty(OAUTH_CLIENT_SECRET), "UTF-8")
				+ "&refresh_token=" + URLEncoder.encode(mailConf.getProperty(OAUTH_REFRESH_TOKEN), "UTF-8")
				+ "&grant_type=refresh_token";
		HttpURLConnection conn = (HttpURLConnection) new URL(mailConf.getProperty(OAUTH_TOKEN_URL)).openConnection();
		conn.setDoOutput(true);
		conn.setRequestMethod("POST");
		PrintWriter out = new PrintWriter(conn.getOutputStream());
		out.print(request); // note: println causes error
		out.flush();
		out.close();
		conn.connect();
		try {
			HashMap<String, Object> result;
			result = new ObjectMapper().readValue(conn.getInputStream(), new TypeReference<HashMap<String, Object>>() {
			});
			accessToken = (String) result.get("access_token");
//			tokenExpires = System.currentTimeMillis() + (((Number) result.get("expires_in")).intValue() * 1000);
		} catch (IOException e) {
			logger.error("ERROR while creating access token");
			String line;
			BufferedReader in = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
			while ((line = in.readLine()) != null) {
				System.out.println(line);
			}
			System.out.flush();
		}
		Properties props = new Properties();
		props.put("mail.smtp.host", "smtp.gmail.com");
		props.put("mail.smtp.ssl.enable", "true"); // required for Gmail
		props.put("mail.smtp.auth.mechanisms", "XOAUTH2");

		Session session = Session.getInstance(props);
		String sent = "Email was sent successfully";
		try{

			Message msg = new MimeMessage(session);
			msg.setFrom(new InternetAddress(mailConf.getProperty(MAIL_SENDER)));
			msg.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
			if(cc != null && !("".equals(cc)) && ev.isValid(cc)) {
				msg.addRecipient(MimeMessage.RecipientType.CC, new InternetAddress(
						cc));
			}

			msg.setSubject(subject);
			msg.setSentDate(new Date());


			MimeBodyPart messageBodyPart = new MimeBodyPart();
			messageBodyPart.setText(message);
			Multipart multipart = new MimeMultipart();
			multipart.addBodyPart(messageBodyPart);
			MimeBodyPart att = new MimeBodyPart();
			att.setDataHandler(new DataHandler(new ByteArrayDataSource(attachment, "application/pdf")));
			att.setFileName(attachmentName + ".pdf");
			multipart.addBodyPart(att);
			msg.setContent(multipart);
			msg.saveChanges();

			Transport transport = session.getTransport("smtp");
			transport.connect("smtp.gmail.com",mailConf.getProperty(MAIL_USER), accessToken);
			transport.sendMessage(msg, msg.getAllRecipients());
		}catch (Exception e){
			sent = "Sending Email failed: "+e.getMessage();
		}

		return sent;
	}


}
