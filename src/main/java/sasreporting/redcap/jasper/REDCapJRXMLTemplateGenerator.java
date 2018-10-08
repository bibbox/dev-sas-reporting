package sasreporting.redcap.jasper;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.design.JRDesignBand;
import net.sf.jasperreports.engine.design.JRDesignExpression;
import net.sf.jasperreports.engine.design.JRDesignField;
import net.sf.jasperreports.engine.design.JRDesignFrame;
import net.sf.jasperreports.engine.design.JRDesignImage;
import net.sf.jasperreports.engine.design.JRDesignLine;
import net.sf.jasperreports.engine.design.JRDesignSection;
import net.sf.jasperreports.engine.design.JRDesignStaticText;
import net.sf.jasperreports.engine.design.JRDesignStyle;
import net.sf.jasperreports.engine.design.JRDesignTextField;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.type.EvaluationTimeEnum;
import net.sf.jasperreports.engine.type.HorizontalTextAlignEnum;
import net.sf.jasperreports.engine.type.PositionTypeEnum;
import net.sf.jasperreports.engine.type.SectionTypeEnum;
import net.sf.jasperreports.engine.type.SplitTypeEnum;
import net.sf.jasperreports.engine.type.VerticalImageAlignEnum;
import net.sf.jasperreports.engine.type.VerticalTextAlignEnum;
import net.sf.jasperreports.engine.xml.JRXmlWriter;


public class REDCapJRXMLTemplateGenerator {

	private static final Logger logger = LogManager.getLogger(REDCapJRXMLTemplateGenerator.class);
	
	public final static int PAGE_WIDTH_A4 = 595;	
	public final static int PAGE_HEIGHT_A4 = 842;	
	public final static int PAGE_MARGIN = 25;
	public final static int ELEMENT_HEIGHT = 40;
	public final static int FIELDS_VERTICAL_SPACING = 10;
	public final static int DESCRIPTION_VERTICAL_SPACING = 30;
	public final static int HEADER_VERTICAL_SPACING = 35;
	public final static int SECTION_HEADER_HEIGHT = 50;
	public final static int BANNER_HEIGHT = 113;
	public final static int FOOTER_HEIGHT = 113;
	public final static int BANNER_SPACE = 30;
	public final static int FOOTER_SPACE = 30;
	public final static int RADIO_BUTTON_SIZE = 10;
	public final static int CHECKBOX_SIZE = 12;
	public final static int TEXT_BUTTON_SPACING = 3;
	
	private final static String DEFAULT_FONT_BOLD = "net/sf/jasperreports/fonts/dejavu/DejaVuSerif-Bold.ttf";
	private final static String DEFAULT_FONT_ITALIC = "net/sf/jasperreports/fonts/dejavu/DejaVuSerif-Italic.ttf";
	private final static String IMAGE_CHECKED = "checked.png";
	private final static String IMAGE_UNCHECKED = "unchecked.png";
	private final static String CHECKBOX_CHECKED = "checkbox_checked.png";
	private final static String CHECKBOX_UNCHECKED = "checkbox_unchecked.png";
	private final static String FIELD_NAME = "field_name";
	private final static String FIELD_LABEL = "field_label";
	private final static String FIELD_SELECT_CHOICES_OR_CALCULATIONS = "select_choices_or_calculations";
	private final static String FIELD_TYPE = "field_type";
	private final static String SECTION_HEADER = "section_header";
	private final static String FIELD_TYPE_DESCRIPTIVE = "descriptive";
	private final static String FIELD_TYPE_TEXT = "text";
	private final static String FIELD_TYPE_DROPDOWN = "dropdown";
	private final static String FIELD_TYPE_CHECKBOX = "checkbox";
	private final static String FIELD_TYPE_RADIO = "radio";
	private final static String FIELD_TYPE_YESNO = "yesno";
	private final static String FIELD_VAL_YES = "Yes";
	private final static String FIELD_VAL_NO = "No";
	private final static String FIELDNAME_RECORD_ID = "record_id";
	private final static float FONT_SIZE_SECTION_HEADER = 14f;
	private final static float FONT_SIZE_TEXT_FIELDS = 12f;
	private final static float FONT_SIZE_FOOTER_FIELDS = 8f;
	private final static String FIELD_MARKUP_HTML = "html";
	
