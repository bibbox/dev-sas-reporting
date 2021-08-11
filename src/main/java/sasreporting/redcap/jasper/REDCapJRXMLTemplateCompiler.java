package sasreporting.redcap.jasper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
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

import net.sf.jasperreports.engine.JREmptyDataSource;

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
		///Test
		String[] findables = new String[] {"metadata_is_identified_by", "data_is_identified_by_a_pe", "metadata_is_identified_by_a_gobally_unique_identifier", "data_is_identified_by_a_gl", "rich_metadata_is_provided", "metadata_includes_the_iden", "metadata_is_offered_in_suc"};
		//JRTableModelDataSource[] jrtmRecords = new JRTableModelDataSource[findables.length];

		///Test
		if(records.size() == 0) {
			
			csvp.close();
			
			return jrtmRecords;
		}
		
		CSVRecord header = records.get(0);
		
		String[] headerColumns = new String[3];
//		String[] headerColumns = new String[header.size()];

//		for(int i=0; i<headerColumns.length-3; i++) {
////		for(int i=0; i<headerColumns.length; i++) {
//			headerColumns[i]=header.get(i);
//		}
		///Test
		headerColumns[headerColumns.length-3]="Findable_Series";
		headerColumns[headerColumns.length-2]="Findable_Value";
		headerColumns[headerColumns.length-1]="Findable_Category";

		for(int j=1; j<records.size(); j++) {


			Object[][] data = new Object[findables.length][headerColumns.length];
//			findables.length
//			CSVRecord csvRecord = records.get(j);
//
//			for(int i=0; i<headerColumns.length-3; i++) {
//
//				data[0][i]=csvRecord.get(i);
//				//System.out.println(headerColumns[i] + ": " + data[0][i].length());
//				logger.info("data: "+headerColumns[i]+ ": "+data[0][i]);
//			}
			///Test
//			data[0][headerColumns.length-3]= "Test";//new String[]{"Test","Test","Test"};///
//			data[0][headerColumns.length-2]= new int[]{1, 2, 0};
//			data[0][headerColumns.length-1]= new String[]{findables[0], findables[1], findables[2]};
			for(int test=0;test< data.length;test++){
				//data[test]=data[0];
				data[test][headerColumns.length-3]= "Test";///
				data[test][headerColumns.length-2]= test;//"{1,2,0,3,1,2,0}";
				data[test][headerColumns.length-1]= findables[test];

			}
			///Test


			DefaultTableModel dtModel = new DefaultTableModel(data, headerColumns);
			logger.info("dtModel: "+dtModel.getRowCount()+" x "+dtModel.getColumnCount());
			jrtmRecords[j-1]= new JRTableModelDataSource(dtModel);

		}
		
		csvp.close();
		
		return jrtmRecords;
	}
	
	
	public JasperPrint getReportFromJRXML(InputStream csvSrc, InputStream jrxmlSrc) throws IOException, JRException {
			
		JasperReport jr = JasperCompileManager.compileReport(jrxmlSrc);

		JRTableModelDataSource[] records = readRecordCSV(csvSrc);
		logger.info("records_length: "+records.length);
		JasperPrint firstPrint;
		
		if(records.length > 0) {
			logger.info("records: "+records[0]);
			HashMap<String, Object> parameters = new HashMap<String, Object>();
			
//			parameters.put("Findable_Series", new String[]{"Findable_Series","Findable_Series"});
//			parameters.put("Findable_Value",  new String[]{"1","2"});
//			parameters.put("Findable_Category",  new String[]{"cat1","cat2"});
//			logger.info("parameters: "+parameters);

			firstPrint = JasperFillManager.fillReport(jr, parameters, records[0]);
		
			firstPrint.setPageWidth(PAGE_WIDTH_A4);
			firstPrint.setPageHeight(PAGE_HEIGHT_A4);
			firstPrint.setTopMargin(PAGE_MARGIN);
			firstPrint.setBottomMargin(PAGE_MARGIN);
			firstPrint.setLeftMargin(PAGE_MARGIN);
			firstPrint.setRightMargin(PAGE_MARGIN);
		    
			for(int j=1; j<records.length; j++) {
				logger.info("record["+j+"]: "+records[j]);
				if(records[j] == null ){
					logger.info("next ");

					continue;
				}

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
