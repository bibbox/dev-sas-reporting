package sasreporting.redcap.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang.StringUtils;

import sasreporting.redcap.con.REDCapHttpConnector;
import sasreporting.redcap.jasper.REDCapJRXMLTemplateCompiler;
import sasreporting.redcap.jasper.REDCapJRXMLTemplateGenerator;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;

public class TemplateAndReportGeneratorTest {

	private static String api_url = "https://heap.bibbox.org/api/";

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		testGenerateJRXMLTemplates();
		//testCompileJRXMLTemplates();
	}

	
	public static void testGenerateJRXMLTemplates() {
		
		BufferedReader in;
		try {
			
			in = new BufferedReader(new InputStreamReader(new FileInputStream("dev-config/projects.csv")));
			
			CSVParser csvp = new CSVParser(in, CSVFormat.DEFAULT.withHeader());
			
			Map<String, Integer> headerMap = csvp.getHeaderMap();
			List<CSVRecord> records = csvp.getRecords();
		
			for(CSVRecord record : records) {
				
				String project = StringUtils.trim(record.get(headerMap.get("project")));
				String token = StringUtils.trim(record.get(headerMap.get("token")));
				testGenerateJRXMLTemplate(project, token);
			}
			
			in.close();
			csvp.close();
			
		} catch (IOException e) {
			
			e.printStackTrace();
		}
	}
	
	
	public static void testGenerateJRXMLTemplate(String jrxmlTemplateName, String token) {
		
		REDCapHttpConnector httpCon 
		= new REDCapHttpConnector(api_url, token);
		
		try {
			
			InputStream projectInfo = httpCon.getInfoFromREDCapProject("project_title", "csv");
			InputStream metadata = httpCon.getMetadataFromREDCapProject("csv");
			
			REDCapJRXMLTemplateGenerator jrxmlGenerator = new REDCapJRXMLTemplateGenerator(projectInfo, metadata);
			
			JasperReport jr = jrxmlGenerator.generateREDCapRecordJRXMLTemplateWithBandPerElement();
			
			File jrxmlTemplateFile = jrxmlGenerator.writeJRXMLTemplateToFile(jr, "dev-config/templates/" + jrxmlTemplateName + ".jrxml");
			
	
			
			metadata.close();
			
		} catch (UnsupportedOperationException | IOException | JRException | KeyManagementException | NoSuchAlgorithmException | KeyStoreException e) {
			
			e.printStackTrace();
		}
	}
	
	
public static void testCompileJRXMLTemplates() {
		
		BufferedReader in;
		
		try {
			
			in = new BufferedReader(new InputStreamReader(new FileInputStream("dev-config/projects.csv")));
			
			CSVParser csvp = new CSVParser(in, CSVFormat.DEFAULT.withHeader());
			
			Map<String, Integer> headerMap = csvp.getHeaderMap();
			List<CSVRecord> records = csvp.getRecords();
		
			for(CSVRecord record : records) {
				
				String project = StringUtils.trim(record.get(headerMap.get("project")));
				String token = StringUtils.trim(record.get(headerMap.get("token")));
				testCompileJRXMLTemplate(project, token);
			}
			
			in.close();
			csvp.close();
			
		} catch (IOException e) {
			
			e.printStackTrace();
		}
	}
	
	
	public static void testCompileJRXMLTemplate(String jrxmlTemplateName, String token) {
		
		REDCapHttpConnector httpCon 
		= new REDCapHttpConnector(api_url, token);
		
		InputStream record;
		
		try {
			
			record = httpCon.getRecordsFromREDCapProject("2", null, "csv");
			
		
			REDCapJRXMLTemplateCompiler jrxmlCompiler = new REDCapJRXMLTemplateCompiler();
			
			File jrxmlTemplateFile = new File("dev-config/templates/" + jrxmlTemplateName + ".jrxml");
			
		
			JasperPrint redcapPrint = jrxmlCompiler.getReportFromJRXML(record, new FileInputStream(jrxmlTemplateFile));
			
			
			jrxmlCompiler.exportJasperPrintToPDF(redcapPrint, "dev-config/reports/" + jrxmlTemplateName + ".pdf");
			
			
			jrxmlCompiler.showJasperViewer(redcapPrint);
			
			record.close();
			
		} catch (UnsupportedOperationException | IOException | JRException | KeyManagementException | NoSuchAlgorithmException | KeyStoreException e) {
			
			e.printStackTrace();
		}
	}
}