	private Reader projectInfo;
	private Reader metadata;
	
	
	public REDCapJRXMLTemplateGenerator(InputStream projectInfo, InputStream metadata) {
		
		this.projectInfo=new InputStreamReader(projectInfo);
		this.metadata=new InputStreamReader(metadata);
	}
	
	
	public JasperReport generateREDCapRecordJRXMLTemplateWithBandPerElement() throws FileNotFoundException, IOException, JRException {
		
		CSVParser csvpProjectInfo = new CSVParser(projectInfo, CSVFormat.DEFAULT.withHeader());
		CSVParser csvpMetadata = new CSVParser(metadata, CSVFormat.DEFAULT.withHeader());
		
		List<CSVRecord> projectInfoRecords = csvpProjectInfo.getRecords();
		
		Map<String, Integer> headerMap = csvpMetadata.getHeaderMap();
		List<CSVRecord> metaRecords = csvpMetadata.getRecords();
			
		csvpProjectInfo.close();
		csvpMetadata.close();
		
		String projectTitle = projectInfoRecords.get(0).get("project_title");
		String creationTime = projectInfoRecords.get(0).get("creation_time");
		
		JasperDesign jd = new JasperDesign();
		jd.setName(projectTitle);
		jd.setPageWidth(PAGE_WIDTH_A4);
		jd.setPageHeight(PAGE_HEIGHT_A4);
		jd.setTopMargin(0);
		jd.setBottomMargin(0);
		jd.setLeftMargin(0);
		jd.setRightMargin(0);
	
		
		JRDesignBand jdbHeader = new JRDesignBand();		
		jdbHeader.setHeight(BANNER_HEIGHT+BANNER_SPACE);
		
		JRDesignImage jrdiBanner = new JRDesignImage(new JRDesignStyle().getDefaultStyleProvider());
		jrdiBanner.setWidth(PAGE_WIDTH_A4);
		jrdiBanner.setHeight(BANNER_HEIGHT);
		JRDesignExpression jrdiexBanner = new JRDesignExpression();
		jrdiexBanner.setText("\""+""+"banner.jpg"+"\"");
		jrdiBanner.setExpression(jrdiexBanner);
		jdbHeader.addElement(jrdiBanner);
		
		JRDesignStaticText jrdsth = new JRDesignStaticText();
		
		jrdsth.setMarkup(FIELD_MARKUP_HTML);
		
		jrdsth.setPdfEmbedded(true);
		jrdsth.setPositionType(PositionTypeEnum.FLOAT);
		jrdsth.setFontSize(FONT_SIZE_SECTION_HEADER);
		jrdsth.setText("<font size='4' color='#004280'><b>REPORT</b></font>"
				+ "<br><font size='2' color='#004280'>SELF-ASSESSMENT SURVEY"
				+ "<br><font size='1' color='#004280'>"+projectTitle+"</font>");
		jrdsth.setVerticalTextAlign(VerticalTextAlignEnum.MIDDLE);
		jrdsth.setWidth(jd.getPageWidth()-PAGE_MARGIN*2);
		jrdsth.setHorizontalTextAlign(HorizontalTextAlignEnum.CENTER);
		jrdsth.setHeight(BANNER_HEIGHT-FIELDS_VERTICAL_SPACING);
		jrdsth.setY(0);
		
		jdbHeader.addElement(jrdsth);
		
		List<JRDesignBand> jrdBands = new ArrayList<JRDesignBand>();
		
		for(CSVRecord metaRecord: metaRecords) {
			
			JRDesignBand jdbDetail = new JRDesignBand(); 
			String fieldName = metaRecord.get(headerMap.get(FIELD_NAME));
			String fieldLabel = metaRecord.get(headerMap.get(FIELD_LABEL));
			String fieldType = metaRecord.get(headerMap.get(FIELD_TYPE));
			String sectionHeader = metaRecord.get(headerMap.get(SECTION_HEADER));
			String fieldSelectChoices = metaRecord.get(headerMap.get(FIELD_SELECT_CHOICES_OR_CALCULATIONS));
			String[] fieldSelectChoicesElements = fieldSelectChoices.split(Pattern.quote("|"));
			
			boolean hasSectionHeader = sectionHeader.trim().length() > 0;
			
			if(hasSectionHeader) {
				
				JRDesignStaticText jrdst = new JRDesignStaticText();
				
				jrdst.setMarkup(FIELD_MARKUP_HTML);
				jrdst.setPdfFontName(DEFAULT_FONT_BOLD);
				jrdst.setPdfEmbedded(true);
				jrdst.setPositionType(PositionTypeEnum.FLOAT);
				jrdst.setFontSize(FONT_SIZE_SECTION_HEADER);
				jrdst.setText(sectionHeader.replace("\n", "<br>"));
				jrdst.setVerticalTextAlign(VerticalTextAlignEnum.MIDDLE);
				jrdst.setWidth(jd.getPageWidth()-PAGE_MARGIN * 2);
				int tfHeight = calculateTextFieldHeight(sectionHeader, jrdst.getWidth(),"DejaVuSerif", (int)FONT_SIZE_SECTION_HEADER)+HEADER_VERTICAL_SPACING;
				jrdst.setHeight(tfHeight);
				jrdst.setX(PAGE_MARGIN);
				jrdst.setY(0);
				jrdst.setBold(true);
				jdbDetail.setHeight(tfHeight);
				jdbDetail.addElement(jrdst);
				jrdBands.add(jdbDetail);
				
				if("Confidentiality Note".equals(sectionHeader.trim())) {
							
					JRDesignLine jrdl = new JRDesignLine();
					jrdl.setWidth(jd.getPageWidth()-PAGE_MARGIN * 2);
					jrdl.setHeight(1);
					jrdl.setX(PAGE_MARGIN);	
					jrdl.setY(9);
					jdbDetail.setHeight((tfHeight > SECTION_HEADER_HEIGHT+10) ? tfHeight : SECTION_HEADER_HEIGHT+10);
					jdbDetail.addElement(jrdl);
				}
				jdbDetail = new JRDesignBand(); 
			}
			
			if(fieldType.equals(FIELD_TYPE_DESCRIPTIVE)) {
				
				JRDesignStaticText jrdst = new JRDesignStaticText();
				
				jrdst.setHorizontalTextAlign(HorizontalTextAlignEnum.JUSTIFIED);
				
				jrdst.setPdfFontName(DEFAULT_FONT_ITALIC);
				jrdst.setMarkup(FIELD_MARKUP_HTML);
				jrdst.setFontSize(FONT_SIZE_TEXT_FIELDS);
				jrdst.setPdfEmbedded(true);
				jrdst.setPositionType(PositionTypeEnum.FLOAT);
				jrdst.setText(fieldLabel.replace("\n", "<br>"));
				
				jrdst.setWidth(jd.getPageWidth() - PAGE_MARGIN * 2);
				int tfHeight = calculateTextFieldHeight(fieldLabel, jrdst.getWidth(),"DejaVuSerif", (int)FONT_SIZE_TEXT_FIELDS)+DESCRIPTION_VERTICAL_SPACING;
				jrdst.setHeight(tfHeight);
				jrdst.setVerticalTextAlign(VerticalTextAlignEnum.MIDDLE);
				jrdst.setX(PAGE_MARGIN);
				jrdst.setY(0);
				jdbDetail.setHeight(tfHeight);
				jdbDetail.addElement(jrdst);
			}
			
			/*
			 * FIELD_TYPE_TEXT
			 */
			
			if(FIELD_TYPE_TEXT.equals(fieldType) || FIELD_TYPE_DROPDOWN.equals(fieldType) || FIELD_TYPE_YESNO.equals(fieldType) || FIELD_TYPE_RADIO.equals(fieldType) || FIELD_TYPE_CHECKBOX.equals(fieldType)) {
				
				
				if(FIELD_TYPE_CHECKBOX.equals(fieldType)) {
					
					for(int i=0; i<fieldSelectChoicesElements.length; i++) {
						
						String fieldSelectChoicesIndex = 
								
								fieldSelectChoicesElements[i].substring(0, fieldSelectChoicesElements[i].indexOf(",")).trim();
						
						JRDesignField jdf = new JRDesignField();
						jdf.setName(fieldName+"___" + fieldSelectChoicesIndex);
						jd.addField(jdf);
					}
				}
				
				else {
					
					JRDesignField jdf = new JRDesignField();
					jdf.setName(fieldName);
					jd.addField(jdf);
				}
				
				JRDesignStaticText jrdst = new JRDesignStaticText();
				jrdst.setMarkup(FIELD_MARKUP_HTML);
				jrdst.setText(fieldLabel.replace("\n", "<br>"));
				jrdst.setVerticalTextAlign(VerticalTextAlignEnum.TOP);
				jrdst.setFontSize(FONT_SIZE_TEXT_FIELDS);
				jrdst.setWidth((jd.getPageWidth() - PAGE_MARGIN * 2) / 2);
				int tfHeight = calculateTextFieldHeight(fieldLabel, jrdst.getWidth(),"DejaVuSerif",(int)FONT_SIZE_TEXT_FIELDS)+FIELDS_VERTICAL_SPACING;
				jrdst.setHeight(tfHeight);
				jrdst.setX(PAGE_MARGIN);
				jrdst.setY(0);
				jdbDetail.addElement(jrdst);
				
				int xr = jrdst.getWidth() + PAGE_MARGIN * 2;
				
				/*
				 * FIELD_TYPE_TEXT or FIELD_TYPE_DROPDOWN
				 */
				
				if(FIELD_TYPE_TEXT.equals(fieldType) || FIELD_TYPE_DROPDOWN.equals(fieldType)) {
					
					JRDesignTextField jrdtf = new JRDesignTextField();
					jrdtf.setMarkup(FIELD_MARKUP_HTML);
					jrdtf.setFontSize(FONT_SIZE_TEXT_FIELDS);
					jrdtf.setVerticalTextAlign(VerticalTextAlignEnum.MIDDLE);
					jrdtf.setWidth(jrdst.getWidth() - PAGE_MARGIN * 2);
					jrdtf.setHeight(jrdst.getHeight());
					jrdtf.setY(0);
					jrdtf.setX(xr);
					JRDesignExpression jrdex = new JRDesignExpression();
					
					jrdex.setText("$F{"+fieldName+"}+" + "\""+ "<br>" +"\"");
					jrdtf.setStretchWithOverflow(true);
					jrdtf.setExpression(jrdex);
					jdbDetail.addElement(jrdtf);
					
					if(FIELDNAME_RECORD_ID.equals(fieldName)) {
						
						jrdst.setPdfFontName(DEFAULT_FONT_BOLD);
						jrdst.setPdfEmbedded(true);
						jrdtf.setHorizontalTextAlign(HorizontalTextAlignEnum.RIGHT);
						jrdtf.setPdfEmbedded(true);
						jrdtf.setPdfFontName(DEFAULT_FONT_BOLD);
					}	
				}
				
				/*
				 * FIELD_TYPE_YESNO
				 */
				
				if(FIELD_TYPE_YESNO.equals(fieldType)) {
					
					JRDesignStaticText jrdstYes = new JRDesignStaticText();
					jrdstYes.setMarkup(FIELD_MARKUP_HTML);
					jrdstYes.setText(FIELD_VAL_YES);
					jrdstYes.setFontSize(FONT_SIZE_TEXT_FIELDS);
					jrdstYes.setVerticalTextAlign(VerticalTextAlignEnum.TOP);
					jrdstYes.setWidth(calculateTextFieldWidth(FIELD_VAL_YES, "DejaVuSerif",(int)FONT_SIZE_TEXT_FIELDS));
					jrdstYes.setHeight(calculateTextFieldHeight(FIELD_VAL_YES, jrdstYes.getWidth(), "DejaVuSerif",(int)FONT_SIZE_TEXT_FIELDS));
					jrdstYes.setX(xr+RADIO_BUTTON_SIZE+TEXT_BUTTON_SPACING);
					jrdstYes.setY(0);
					jdbDetail.addElement(jrdstYes);
					
					JRDesignExpression jrdiexChecked = new JRDesignExpression();
					jrdiexChecked.setText("\""+""+IMAGE_CHECKED+"\"");
					JRDesignExpression jrdiexPrintWhenYes = new JRDesignExpression();
					jrdiexPrintWhenYes.setText("$F{"+fieldName+"}.trim().equalsIgnoreCase(\""+FIELD_VAL_YES+"\")");
					
					JRDesignExpression jrdiexUnchecked = new JRDesignExpression();
					jrdiexUnchecked.setText("\""+""+IMAGE_UNCHECKED+"\"");
					JRDesignExpression jrdiexPrintWhenNo = new JRDesignExpression();
					jrdiexPrintWhenNo.setText("$F{"+fieldName+"}.trim().equalsIgnoreCase(\""+FIELD_VAL_NO+"\")");	
					
					JRDesignImage jrdiYesUnchecked = new JRDesignImage(new JRDesignStyle().getDefaultStyleProvider());
					jrdiYesUnchecked.setWidth(RADIO_BUTTON_SIZE);
					jrdiYesUnchecked.setHeight(RADIO_BUTTON_SIZE);
					jrdiYesUnchecked.setY(0);
					jrdiYesUnchecked.setVerticalImageAlign(VerticalImageAlignEnum.TOP);
					jrdiYesUnchecked.setX(xr);
					jrdiYesUnchecked.setExpression(jrdiexUnchecked);
			        
			        JRDesignImage jrdiYesChecked = (JRDesignImage)jrdiYesUnchecked.clone();
			        jrdiYesChecked.setPrintWhenExpression(jrdiexPrintWhenYes);
			        jrdiYesChecked.setExpression(jrdiexChecked);
			        jdbDetail.addElement(jrdiYesUnchecked);
			        jdbDetail.addElement(jrdiYesChecked);
					
					JRDesignStaticText jrdstNo = new JRDesignStaticText();
					jrdstNo.setMarkup(FIELD_MARKUP_HTML);
					jrdstNo.setText(FIELD_VAL_NO);
					jrdstNo.setFontSize(FONT_SIZE_TEXT_FIELDS);
					jrdstNo.setVerticalTextAlign(VerticalTextAlignEnum.TOP);
					jrdstNo.setWidth(calculateTextFieldWidth(FIELD_VAL_NO, "DejaVuSerif",(int)FONT_SIZE_TEXT_FIELDS));
					jrdstNo.setHeight(calculateTextFieldHeight(FIELD_VAL_NO, jrdstNo.getWidth(), "DejaVuSerif",(int)FONT_SIZE_TEXT_FIELDS));
					jrdstNo.setX(xr+RADIO_BUTTON_SIZE+TEXT_BUTTON_SPACING);
					jrdstNo.setY(jrdstYes.getHeight()+1);
					jdbDetail.addElement(jrdstNo);
					
					JRDesignImage jrdiNoUnchecked = new JRDesignImage(new JRDesignStyle().getDefaultStyleProvider());
					jrdiNoUnchecked.setVerticalImageAlign(VerticalImageAlignEnum.TOP);
					jrdiNoUnchecked.setExpression(jrdiexUnchecked);
					jrdiNoUnchecked.setWidth(RADIO_BUTTON_SIZE);
					jrdiNoUnchecked.setHeight(RADIO_BUTTON_SIZE);
					jrdiNoUnchecked.setY(jrdstNo.getHeight()+1);
					jrdiNoUnchecked.setX(xr);
					tfHeight= (tfHeight > jrdstYes.getHeight() * 2) ? tfHeight: jrdstYes.getHeight() * 2 + 1;
					
					JRDesignImage jrdiNoChecked = (JRDesignImage)jrdiNoUnchecked.clone();
					jrdiNoChecked.setPrintWhenExpression(jrdiexPrintWhenNo);
					jrdiNoChecked.setExpression(jrdiexChecked);
			        jdbDetail.addElement(jrdiNoUnchecked);
					jdbDetail.addElement(jrdiNoChecked);
				}
				
				/*
				 * FIELD_TYPE_CHECKBOX or FIELD_TYPE_RADIO
				 */
				
				if(FIELD_TYPE_CHECKBOX.equals(fieldType) || FIELD_TYPE_RADIO.equals(fieldType)) {
					
					String[] fieldSelectChoicesIndizes = new String[fieldSelectChoicesElements.length];
					
					for(int i=0; i<fieldSelectChoicesElements.length; i++) {
						
						fieldSelectChoicesIndizes[i] = 
								
							fieldSelectChoicesElements[i].substring(0, fieldSelectChoicesElements[i].indexOf(",")).trim();
						
						fieldSelectChoicesElements[i] = 
								
							fieldSelectChoicesElements[i].substring(fieldSelectChoicesElements[i].indexOf(",") + 1, fieldSelectChoicesElements[i].length()).trim();
					}
				
					if(FIELD_TYPE_CHECKBOX.equals(fieldType)) {
					
						int maxLineWidth = jrdst.getWidth() - PAGE_MARGIN * 2;
						int sumWidth = 0; 
						int bandHeight = 0;
						int y = 0;
						int prevElementHeight = 0;
					
						for(int i=0; i<fieldSelectChoicesElements.length; i++) {
						
							int fieldSelectChoicesElementWidth = Math.max(calculateTextFieldWidth(fieldSelectChoicesElements[i], "DejaVuSerif",(int)FONT_SIZE_TEXT_FIELDS), jrdst.getWidth() - PAGE_MARGIN * 2);
							int height = (1 + (int)((fieldSelectChoicesElementWidth / maxLineWidth))) * CHECKBOX_SIZE;
						
							bandHeight = bandHeight + height;
						
							fieldSelectChoicesElementWidth = 
								(fieldSelectChoicesElementWidth > (maxLineWidth - CHECKBOX_SIZE)) ? (maxLineWidth - CHECKBOX_SIZE) : fieldSelectChoicesElementWidth;
								
							if(height > CHECKBOX_SIZE || (sumWidth + (fieldSelectChoicesElementWidth + CHECKBOX_SIZE)) > maxLineWidth) {
							
								sumWidth = 0;
								y = (prevElementHeight == CHECKBOX_SIZE) ? y + prevElementHeight : y; 
							}
						
							JRDesignStaticText jrdstFieldSelectChoice = new JRDesignStaticText();
							jrdstFieldSelectChoice.setMarkup(FIELD_MARKUP_HTML);
							jrdstFieldSelectChoice.setFontSize(FONT_SIZE_TEXT_FIELDS);
							jrdstFieldSelectChoice.setText(fieldSelectChoicesElements[i]);
							jrdstFieldSelectChoice.setVerticalTextAlign(VerticalTextAlignEnum.TOP);
							jrdst.setVerticalTextAlign(VerticalTextAlignEnum.TOP);
							jrdstFieldSelectChoice.setWidth(fieldSelectChoicesElementWidth);
							jrdstFieldSelectChoice.setX(xr+CHECKBOX_SIZE+TEXT_BUTTON_SPACING);
							jrdstFieldSelectChoice.setHeight(height);
						
							jrdstFieldSelectChoice.setY(y);
						
							sumWidth = sumWidth + fieldSelectChoicesElementWidth;
						
							JRDesignImage jrdiUnchecked = new JRDesignImage(new JRDesignStyle().getDefaultStyleProvider());
							JRDesignExpression jrdiexUnchecked = new JRDesignExpression();
						
							jrdiexUnchecked.setText("\""+""+CHECKBOX_UNCHECKED+"\"");
							jrdiUnchecked.setExpression(jrdiexUnchecked);
							jrdiUnchecked.setWidth(CHECKBOX_SIZE);
							jrdiUnchecked.setHeight(height);
							jrdiUnchecked.setX(xr);
							jrdiUnchecked.setY(y);
							jrdiUnchecked.setVerticalImageAlign(VerticalImageAlignEnum.TOP);
						
							JRDesignImage jrdiChecked = new JRDesignImage(new JRDesignStyle().getDefaultStyleProvider());
							JRDesignExpression jrdiexChecked = new JRDesignExpression();
							JRDesignExpression jrdiexPrintWhenChecked = new JRDesignExpression();
						
							jrdiexPrintWhenChecked.setText("$F{"+fieldName+"___"+fieldSelectChoicesIndizes[i]+"}.trim().equalsIgnoreCase(\""+fieldSelectChoicesElements[i]+"\")");
							jrdiChecked.setPrintWhenExpression(jrdiexPrintWhenChecked);
							jrdiexChecked.setText("\""+""+CHECKBOX_CHECKED+"\"");
							jrdiChecked.setExpression(jrdiexChecked);
							jrdiChecked.setWidth(CHECKBOX_SIZE);
							jrdiChecked.setHeight(height);
							jrdiChecked.setX(xr);
							jrdiChecked.setY(y);
							jrdiChecked.setVerticalImageAlign(VerticalImageAlignEnum.TOP);
						
							jdbDetail.addElement(jrdstFieldSelectChoice);
							jdbDetail.addElement(jrdiUnchecked);
							jdbDetail.addElement(jrdiChecked);
						
							sumWidth = sumWidth + CHECKBOX_SIZE;
						
							y = y + height;
						
							prevElementHeight = height; 
						}
						tfHeight = (tfHeight < bandHeight) ? bandHeight : tfHeight;
						jrdst.setHeight(tfHeight);
					}
				
					if(FIELD_TYPE_RADIO.equals(fieldType)) {
					
						int maxLineWidth = jrdst.getWidth() - PAGE_MARGIN * 2- RADIO_BUTTON_SIZE - TEXT_BUTTON_SPACING;
						int bandHeight = 0;
						int y = 0;
					
						for(String fieldchoiceElement : fieldSelectChoicesElements) {
							
							int fieldSelectChoicesElementWidth = Math.min(calculateTextFieldWidth(fieldchoiceElement, "DejaVuSerif", (int)FONT_SIZE_TEXT_FIELDS), maxLineWidth);
							int height = calculateTextFieldHeight(fieldchoiceElement, maxLineWidth, "DejaVuSerif", (int)FONT_SIZE_TEXT_FIELDS); 
						
							bandHeight = bandHeight + height;
								
							JRDesignStaticText jrdstFieldSelectChoice = new JRDesignStaticText();
							jrdstFieldSelectChoice.setMarkup(FIELD_MARKUP_HTML);
							jrdstFieldSelectChoice.setFontSize(FONT_SIZE_TEXT_FIELDS);
							jrdstFieldSelectChoice.setText(fieldchoiceElement);
							jrdstFieldSelectChoice.setVerticalTextAlign(VerticalTextAlignEnum.TOP);
							jrdst.setVerticalTextAlign(VerticalTextAlignEnum.TOP);
							jrdstFieldSelectChoice.setWidth(fieldSelectChoicesElementWidth);
							jrdstFieldSelectChoice.setX(xr+RADIO_BUTTON_SIZE+TEXT_BUTTON_SPACING);
							jrdstFieldSelectChoice.setHeight(height);
							jrdstFieldSelectChoice.setY(y);
						
							JRDesignImage jrdiUnchecked = new JRDesignImage(new JRDesignStyle().getDefaultStyleProvider());
							JRDesignExpression jrdiexUnchecked = new JRDesignExpression();
						
							jrdiexUnchecked.setText("\""+""+IMAGE_UNCHECKED+"\"");
							jrdiUnchecked.setExpression(jrdiexUnchecked);
							jrdiUnchecked.setWidth(RADIO_BUTTON_SIZE);
							jrdiUnchecked.setHeight(Math.max(height, RADIO_BUTTON_SIZE));
							jrdiUnchecked.setX(xr);
							jrdiUnchecked.setY(y);
							jrdiUnchecked.setVerticalImageAlign(VerticalImageAlignEnum.TOP);
						
							JRDesignImage jrdiChecked = new JRDesignImage(new JRDesignStyle().getDefaultStyleProvider());
							JRDesignExpression jrdiexChecked = new JRDesignExpression();
							JRDesignExpression jrdiexPrintWhenChecked = new JRDesignExpression();
						
							jrdiexPrintWhenChecked.setText("$F{"+fieldName+"}.trim().equalsIgnoreCase(\""+fieldchoiceElement+"\")");
							jrdiChecked.setPrintWhenExpression(jrdiexPrintWhenChecked);
							jrdiexChecked.setText("\""+""+IMAGE_CHECKED+"\"");
							jrdiChecked.setExpression(jrdiexChecked);
							jrdiChecked.setWidth(RADIO_BUTTON_SIZE);
							jrdiChecked.setHeight(Math.max(height, RADIO_BUTTON_SIZE));
							jrdiChecked.setX(xr);
							jrdiChecked.setY(y);
							jrdiChecked.setVerticalImageAlign(VerticalImageAlignEnum.TOP);
						
							jdbDetail.addElement(jrdstFieldSelectChoice);
							jdbDetail.addElement(jrdiUnchecked);
							jdbDetail.addElement(jrdiChecked);
						
							y = y + height;
						}
						tfHeight = (tfHeight < bandHeight) ? bandHeight + FIELDS_VERTICAL_SPACING : tfHeight + FIELDS_VERTICAL_SPACING;
						jrdst.setHeight(tfHeight);
					}
				}
				jdbDetail.setHeight(tfHeight);
				jdbDetail.setSplitType(SplitTypeEnum.PREVENT);
			}
			jrdBands.add(jdbDetail);
		}
		
		/*
		 * Create page footer
		 */
		
		JRDesignBand jdbFooter = new JRDesignBand();		
		jdbFooter.setHeight(FOOTER_HEIGHT+FOOTER_SPACE);
		
		JRDesignImage jrdiFooterBanner = new JRDesignImage(new JRDesignStyle().getDefaultStyleProvider());
		jrdiFooterBanner.setVerticalImageAlign(VerticalImageAlignEnum.BOTTOM);
		jrdiFooterBanner.setWidth(PAGE_WIDTH_A4);
		jrdiFooterBanner.setHeight(FOOTER_HEIGHT+FOOTER_SPACE);
		JRDesignExpression jrdiexFooterBanner = new JRDesignExpression();
		jrdiexFooterBanner.setText("\""+""+"footer.jpg"+"\"");
		jrdiFooterBanner.setExpression(jrdiexFooterBanner);
		jdbFooter.addElement(jrdiFooterBanner);
		
		JRDesignStaticText jrdstfLeft = new JRDesignStaticText();
		
		jrdstfLeft.setMarkup(FIELD_MARKUP_HTML);
		jrdstfLeft.setPdfEmbedded(true);
		jrdstfLeft.setPositionType(PositionTypeEnum.FLOAT);
		jrdstfLeft.setFontSize(FONT_SIZE_FOOTER_FIELDS);
		jrdstfLeft.setForecolor(Color.decode("#004280"));
		String documentTitle = "BBMRI-ERIC SAS for CEN/TS 16835-1:2015";
		String versionNumber = "v02, 2018-06-29";
		String createdBy = "Sabrina Neururer, BBMRI.at";
		String approvedBy = "Andrea Wutte, BBMRI-ERIC";	
		String footerText = "&emsp;Document title: "+documentTitle
				+"<br>&emsp;Version number: "+ versionNumber 
				+"<br>&emsp;Created by: "+ createdBy 
				+"<br>&emsp;Approved by: "+ approvedBy
				+"";
		jrdstfLeft.setText(footerText);
		jrdstfLeft.setVerticalTextAlign(VerticalTextAlignEnum.TOP);
	
		JRDesignFrame dateFrame = new JRDesignFrame();
		
		JRDesignTextField jrdtfRecordID = new JRDesignTextField();
		jrdtfRecordID.setMarkup(FIELD_MARKUP_HTML);
		jrdtfRecordID.setFontSize(8.0f);
		jrdtfRecordID.setForecolor(Color.decode("#004280"));
		jrdtfRecordID.setVerticalTextAlign(VerticalTextAlignEnum.TOP);
		
		jrdtfRecordID.setVerticalTextAlign(VerticalTextAlignEnum.BOTTOM);
		jrdtfRecordID.setHorizontalTextAlign(HorizontalTextAlignEnum.RIGHT);	
		jrdtfRecordID.setEvaluationTime(EvaluationTimeEnum.NOW);
		JRDesignExpression jrdexRecordID = new JRDesignExpression();
		jrdexRecordID.setText("\"Record ID: \"+$F{"+FIELDNAME_RECORD_ID+"}");
		jrdtfRecordID.setStretchWithOverflow(true);
		jrdtfRecordID.setExpression(jrdexRecordID);
		
		JRDesignTextField jrdtfPageDate = new JRDesignTextField();
		jrdtfPageDate.setMarkup(FIELD_MARKUP_HTML);
		jrdtfPageDate.setFontSize(8.0f);
		jrdtfPageDate.setForecolor(Color.decode("#004280"));
		jrdtfPageDate.setVerticalTextAlign(VerticalTextAlignEnum.TOP);
		
		jrdtfPageDate.setVerticalTextAlign(VerticalTextAlignEnum.BOTTOM);
		jrdtfPageDate.setHorizontalTextAlign(HorizontalTextAlignEnum.RIGHT);	
		jrdtfPageDate.setEvaluationTime(EvaluationTimeEnum.NOW);
		JRDesignExpression jrdexPageDate = new JRDesignExpression();
		jrdexPageDate.setText("new SimpleDateFormat(\"dd/MM/yy\").format(new Date())");
		jrdtfPageDate.setStretchWithOverflow(true);
		jrdtfPageDate.setExpression(jrdexPageDate);
		
		JRDesignStaticText jrdstPage = new JRDesignStaticText();
		
		jrdstPage.setMarkup(FIELD_MARKUP_HTML);
		jrdstPage.setPdfEmbedded(true);
		jrdstPage.setPositionType(PositionTypeEnum.FLOAT);
		jrdstPage.setFontSize(FONT_SIZE_FOOTER_FIELDS);
		jrdstPage.setForecolor(Color.decode("#004280"));
		jrdstPage.setText("page");
		jrdstPage.setHorizontalTextAlign(HorizontalTextAlignEnum.RIGHT);
		
		JRDesignTextField jrdtfPageNumber = new JRDesignTextField();
		jrdtfPageNumber.setMarkup(FIELD_MARKUP_HTML);
		jrdtfPageNumber.setFontSize(FONT_SIZE_FOOTER_FIELDS);
		jrdtfPageNumber.setForecolor(Color.decode("#004280"));
		jrdtfPageNumber.setVerticalTextAlign(VerticalTextAlignEnum.TOP);
		
		jrdtfPageNumber.setVerticalTextAlign(VerticalTextAlignEnum.BOTTOM);
		jrdtfPageNumber.setHorizontalTextAlign(HorizontalTextAlignEnum.RIGHT);	
	    jrdtfPageNumber.setEvaluationTime(EvaluationTimeEnum.NOW);
		
		JRDesignExpression jrdexPageNumber = new JRDesignExpression();
		jrdexPageNumber.setText("$V{PAGE_NUMBER}+" + "\"" + "" + "\"");
		jrdtfPageNumber.setStretchWithOverflow(true);
		jrdtfPageNumber.setExpression(jrdexPageNumber);
		
		JRDesignTextField jrdtfPageCount = new JRDesignTextField();
		jrdtfPageCount.setMarkup(FIELD_MARKUP_HTML);
		jrdtfPageCount.setFontSize(FONT_SIZE_FOOTER_FIELDS);
		jrdtfPageCount.setForecolor(Color.decode("#004280"));
		jrdtfPageCount.setVerticalTextAlign(VerticalTextAlignEnum.TOP);
		
		jrdtfPageCount.setVerticalTextAlign(VerticalTextAlignEnum.BOTTOM);
		jrdtfPageCount.setHorizontalTextAlign(HorizontalTextAlignEnum.RIGHT);	
		jrdtfPageCount.setEvaluationTime(EvaluationTimeEnum.REPORT);
		
		JRDesignExpression jrdexPageCount = new JRDesignExpression();
		jrdexPageCount.setText("\"" + "/ " + "\"" + "+$V{PAGE_NUMBER}");
		jrdtfPageCount.setStretchWithOverflow(true);
		jrdtfPageCount.setExpression(jrdexPageCount);
		
		/*
		 * Calculate footer positions
		 */
		
		int jrdtfRecordIdWidth = calculateTextFieldWidth("Record ID: ids", "DejaVuSerif",(int)FONT_SIZE_FOOTER_FIELDS);
		int jrdtfPageDateWidth = calculateTextFieldWidth(new SimpleDateFormat("dd/MM/yy").format(new Date()), "DejaVuSerif",(int)FONT_SIZE_FOOTER_FIELDS);
		int jrdstPageWidth = calculateTextFieldWidth("page", "DejaVuSerif",(int)FONT_SIZE_FOOTER_FIELDS+1);
		int jrdtfPageNumberWidth = calculateTextFieldWidth("99", "DejaVuSerif",(int)FONT_SIZE_FOOTER_FIELDS+2);
		int jrdtfPageCountWidth = calculateTextFieldWidth("/999", "DejaVuSerif", (int)FONT_SIZE_FOOTER_FIELDS);
		
		int jrdtfRecordIdHeight = calculateTextFieldHeight("Record ID: id", 
				(jd.getPageWidth() - PAGE_MARGIN * 2) / 2, "DejaVuSerif",(int)FONT_SIZE_FOOTER_FIELDS);		
		int jrdtfPageDateHeight = calculateTextFieldHeight(new SimpleDateFormat("dd/MM/yy").format(new Date()), 
				(jd.getPageWidth() - PAGE_MARGIN * 2) / 2, "DejaVuSerif",(int)FONT_SIZE_FOOTER_FIELDS);		
		int jrdstPageHeight = calculateTextFieldHeight("page", 
				(jd.getPageWidth() - PAGE_MARGIN * 2) / 2, "DejaVuSerif",(int)FONT_SIZE_FOOTER_FIELDS);
		int jrdtfPageNumberHeight = calculateTextFieldHeight("999", 
				(jd.getPageWidth() - PAGE_MARGIN * 2) / 2, "DejaVuSerif",(int)FONT_SIZE_FOOTER_FIELDS);
		int jrdtfPageCountHeight = calculateTextFieldHeight("999", 
				(jd.getPageWidth() - PAGE_MARGIN * 2) / 2, "DejaVuSerif", (int)FONT_SIZE_FOOTER_FIELDS);

		dateFrame.setWidth((jd.getPageWidth() - PAGE_MARGIN * 2 ) / 2);
		
		jrdtfRecordID.setWidth(Math.max(jrdtfRecordIdWidth,Math.max(jrdtfPageDateWidth,jrdstPageWidth+jrdtfPageNumberWidth+jrdtfPageCountWidth)));
		jrdtfPageDate.setWidth(Math.max(jrdtfRecordIdWidth,Math.max(jrdtfPageDateWidth,jrdstPageWidth+jrdtfPageNumberWidth+jrdtfPageCountWidth)));
		jrdstPage.setWidth(jrdstPageWidth);
		jrdtfPageNumber.setWidth(jrdtfPageNumberWidth);
		jrdtfPageCount.setWidth(jrdtfPageCountWidth);
		
		dateFrame.setHeight(jrdtfRecordIdHeight+jrdtfPageDateHeight+jrdstPageHeight);
		jrdtfRecordID.setHeight(jrdtfRecordIdHeight);
		jrdtfPageDate.setHeight(jrdtfPageDateHeight);		
		jrdstPage.setHeight(jrdstPageHeight);
		jrdtfPageNumber.setHeight(jrdtfPageNumberHeight);
		jrdtfPageCount.setHeight(jrdtfPageCountHeight);

		dateFrame.setX((jd.getPageWidth() / 2));
		jrdtfRecordID.setX(dateFrame.getWidth()-jrdtfRecordID.getWidth());
		jrdtfPageDate.setX(dateFrame.getWidth()-jrdtfPageDate.getWidth());
		jrdstPage.setX(dateFrame.getWidth()-jrdtfPageNumberWidth-jrdtfPageCountWidth-jrdstPageWidth);
		jrdtfPageNumber.setX(dateFrame.getWidth()-jrdtfPageNumberWidth-jrdtfPageCountWidth);
		jrdtfPageCount.setX(dateFrame.getWidth()-jrdtfPageCountWidth);
		
		dateFrame.setY(FOOTER_SPACE*2);
		jrdtfRecordID.setY(0);
		jrdtfPageDate.setY(jrdtfRecordID.getHeight());
		jrdstPage.setY(jrdtfRecordIdHeight+jrdtfPageDateHeight);
		jrdtfPageNumber.setY(jrdtfRecordIdHeight+jrdtfPageDateHeight);
		jrdtfPageCount.setY(jrdtfRecordIdHeight+jrdtfPageDateHeight);
		
		dateFrame.addElement(jrdstPage);
		dateFrame.addElement(jrdtfPageNumber);
		dateFrame.addElement(jrdtfPageCount);
		dateFrame.addElement(jrdtfPageDate);
		dateFrame.addElement(jrdtfRecordID);
			
		jrdstfLeft.setWidth((jd.getPageWidth() - PAGE_MARGIN * 2 ) / 2 );
		jrdstfLeft.setX(PAGE_MARGIN);
		jrdstfLeft.setHeight(FOOTER_HEIGHT-FOOTER_SPACE);
		jrdstfLeft.setY(FOOTER_SPACE*2);
		
		jdbFooter.addElement(dateFrame);
		
		jdbFooter.addElement(jrdstfLeft);
		
		JRDesignSection jds = (JRDesignSection) jd.getDetailSection();
		
		for (JRDesignBand jrdb : jrdBands) {
			
			jds.addBand(jrdb);
		}
		
		jd.setPageHeader(jdbHeader);
		
		jd.setPageFooter(jdbFooter);

		JasperReport jr = JasperCompileManager.compileReport(jd);
		
		jr.setSectionType(SectionTypeEnum.BAND);
		
		logger.info("JRXML template generated");
		
		return jr;
	}
	
