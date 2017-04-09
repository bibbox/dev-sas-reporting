package sasreporting.redcap.test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.mail.EmailException;

import sasreporting.redcap.con.REDCapHttpConnector;
import sasreporting.redcap.jasper.REDCapJRXMLTemplateCompiler;
import sasreporting.redcap.mail.REDCapMailClient;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperPrint;

public class REDCapMailClientTest {

	public static void main(String[] argv) {
		
		REDCapJRXMLTemplateCompiler jrxmlCompiler = new REDCapJRXMLTemplateCompiler();
		REDCapHttpConnector httpCon 
		= new REDCapHttpConnector("https://sas.bbmri-eric.eu/api/", "6FA3DF4D6B44AF7F8CEAD59E662B15FF");
		
		InputStream record;
		
		try {
			
			File file = new File("/config/mail.txt");
			FileInputStream fis = new FileInputStream(file);
			byte[] data = new byte[(int) file.length()];
			fis.read(data);
			fis.close();

			String str = new String(data, "UTF-8");
			
			System.out.println(str);
			
			record = httpCon.getRecordsFromREDCapProject("8,", null, "csv");
			REDCapMailClient mailClient = new REDCapMailClient();
			File jrxmlTemplateFile = new File("/config/templates/BBMRI-ERIC Self-Assessment Tool (FFPE Tissue - Part 1: Isolated RNA) - ERIC-REDCap test.jrxml");
			JasperPrint redcapPrint = jrxmlCompiler.getReportFromJRXML(record, new FileInputStream(jrxmlTemplateFile));
		
			jrxmlCompiler.showJasperViewer(redcapPrint);
			
			System.out.println(mailClient.sendMailWithREDCapRecordAttachement("ph.hofer@live.at", "philipp.hofer@i-med.ac.at", new ByteArrayInputStream(JasperExportManager.exportReportToPdf(redcapPrint)), "REDCAP Test", "REDCAP Test Message", str));
			
			record.close();
		
		} catch (IOException | JRException | EmailException | KeyManagementException | UnsupportedOperationException | NoSuchAlgorithmException | KeyStoreException e) {
			
			
			e.printStackTrace();
		}
	}

}
