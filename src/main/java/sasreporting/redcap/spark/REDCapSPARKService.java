package sasreporting.redcap.spark;

import static spark.Spark.*;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.mail.EmailException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import sasreporting.redcap.con.REDCapHttpConnector;
import sasreporting.redcap.jasper.REDCapJRXMLTemplateCompiler;
import sasreporting.redcap.mail.REDCapMailClient;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperPrint;

import java.io.OutputStream;
import java.io.FileOutputStream;


public class REDCapSPARKService {

	
	private static final Logger logger = LogManager.getLogger(REDCapSPARKService.class);
	
	public static String PROPERTIES_FILE_MAIL = "/config/mail.properties";
	public static String PROPERTIES_FILE_REPORT_ALERT = "/config/service.properties";
	
	private static final String FIELD_PROJECT = "project_id";
	private static final String FIELD_RECORD = "record";
	
	public static final String FIELD_PROJECTS_TEMPLATE = "template";
	public static final String FIELD_PROJECTS_MAIL = "mail";
	
	public static final String FIELD_SERVICE_PROJECTS = "project_file";
	public static final String FIELD_SERVICE_TEMPLATE_DIR = "template_dir";
	public static final String FIELD_SERVICE_MAIL_DIR = "mail_dir";
	public static final String FIELD_SERVICE_MAIL = "mail_field";
	public static final String FIELD_SERVICE_SEND = "send_field";
	public static final String FIELD_SERVICE_CC = "cc_field";

	public static final String REDCAP_FIELD_URL = "redcap_url";
	
	public static final String SUBMIT_INTERNAL = "Generate report for internal use";
	public static final String SUBMIT_INTERNAL_ERIC = "Generate report and submit results to HEAP";//Generate report and submit results to BBMRI-ERIC";
	
	private static Properties mailConf, serviceConf;
	
	
	public REDCapSPARKService() {
		
		//BasicConfigurator.configure();
		
		//staticFileLocation("/public");
	}
	
	/*
	 * Initializes the REDCap report tool SPARK service
	 */
	
	public void init() {
		
		logger.info("Starting REDCap listener");
		
		try {
			mailConf = new Properties();
			mailConf.load(new FileReader(REDCapSPARKService.PROPERTIES_FILE_MAIL));
			
			serviceConf = new Properties();
			serviceConf.load(new FileReader(REDCapSPARKService.PROPERTIES_FILE_REPORT_ALERT));
			
			before((request, response) -> {

				try {
					Map<String, String[]> queryMap = request.queryMap().toMap();

					String url = serviceConf.getProperty(REDCapHttpConnector.FIELD_URL);

					boolean authenticated = (queryMap.get(REDCapSPARKService.REDCAP_FIELD_URL) != null && url.startsWith(queryMap.get(REDCapSPARKService.REDCAP_FIELD_URL)[0]));

					if (!authenticated) {
						halt(401, "Invalid REDCap service");

					}
				} catch (Exception e) {

					logger.error(e.getMessage());
				}
			});
			
			post("/redcap_trigger_receive/", (request, response) -> {

				try {
				
					logger.info("Incoming event:\n");

					Map<String, String[]> queryMap = request.queryMap().toMap();

					String url = serviceConf.getProperty(REDCapHttpConnector.FIELD_URL);



					logger.info("REDCap Url: " + queryMap.get(REDCapSPARKService.REDCAP_FIELD_URL)[0]);

					String[] projectIDs = queryMap.get(REDCapSPARKService.FIELD_PROJECT);
					String[] recordIDs = queryMap.get(REDCapSPARKService.FIELD_RECORD);

					for(int i=0; i<recordIDs.length; i++) {

						String[] projectTokenAndTemplate = getProjectTokenandTemplate(projectIDs[i]);

						REDCapHttpConnector httpCon = new REDCapHttpConnector(url, projectTokenAndTemplate[0]);

						String mailFields = serviceConf.getProperty(REDCapSPARKService.FIELD_SERVICE_MAIL)+","+serviceConf.getProperty(REDCapSPARKService.FIELD_SERVICE_CC);

						InputStream mailInfo = httpCon.getRecordsFromREDCapProject(recordIDs[i], mailFields, "csv");

						CSVParser csvpMailInfo = new CSVParser(new InputStreamReader(mailInfo), CSVFormat.DEFAULT.withHeader());
						Map<String, Integer> csvpHeader = csvpMailInfo.getHeaderMap();
						List<CSVRecord> mainInfRecords = csvpMailInfo.getRecords();

						csvpMailInfo.close();
						mailInfo.close();

						if(mainInfRecords.size() > 0) {

							CSVRecord record = mainInfRecords.get(0);

							String to = record.get(csvpHeader.get(serviceConf.getProperty(REDCapSPARKService.FIELD_SERVICE_MAIL)));

							String ericSubmitField = record.get(csvpHeader.get(serviceConf.getProperty(REDCapSPARKService.FIELD_SERVICE_CC)));

							if(SUBMIT_INTERNAL.equals(ericSubmitField) || SUBMIT_INTERNAL_ERIC.equals(ericSubmitField)) {

								boolean ccEnabled = false;

								if(SUBMIT_INTERNAL_ERIC.equals(ericSubmitField)) {

									ccEnabled = true;
								}

								logger.info("Sending email to: " + to + " CC enabled: " + ccEnabled);

								if(to == null || "".equals(to)) {

									logger.info("No valid mail address");
									halt(401, "No valid mail address");
								}

								logger.info("Project ID: " + projectIDs[i] + ", Record ID: " + recordIDs[i]);

								String jrxmlTemplatePath = serviceConf.getProperty(REDCapSPARKService.FIELD_SERVICE_TEMPLATE_DIR) + File.separator + projectTokenAndTemplate[1];

								String mailPath = serviceConf.getProperty(REDCapSPARKService.FIELD_SERVICE_MAIL_DIR) + File.separator + projectTokenAndTemplate[2];

								logger.info("JRXML template path: " + jrxmlTemplatePath);

								logger.info("Mail path: " + mailPath);

								sendMail(httpCon, recordIDs[i], to, jrxmlTemplatePath, mailPath, ccEnabled);
							}
						}
					}
				} catch (Exception e) {

					logger.error(e.getMessage());
				}
				return true;
			});
		
		} catch (Exception e) {
			
			logger.error(e.getMessage());
		}
	}
	
