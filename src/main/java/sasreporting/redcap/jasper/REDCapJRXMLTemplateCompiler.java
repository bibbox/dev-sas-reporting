package sasreporting.redcap.jasper;

import java.io.*;
import java.util.*;

import javax.swing.table.DefaultTableModel;

import net.sf.jasperreports.engine.*;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.sf.jasperreports.engine.data.JRTableModelDataSource;
import net.sf.jasperreports.view.JasperViewer;

import net.sf.jasperreports.engine.design.JRDesignField;


public class REDCapJRXMLTemplateCompiler {

	
	private static final Logger logger = LogManager.getLogger(REDCapJRXMLTemplateCompiler.class);
	
	public final static String FIELD_JRXML_TEMPLATE = "jrxml";
	public final static int PAGE_WIDTH_A4 = 595;
	public final static int PAGE_HEIGHT_A4 = 842;	
	public final static int PAGE_MARGIN = 25;
	

	private Map<String, Map<String, String>> createFAIRdicts(){
		//TODO load from a config file
		Map<String, Map<String, String>> dictionaries = new LinkedHashMap<String, Map<String, String>>();
		Map<String, String> findables = new LinkedHashMap<String, String>();
		Map<String, String> accessibles = new LinkedHashMap<String, String>();
		Map<String, String> interoperables = new LinkedHashMap<String, String>();
		Map<String, String> reusables = new LinkedHashMap<String, String>();

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

		//Load Fair fields and indicators
		Map<String, Map<String,String>> fair_dictionaries=createFAIRdicts();

		if(records.size() == 0) {
			
			csvp.close();
			
			return jrtmRecords;
		}
		
		CSVRecord header = records.get(0);
		//3 Fields for Spider Chart: Series, Category and Value
		int fair_fields_per_module=3;
		int fair_columns=fair_fields_per_module*fair_dictionaries.size();
		String[] headerColumns = new String[header.size()+fair_columns];
//		String[] headerColumns = new String[header.size()];

		for(int i=0; i<headerColumns.length-fair_columns; i++) {
			headerColumns[i]=header.get(i);
		}

		// Create header names (FINDABLE, ACCESSABLE, INTEROPERABLE, REUSABLE) with _Series, _Value and _Category
		// Has to have the same name in the jrxml template
		Map<String, Integer> fair_offsets = new LinkedHashMap<String, Integer>();
		for(int i = 0;i<fair_dictionaries.size();i++){
			String current_key = fair_dictionaries.keySet().toArray()[i].toString();
			fair_offsets.put(current_key , i*fair_fields_per_module);
			headerColumns[headerColumns.length-fair_columns+i*fair_fields_per_module]=current_key.toUpperCase()+"_Series";
			headerColumns[headerColumns.length-fair_columns+i*fair_fields_per_module+1]=current_key.toUpperCase()+"_Value";
			headerColumns[headerColumns.length-fair_columns+i*fair_fields_per_module+2]=current_key.toUpperCase()+"_Category";
		}

		for(int j=1; j<records.size(); j++) {

			// Expression for Value in SpiderChart allows to convert string to int, but then only one value gets used.
			// Therefore, are teh values converted here to int and data has to hold int and String.
			Object[][] data = new Object[maxMapSize(fair_dictionaries)][headerColumns.length];
			CSVRecord csvRecord = records.get(j);
			//Fill data
			for(int i=0; i<headerColumns.length-fair_columns; i++) {

				data[0][i]=csvRecord.get(i);
//				//System.out.println(headerColumns[i] + ": " + data[0][i].length());
			}

			// Fill extra columns with aggregated FAIR data for Spider Chart
			for ( String fair_module : fair_dictionaries.keySet() ) {
//				logger.info("fair_module: "+fair_module);

				for(int data_index=0;data_index<data.length;data_index++){

					int col_offset=headerColumns.length - fair_columns + fair_offsets.get(fair_module);
					data[data_index][col_offset] = fair_module;///
					// SpiderChart does not allow null in Category series name. Therefore loop back to beginning. (Data gets grouped anywhy)
					String current_indicator = fair_dictionaries.get(fair_module).keySet().toArray()[data_index % fair_dictionaries.get(fair_module).size()].toString();

					//Get index for the current indicator
					int index_of_indicator = Arrays.asList(headerColumns).indexOf(current_indicator);
					// not found -> -1
					if (index_of_indicator < 0 ) {
						data[data_index][col_offset + 1] = 0;
					} else {
						// Field is empty if nothing was selected --> This is interpreted as 0 for this indicator
						if(data[0][index_of_indicator].equals("")){
							data[data_index][col_offset + 1] = 0;
						}else{
							data[data_index][col_offset + 1] = Integer.parseInt(data[0][index_of_indicator].toString().substring(0, 1));
						}
					}
					data[data_index][col_offset + 2] = fair_dictionaries.get(fair_module).get(current_indicator);
				}

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

			//Add properties to use in file name
			records[0].moveFirst();
			JRDesignField cohort_field = new JRDesignField();
			cohort_field.setName("cohort_id");
			JRDesignField timestamp_field = new JRDesignField();
			timestamp_field.setName("selfassessmenttool_timestamp");

			String cohort_id=null;
			String date=null;
			while (cohort_id == null && date==null && records[0].next()){
				if(cohort_id == null){
					cohort_id=records[0].getFieldValue( cohort_field).toString();
				}
				if(date==null){
					date = records[0].getFieldValue( timestamp_field).toString().substring(0,10); //Only select date
				}
			}
			firstPrint.setProperty("cohort_id",cohort_id);
			firstPrint.setProperty("date",date);


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