	private int calculateTextFieldHeight(String text, int width, String fontName, int fontSize) {
		
		Whitelist wl = new Whitelist();
		
		Font defaultFont = new Font(fontName, Font.PLAIN, fontSize);
		
		BufferedImage img = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
		
		FontMetrics fm = img.getGraphics().getFontMetrics(defaultFont);
		
		String[] textLines = text.split("\r\n|\r|\n");
		
		int height = 0;
		
		for(String textLine: textLines) {
			
			double w = (textLine.length()==0) ? 1.0 : Math.ceil((double)fm.stringWidth(Jsoup.clean(textLine,wl))/width);
			
			height += (int)(w) * fm.getHeight();
		}
		
		return height;
	}
	
	
	private int calculateTextFieldWidth(String text, String fontName, int fontSize) {
		
		Whitelist wl = new Whitelist();
		
		Font defaultFont = new Font(fontName, Font.PLAIN, fontSize);
		
		BufferedImage img = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
		
		FontMetrics fm = img.getGraphics().getFontMetrics(defaultFont);
		
		return fm.stringWidth(Jsoup.clean(text,wl));
	}
	
	
	public File writeJRXMLTemplateToFile(JasperReport jr, String jrxmlDestPath) throws JRException {
		
		JRXmlWriter.writeReport(jr, jrxmlDestPath, "UTF-8");
		
		logger.info("JRXML template file written to: " + jrxmlDestPath);
		
		return new File(jrxmlDestPath);
	}
}
