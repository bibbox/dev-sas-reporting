package sasreporting.redcap.jasper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;

import javax.swing.table.DefaultTableModel;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRPrintPage;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRTableModelDataSource;
import net.sf.jasperreports.view.JasperViewer;

public class REDCapJRXMLTemplateCompiler {

	
	private static final Logger logger = LogManager.getLogger(REDCapJRXMLTemplateCompiler.class);
	
	public final static String FIELD_JRXML_TEMPLATE = "jrxml";
	public final static int PAGE_WIDTH_A4 = 595;
	public final static int PAGE_HEIGHT_A4 = 842;	
	public final static int PAGE_MARGIN = 25;
	
	
	private JRTableModelDataSource[] readRecordCSV(InputStream csvInput) throws IOException {
		
		CSVParser csvp = new CSVParser(new BufferedReader(new InputStreamReader(csvInput)), CSVFormat.DEFAULT);
		
		List<CSVRecord> records = csvp.getRecords();
		
		JRTableModelDataSource[] jrtmRecords = new JRTableModelDataSource[records.size()];
		
		if(records.size() == 0) {
			
			csvp.close();
			
			return jrtmRecords;
		}
		
		CSVRecord header = records.get(0);
		
		String[] headerColumns = new String[header.size()];
		
		for(int i=0; i<headerColumns.length; i++) {
			
			headerColumns[i]=header.get(i);
		}
		
		for(int j=1; j<records.size(); j++) {
			
			String[][] data = new String[1][headerColumns.length];
			
			CSVRecord csvRecord = records.get(j);
			
			for(int i=0; i<headerColumns.length; i++) {
				
				data[0][i]=csvRecord.get(i);
				//System.out.println(headerColumns[i] + ": " + data[0][i].length());
			}
			
			DefaultTableModel dtModel = new DefaultTableModel(data, headerColumns);
			
			jrtmRecords[j-1]= new JRTableModelDataSource(dtModel);
		}
		
		csvp.close();
		
		return jrtmRecords;
	}
	
	
	public JasperPrint getReportFromJRXML(InputStream csvSrc, InputStream jrxmlSrc) throws IOException, JRException {
			
		JasperReport jr = JasperCompileManager.compileReport(jrxmlSrc);
		
		JRTableModelDataSource[] records = readRecordCSV(csvSrc);
		
		JasperPrint firstPrint;
		
		if(records.length > 0) {
		
			firstPrint = JasperFillManager.fillReport(jr, new HashMap<String, Object>(), records[0]);      
		
			firstPrint.setPageWidth(PAGE_WIDTH_A4);
			firstPrint.setPageHeight(PAGE_HEIGHT_A4);
			firstPrint.setTopMargin(PAGE_MARGIN);
			firstPrint.setBottomMargin(PAGE_MARGIN);
			firstPrint.setLeftMargin(PAGE_MARGIN);
			firstPrint.setRightMargin(PAGE_MARGIN);
		    
			for(int j=1; j<records.length; j++) {
		    
				JasperPrint jasperPrint = JasperFillManager.fillReport(jr, new HashMap<String, Object>(), records[j]);
				jasperPrint.setPageWidth(PAGE_WIDTH_A4);
				jasperPrint.setPageHeight(PAGE_HEIGHT_A4);
				jasperPrint.setTopMargin(PAGE_MARGIN);
				jasperPrint.setBottomMargin(PAGE_MARGIN);
				jasperPrint.setLeftMargin(PAGE_MARGIN);
				jasperPrint.setRightMargin(PAGE_MARGIN);
		    
				List<JRPrintPage> pages = jasperPrint.getPages();	
		    	
				for (int i = 0; i < pages.size(); i++) {
	                
					JRPrintPage object = (JRPrintPage)pages.get(i);
					firstPrint.addPage(object);
				}
			}
		}
		
		else {
			
			firstPrint = new JasperPrint();
			logger.info("No record(s) returned");
			firstPrint.setName("No record(s)");
		}
			
		return firstPrint;
	}
	
	
	public void exportJasperPrintToPDF(JasperPrint jp, String pdfDestPath) throws JRException, IOException {
		
		OutputStream output = new FileOutputStream(new File(pdfDestPath)); 
		JasperExportManager.exportReportToPdfStream(jp, output); 
		
		logger.info("Generated PDF file: " + pdfDestPath + " from JRXML template: " + jp.getName());
		
		output.close();
	}
	
	
	public void showJasperViewer(JasperPrint jp) {
		
		JasperViewer jasperViewer = new JasperViewer(jp);
		jasperViewer.setVisible(true);   
	}
	
}