	/**
	 * (i) Generates a PDF report from a REDCap project record and a jrxml-based report template
	 * (ii) Sends a generated PDF report to a mail recipient specified within the record 
	 * 
	 * @param httpCon HttpConnector instance
	 * @param recordID REDCap record ID
	 * @param to Email recipient
	 * @param jrxmlTemplatePath JRXML template file path
	 * @param ccEnabled True, if the report should be sent to a mail recipient
	 * 
	 * @throws EmailException If email could not be sent
	 * @throws IOException If a resource file (mail.txt, template.jrxml) is missing or not accessible 
	 * @throws JRException If the report could not be generated 
	 * @throws UnsupportedOperationException If a record stream cannot be read
	 * @throws KeyStoreException 
	 * @throws NoSuchAlgorithmException 
	 * @throws KeyManagementException 
	 *
	 */
	
	public void sendMail(REDCapHttpConnector httpCon, String recordID, String to, String jrxmlTemplatePath, String mailPath, boolean ccEnabled) throws UnsupportedOperationException, IOException, JRException, EmailException, KeyManagementException, NoSuchAlgorithmException, KeyStoreException {
		
		InputStream record = httpCon.getRecordsFromREDCapProject(recordID, "", "csv");
		
		FileInputStream jrxmlTemplate = new FileInputStream(jrxmlTemplatePath);
		
		REDCapJRXMLTemplateCompiler compiler = new REDCapJRXMLTemplateCompiler();
		
		JasperPrint jp = compiler.getReportFromJRXML(record, jrxmlTemplate);
	
		jrxmlTemplate.close();

		///Test save report
		//OutputStream output = new FileOutputStream(new File("test_output/Report.pdf"));
		//JasperExportManager.exportReportToPdfStream(jp, output);
		//output = new FileOutputStream(new File("test_output/Report.jrxml"));
		//JasperExportManager.exportReportToXmlFile(jp ,  "test_output/Report.jrxml",false);
		///

		String reportName = FilenameUtils.removeExtension((new File(jrxmlTemplatePath).getName()));
		
		String cc = "";
		
		if(ccEnabled) {
			
			cc = mailConf.getProperty(REDCapMailClient.MAIL_CC);
		}
		
		logger.info("CC Address: " + cc);
		
		REDCapMailClient mailClient = new REDCapMailClient();
		
		String sent = mailClient.sendMailWithREDCapRecordAttachement(to, cc, new ByteArrayInputStream(JasperExportManager.exportReportToPdf(jp)), reportName, "REDCAP Report Event Mail", readMailText(mailPath));
		
		logger.info(sent);
		
		record.close();
	}
	
	/**
	 * 
	 * @param projectID REDCap project ID
	 * @return REDCap API token and associated jrxml template path
	 * @throws FileNotFoundException If the project.csv file is missing 
	 * @throws IOException If the csv input stream cannot be read
	 */
	
	public String[] getProjectTokenandTemplate(String projectID) throws FileNotFoundException, IOException {
		
		CSVParser csvpProject = new CSVParser(new InputStreamReader(
				new FileInputStream(serviceConf.getProperty(REDCapSPARKService.FIELD_SERVICE_PROJECTS))), CSVFormat.DEFAULT.withHeader());
		
		List<CSVRecord> projects = csvpProject.getRecords();
		
		Map<String, Integer> csvpHeader = csvpProject.getHeaderMap();
		
		csvpProject.close();
		
		for(CSVRecord project: projects) {
			
			if(projectID.equals(project.get(csvpHeader.get(REDCapSPARKService.FIELD_PROJECT)))) {
				
				String[] tokenAndTemplate = new String[3];
				tokenAndTemplate[0] = project.get(csvpHeader.get(REDCapHttpConnector.FIELD_TOKEN));
				tokenAndTemplate[1] = project.get(csvpHeader.get(REDCapSPARKService.FIELD_PROJECTS_TEMPLATE));
				tokenAndTemplate[2] = project.get(csvpHeader.get(REDCapSPARKService.FIELD_PROJECTS_MAIL));
				
				return tokenAndTemplate;
			}
		}
		return null;
	}
	
	/**
	 * 
	 * @return Mail text body
	 * @throws IOException If the mail.txt file cannot be accessed
	 */
	
	private String readMailText(String path) throws IOException {
		
		File file = new File(path);
		FileInputStream fis = new FileInputStream(file);
		byte[] data = new byte[(int) file.length()];
		fis.read(data);
		fis.close();

		String str = new String(data, "UTF-8");
		
		return str;
	}
}
