package sasreporting.redcap.jasper;

import java.io.*;
import java.util.*;

import javax.swing.table.DefaultTableModel;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang.math.NumberUtils;
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
	

	private Map<String, Map<String, String>> createFAIRdicts(){
		Map<String, Map<String, String>> dictionaries = new LinkedHashMap<String, Map<String, String>>();
		Map<String, String> findables = new LinkedHashMap<String, String>();
		Map<String, String> accessibles = new LinkedHashMap<String, String>();
		Map<String, String> interoperables = new LinkedHashMap<String, String>();
		Map<String, String> reusables = new LinkedHashMap<String, String>();

		//String[] findables = new String[] {"", "", "", "data_is_identified_by_a_gl", "", "", ""};

		findables.put("metadata_is_identified_by", "F1-01M");
		findables.put("data_is_identified_by_a_pe", "F1-01D");
		findables.put("metadata_is_identified_by_a_gobally_unique_identifier", "F1-02M");
		findables.put("data_is_identified_by_a_gl", "F1-02D");
		findables.put("rich_metadata_is_provided", "F2-01M");
		findables.put("metadata_includes_the_iden", "F3-01M");
		findables.put("metadata_is_offered_in_suc", "F4-01M");
		dictionaries.put("findable",findables);

		accessibles.put("metadata_contains_informat", "A1-01M");
		accessibles.put("metadata_can_be_accessed_m", "A1-02M");
		accessibles.put("data_can_be_accessed_manua", "A1-02D");
		accessibles.put("metadata_identifier_resolv", "A1-03M");
		accessibles.put("data_identifier_resolves_t", "A1-03D");
		accessibles.put("metadata_is_accessed_throu", "A1-04M");
		accessibles.put("data_is_accessible_through", "A1-04D");
		accessibles.put("data_can_be_accessed_autom", "A1-05D");
		accessibles.put("metadata_is_accessible_thr", "A1.1-01M");
		accessibles.put("data_is_accessible_through_free_access", "A1.1-01D");
		accessibles.put("data_is_accessible_through_access_protocol", "A1.2-02D");
		accessibles.put("metadata_is_guaranteed_to", "A2-01M");
		dictionaries.put("accessible",accessibles);

		interoperables.put("metadata_uses_knowledge_re", "I1-01M");
		interoperables.put("data_uses_knowledge_repres", "I1-01D");
		interoperables.put("metadata_uses_machine_unde", "I1-02M");
		interoperables.put("data_uses_machine_understa", "I1-02D");
		interoperables.put("metadata_uses_fair_complia", "I2-01M");
		interoperables.put("data_uses_fair_compliant_v", "I2-01D");
		interoperables.put("metadata_includes_referenc", "I3-01M");
		interoperables.put("data_includes_references_t", "I3-01D");
		interoperables.put("metadata_includes_reference_other_data", "I3-02M");
		interoperables.put("data_includes_qualified_re", "I3-02D");
		interoperables.put("metadata_includes_qualifie", "I3-03M");
		interoperables.put("metadata_include_qualified", "I3-04M");
		dictionaries.put("interoperable",interoperables);

		reusables.put("plurality_of_accurate_and", "R1-01M");
		reusables.put("metadata_includes_informat", "R1.1-01M");
		reusables.put("metadata_refers_to_a_stand", "R1.1-02M");
		reusables.put("metadata_refers_to_a_machi", "R1.1-03M");
		reusables.put("metadata_includes_provenan", "R1.2-01M");
		reusables.put("metadata_includes_provenance_cross_community", "R1.2-02M");
		reusables.put("metadata_complies_with_a_c", "R1.3-01M");
		reusables.put("data_complies_with_a_commu", "R1.3-01D");
		reusables.put("metadata_is_expressed_in_c", "R1.3-02M");
		reusables.put("data_is_expressed_in_compl", "R1.3-02D");
		dictionaries.put("reusable",reusables);

		return dictionaries;

	}

	private int maxMapSize(Map<String, Map<String, String>> dict){
		int[] sizes = new int[dict.size()];
		int index=0;
		for ( String key : dict.keySet() ){
			sizes[index]=dict.get(key).size();
			index++;
		}
		return NumberUtils.max(sizes);
	}

	private JRTableModelDataSource[] readRecordCSV(InputStream csvInput) throws IOException {
		
		CSVParser csvp = new CSVParser(new BufferedReader(new InputStreamReader(csvInput)), CSVFormat.DEFAULT);
		
		List<CSVRecord> records = csvp.getRecords();
		
		JRTableModelDataSource[] jrtmRecords = new JRTableModelDataSource[records.size()];
		///Test
		//String[] findables = new String[] {"metadata_is_identified_by", "data_is_identified_by_a_pe", "metadata_is_identified_by_a_gobally_unique_identifier", "data_is_identified_by_a_gl", "rich_metadata_is_provided", "metadata_includes_the_iden", "metadata_is_offered_in_suc"};
		Map<String, Map<String,String>> fair_dictionaries=createFAIRdicts();
		//JRTableModelDataSource[] jrtmRecords = new JRTableModelDataSource[findables.length];

		///Test
		if(records.size() == 0) {
			
			csvp.close();
			
			return jrtmRecords;
		}
		
		CSVRecord header = records.get(0);
		int fair_columns=3*4;
		String[] headerColumns = new String[header.size()+fair_columns];
//		String[] headerColumns = new String[header.size()];

		for(int i=0; i<headerColumns.length-fair_columns; i++) {
//		for(int i=0; i<headerColumns.length; i++) {
			headerColumns[i]=header.get(i);
		}
		///Test
		Map<String, Integer> fair_offsets = new LinkedHashMap<String, Integer>();
		for(int i = 0;i<fair_dictionaries.size();i++){
			String current_key = fair_dictionaries.keySet().toArray()[i].toString();
			fair_offsets.put(current_key , i*3);
			headerColumns[headerColumns.length-fair_columns+i*3]=current_key.toUpperCase()+"_Series";
			headerColumns[headerColumns.length-fair_columns+i*3+1]=current_key.toUpperCase()+"_Value";
			headerColumns[headerColumns.length-fair_columns+i*3+2]=current_key.toUpperCase()+"_Category";
//			logger.info("header_fair: \n"+
//					headerColumns[headerColumns.length-fair_columns+i*3]+"\n"+
//					headerColumns[headerColumns.length-fair_columns+i*3+1]+"\n"+
//					headerColumns[headerColumns.length-fair_columns+i*3+2]+"\n"
//			);
		}
//		fair_offsets.put("findable",0);
//		fair_offsets.put("accessible",3);
//		fair_offsets.put("interoperable",6);
//		fair_offsets.put("reusable",9);


//		The loop is just a loop, but a forEach instructs the library to perform the action on each element, without specifying neither the order of actions (for parallel streams) nor threads which will execute them. If you use forEachOrdered, then there are still no guarantees about threads, but at least you have the guarantee of happens-before relationship between actions on subsequent elements.
//		fair_dictionaries.entrySet().stream().forEachOrdered(x -> fair_offsets.put(x.getKey(), 0));

//		headerColumns[headerColumns.length-fair_columns+fair_offsets.get("findable")]="Findable_Series";
//		headerColumns[headerColumns.length-fair_columns]="Findable_Value";
//		headerColumns[headerColumns.length-fair_columns]="Findable_Category";
//		headerColumns[headerColumns.length-fair_columns]="Accessible_Series";
//		headerColumns[headerColumns.length-fair_columns]="Accessible_Value";
//		headerColumns[headerColumns.length-fair_columns]="Accessible_Category";
//		headerColumns[headerColumns.length-fair_columns]="Interoperable_Series";
//		headerColumns[headerColumns.length-fair_columns]="Interoperable_Value";
//		headerColumns[headerColumns.length-fair_columns]="Interoperable_Category";
//		headerColumns[headerColumns.length-fair_columns]="Reusable_Series";
//		headerColumns[headerColumns.length-fair_columns]="Reusable_Value";
//		headerColumns[headerColumns.length-fair_columns]="Reusable_Category";

		for(int j=1; j<records.size(); j++) {


			Object[][] data = new Object[maxMapSize(fair_dictionaries)][headerColumns.length];
//			findables.length
			CSVRecord csvRecord = records.get(j);

			for(int i=0; i<headerColumns.length-fair_columns; i++) {

				data[0][i]=csvRecord.get(i);
//				//System.out.println(headerColumns[i] + ": " + data[0][i].length());
				logger.info("data: "+headerColumns[i]+ ": "+data[0][i]);
//				for(int test=1;test< data.length;test++){
//					data[test][i]=csvRecord.get(i);
////					data[test][i]="TEST";
//				}
			}
			///Test
//			data[0][headerColumns.length-3]= "Test";//new String[]{"Test","Test","Test"};///
//			data[0][headerColumns.length-2]= new int[]{1, 2, 0};
//			data[0][headerColumns.length-1]= new String[]{findables[0], findables[1], findables[2]};
			//for(int test=0;test< data.length;test++){
//			int data_index=0;
//			LinkedHashMap<String, String> sortedMap = new LinkedHashMap<>();
//			fair_dictionaries.get("findable").entrySet().stream().sorted(Map.Entry.comparingByValue())
//					.forEachOrdered(x -> sortedMap.put(x.getKey(), x.getValue()));
//			for ( String current_findable : fair_dictionaries.get("findable").keySet() ){
//				//data[test]=data[0];
////				String current_findable= key.toString();
//				data[data_index][headerColumns.length-3]= "Findable";///
//				//String current_findable=fair_dictionaries.get("findable").keySet().toArray()[test];
//				//logger.info(fair_dictionaries.get("findable").keySet().toArray()[test]);
//				//String current_findable=fair_dictionaries.get("findable").keySet().toArray()[test].toString();
//				//data[test][headerColumns.length-2]= test%4;//"{1,2,0,3,1,2,0}";
//				int index_of_findable= Arrays.asList(headerColumns).indexOf(current_findable);
//				logger.info(current_findable+": "+fair_dictionaries.get("findable").get(current_findable)+", "+index_of_findable);
//				if(index_of_findable<0){
//					data[data_index][headerColumns.length-2]= 0;//test;//"{1,2,0,3,1,2,0}";
//				}else{
//					data[data_index][headerColumns.length-2]= Integer.parseInt(data[0][index_of_findable].toString().substring(0,1));
////					logger.info("data-value: "+data[test][headerColumns.length-2]);
//				}
//				data[data_index][headerColumns.length-1]= fair_dictionaries.get("findable").get(current_findable);
//				data_index++;
//			}

			for ( String fair_module : fair_dictionaries.keySet() ) {
//				data_index = 0;
				logger.info("fair_module: "+fair_module);
//				for (String current_indicator : fair_dictionaries.get(fair_module).keySet()) {
				for(int data_index=0;data_index<data.length;data_index++){

					int col_offset=headerColumns.length - fair_columns + fair_offsets.get(fair_module);
					data[data_index][col_offset] = fair_module;///
					logger.info("data_index: "+data_index);
					String current_indicator = fair_dictionaries.get(fair_module).keySet().toArray()[data_index % fair_dictionaries.get(fair_module).size()].toString();

					int index_of_findable = Arrays.asList(headerColumns).indexOf(current_indicator);
					logger.info(current_indicator + ": " + fair_dictionaries.get(fair_module).get(current_indicator) + ", " + index_of_findable);
					if (index_of_findable < 0 ) {
						data[data_index][col_offset + 1] = 0;//test;//"{1,2,0,3,1,2,0}";
					} else {
						if(data[0][index_of_findable].equals("")){
							data[data_index][col_offset + 1] = 0;
						}else{
							data[data_index][col_offset + 1] = Integer.parseInt(data[0][index_of_findable].toString().substring(0, 1));
						}
					}
					data[data_index][col_offset + 2] = fair_dictionaries.get(fair_module).get(current_indicator);
					logger.info("fair_header: "+headerColumns[col_offset]+","+headerColumns[col_offset+1]+","+headerColumns[col_offset+2]);
					logger.info("fair_data: "+data[data_index][col_offset]+","+data[data_index][col_offset+1]+","+data[data_index][col_offset+2]);
					//data_index++;
				}

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
