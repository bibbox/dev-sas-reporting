package msig.toolbox.redcap.jasper;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.design.JRDesignBand;
import net.sf.jasperreports.engine.design.JRDesignExpression;
import net.sf.jasperreports.engine.design.JRDesignField;
import net.sf.jasperreports.engine.design.JRDesignImage;
import net.sf.jasperreports.engine.design.JRDesignSection;
import net.sf.jasperreports.engine.design.JRDesignStaticText;
import net.sf.jasperreports.engine.design.JRDesignStyle;
import net.sf.jasperreports.engine.design.JRDesignTextField;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.type.HorizontalTextAlignEnum;
import net.sf.jasperreports.engine.type.PositionTypeEnum;
import net.sf.jasperreports.engine.type.SectionTypeEnum;

import net.sf.jasperreports.engine.type.VerticalImageAlignEnum;
import net.sf.jasperreports.engine.type.VerticalTextAlignEnum;
import net.sf.jasperreports.engine.xml.JRXmlWriter;


public class REDCapJRXMLTemplateGenerator {

	private static final Logger logger = LogManager.getLogger(REDCapJRXMLTemplateGenerator.class);
	
	public final static int PAGE_WIDTH_A4 = 595;	
	public final static int PAGE_HEIGHT_A4 = 842;	
	public final static int PAGE_MARGIN = 25;
	public final static int ELEMENT_HEIGHT = 40;
	public final static int ELEMENT_VERTICAL_SPACING = 10;
	public final static int SECTION_HEADER_HEIGHT = 60;
	public final static int BANNER_HEIGHT = 60;
	public final static int RADIO_BUTTON_SIZE = 20;
	
	private final static String DEFAULT_FONT_BOLD = "net/sf/jasperreports/fonts/dejavu/DejaVuSerif-Bold.ttf";
	private final static String DEFAULT_FONT_ITALIC = "net/sf/jasperreports/fonts/dejavu/DejaVuSerif-Italic.ttf";
	private final static String FIELD_NAME = "field_name";
	private final static String FIELD_LABEL = "field_label";
	private final static String FIELD_SELECT_CHOICES_OR_CALCULATIONS = "select_choices_or_calculations";
	private final static String FIELD_TYPE = "field_type";
	private final static String SECTION_HEADER = "section_header";
	private final static String FIELD_TYPE_DESCRIPTIVE = "descriptive";
	private final static String FIELD_TYPE_TEXT = "text";
	private final static String FIELD_TYPE_RADIO = "radio";
	private final static String FIELD_TYPE_YESNO = "yesno";
	private final static String FIELD_VAL_YES = "Yes";
	private final static String FIELD_VAL_NO = "No";
	private final static String FIELDNAME_RECORD_ID = "record_id";
	private final static float FONT_SIZE_SECTION_HEADER = 14f;
	//private final static float FONT_SIZE_DESCRIPTIVE = 12f;
	private final static float FONT_SIZE_TEXT_FIELDS = 11f;
	private final static String FIELD_MARKUP_HTML = "html";
	
	
	private Reader metadata;
	
	
	public REDCapJRXMLTemplateGenerator(InputStream metadata) {
		
		this.metadata=new InputStreamReader(metadata);
	}
	
	
	public JasperReport generateREDCapRecordJRXMLTemplateWithBandPerElement() throws FileNotFoundException, IOException, JRException {
		
		CSVParser csvp = new CSVParser(metadata, CSVFormat.DEFAULT.withHeader());
		
		Map<String, Integer> headerMap = csvp.getHeaderMap();
		List<CSVRecord> metaRecords = csvp.getRecords();
		
		csvp.close();
		
		JasperDesign jd = new JasperDesign();
		jd.setName("REDCAP Test");
		jd.setPageWidth(PAGE_WIDTH_A4);
		jd.setPageHeight(PAGE_HEIGHT_A4);
		jd.setTopMargin(PAGE_MARGIN);
		jd.setBottomMargin(PAGE_MARGIN);
		
		JRDesignBand jdbHeader = new JRDesignBand();		
		jdbHeader.setHeight(BANNER_HEIGHT+ELEMENT_VERTICAL_SPACING);
		
		JRDesignImage jrdiBanner = new JRDesignImage(new JRDesignStyle().getDefaultStyleProvider());
		jrdiBanner.setWidth(PAGE_WIDTH_A4-PAGE_MARGIN*2);
		jrdiBanner.setHeight(BANNER_HEIGHT);
		JRDesignExpression jrdiexBanner = new JRDesignExpression();
		jrdiexBanner.setText("\""+""+"banner.jpg"+"\"");
		jrdiBanner.setExpression(jrdiexBanner);
		jdbHeader.addElement(jrdiBanner);
		
		List<JRDesignBand> jrdBands = new ArrayList<JRDesignBand>();
		
		for(CSVRecord metaRecord: metaRecords) {
			
			JRDesignBand jdbDetail = new JRDesignBand(); 
			String fieldName = metaRecord.get(headerMap.get(FIELD_NAME));
			String fieldLabel = metaRecord.get(headerMap.get(FIELD_LABEL));
			String fieldType = metaRecord.get(headerMap.get(FIELD_TYPE));
			String sectionHeader = metaRecord.get(headerMap.get(SECTION_HEADER));
			
			boolean hasSectionHeader = sectionHeader.trim().length() > 0;
			
			if(hasSectionHeader) {
				
				JRDesignStaticText jrdst = new JRDesignStaticText();
				
				jrdst.setMarkup(FIELD_MARKUP_HTML);
				jrdst.setPdfFontName(DEFAULT_FONT_BOLD);
				jrdst.setPdfEmbedded(true);
				jrdst.setPositionType(PositionTypeEnum.FLOAT);
				jrdst.setFontSize(FONT_SIZE_SECTION_HEADER);
				jrdst.setText(sectionHeader);
				jrdst.setVerticalTextAlign(VerticalTextAlignEnum.MIDDLE);
				jrdst.setWidth(jd.getPageWidth()-PAGE_MARGIN*2);
				jrdst.setHeight(SECTION_HEADER_HEIGHT);
				jrdst.setY(0);
				jrdst.setBold(true);
				jdbDetail.setHeight(SECTION_HEADER_HEIGHT);
				jdbDetail.addElement(jrdst);
				jrdBands.add(jdbDetail);
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
				jrdst.setText(fieldLabel);
				jrdst.setVerticalTextAlign(VerticalTextAlignEnum.MIDDLE);
				jrdst.setWidth(jd.getPageWidth() - PAGE_MARGIN * 2);
				int tfHeight = calculateTextFieldHeight(fieldLabel, jrdst.getWidth(),"DejaVuSerif");
				jrdst.setHeight(tfHeight);
				jrdst.setY(0);
				jdbDetail.setHeight(tfHeight);
				jdbDetail.addElement(jrdst);
			}
			
			if(FIELD_TYPE_TEXT.equals(fieldType) || FIELD_TYPE_YESNO.equals(fieldType) || FIELD_TYPE_RADIO.equals(fieldType)) {
				
				JRDesignField jdf = new JRDesignField();
				jdf.setName(fieldName);
				jd.addField(jdf);
				
				JRDesignStaticText jrdst = new JRDesignStaticText();
				jrdst.setMarkup(FIELD_MARKUP_HTML);
				jrdst.setText(fieldLabel);
				jrdst.setVerticalTextAlign(VerticalTextAlignEnum.MIDDLE);
				jrdst.setFontSize(FONT_SIZE_TEXT_FIELDS);
				jrdst.setWidth((jd.getPageWidth() - PAGE_MARGIN * 2) / 2);
				int tfHeight = calculateTextFieldHeight(fieldLabel, jrdst.getWidth(),"DejaVuSerif");
				jrdst.setHeight(tfHeight);
				jrdst.setY(0);
				
				jdbDetail.addElement(jrdst);
				
				int xr = jrdst.getWidth() + PAGE_MARGIN * 2;
				
				if(FIELD_TYPE_TEXT.equals(fieldType)) {
					
					JRDesignTextField jrdtf = new JRDesignTextField();
					jrdtf.setMarkup(FIELD_MARKUP_HTML);
					jrdtf.setFontSize(FONT_SIZE_TEXT_FIELDS);
					jrdtf.setVerticalTextAlign(VerticalTextAlignEnum.MIDDLE);
					jrdtf.setWidth(jrdst.getWidth() - PAGE_MARGIN * 2);
					jrdtf.setHeight(jrdst.getHeight());
					jrdtf.setY(0);
					jrdtf.setX(xr);
					JRDesignExpression jrdex = new JRDesignExpression();
					jrdex.addFieldChunk(fieldName);
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
				
				if(FIELD_TYPE_YESNO.equals(fieldType)) {
					
					JRDesignStaticText jrdstYes = new JRDesignStaticText();
					jrdstYes.setMarkup(FIELD_MARKUP_HTML);
					jrdstYes.setText(FIELD_VAL_YES);
					jrdstYes.setFontSize(FONT_SIZE_TEXT_FIELDS);
					jrdstYes.setVerticalTextAlign(VerticalTextAlignEnum.MIDDLE);
					jrdstYes.setWidth(PAGE_MARGIN);
					jrdstYes.setHeight(tfHeight);
					jrdstYes.setX(xr);
					jrdstYes.setY(0);
					jdbDetail.addElement(jrdstYes);
					
					JRDesignImage jrdiYes = new JRDesignImage(new JRDesignStyle().getDefaultStyleProvider());
					JRDesignExpression jrdiexYes = new JRDesignExpression();
					JRDesignExpression jrdiexPrintWhenYes = new JRDesignExpression();
					jrdiexPrintWhenYes.setText("$F{"+fieldName+"}.trim().equalsIgnoreCase(\""+FIELD_VAL_YES+"\")");
					jrdiYes.setPrintWhenExpression(jrdiexPrintWhenYes);
					jrdiexYes.setText("\""+""+"yes.png"+"\"");
			        jrdiYes.setExpression(jrdiexYes);
			        jrdiYes.setWidth(125);
			        jrdiYes.setHeight(tfHeight);
			        jrdiYes.setY(0);
			        jrdiYes.setVerticalImageAlign(VerticalImageAlignEnum.MIDDLE);
			        
			        jrdiYes.setX(xr + jrdstYes.getWidth());
					jdbDetail.addElement(jrdiYes);
					JRDesignStaticText jrdstNo = new JRDesignStaticText();
					jrdstNo.setMarkup(FIELD_MARKUP_HTML);
					jrdstNo.setText(FIELD_VAL_NO);
					jrdstNo.setFontSize(FONT_SIZE_TEXT_FIELDS);
					jrdstNo.setVerticalTextAlign(VerticalTextAlignEnum.MIDDLE);
					jrdstNo.setWidth(PAGE_MARGIN);
					jrdstNo.setHeight(tfHeight);
					jrdstNo.setX(jrdst.getWidth()+jrdstYes.getWidth()+jrdiYes.getWidth());
					jrdstNo.setY(0);
					jdbDetail.addElement(jrdstNo);
					
					JRDesignImage jrdiNo = new JRDesignImage(new JRDesignStyle().getDefaultStyleProvider());
					JRDesignExpression jrdiexNo = new JRDesignExpression();
					JRDesignExpression jrdiexPrintWhenNo = new JRDesignExpression();
					jrdiexPrintWhenNo.setText("$F{"+fieldName+"}.trim().equalsIgnoreCase(\""+FIELD_VAL_NO+"\")");
					jrdiNo.setPrintWhenExpression(jrdiexPrintWhenNo);
					jrdiNo.setVerticalImageAlign(VerticalImageAlignEnum.MIDDLE);
					jrdiexNo.setText("\""+""+"no.png"+"\"");
					jrdiNo.setExpression(jrdiexNo);
					jrdiNo.setWidth(125);
					jrdiNo.setHeight(tfHeight);
					jrdiNo.setY(0);
					jrdiNo.setX(xr+jrdstNo.getWidth());
					jdbDetail.addElement(jrdiNo);
				}
				
				if(FIELD_TYPE_RADIO.equals(fieldType)) {
					
					String fieldSelectChoices = metaRecord.get(headerMap.get(FIELD_SELECT_CHOICES_OR_CALCULATIONS));
					
					String[] fieldSelectChoicesElements = fieldSelectChoices.split(Pattern.quote("|"));
					
					for(int i=0; i<fieldSelectChoicesElements.length; i++) {
						
						fieldSelectChoicesElements[i] = 
								
								fieldSelectChoicesElements[i].substring(fieldSelectChoicesElements[i].indexOf(",") + 1, fieldSelectChoicesElements[i].length()).trim();
					}
					
					int maxLineWidth = jrdst.getWidth() - PAGE_MARGIN * 2;
					int sumWidth = 0; 
					int bandHeight = 0;
					int y = 0;
					int prevElementHeight = 0;
					
					for(String fieldchoiceElement : fieldSelectChoicesElements) {
							
						int fieldSelectChoicesElementWidth = calculateTextFieldWidth(fieldchoiceElement, "DejaVuSerif");
						int height = (1 + (int)((fieldSelectChoicesElementWidth / maxLineWidth))) * RADIO_BUTTON_SIZE;
						
						bandHeight = bandHeight + height;
						
						fieldSelectChoicesElementWidth = 
								(fieldSelectChoicesElementWidth > (maxLineWidth - RADIO_BUTTON_SIZE)) ? (maxLineWidth - RADIO_BUTTON_SIZE) : fieldSelectChoicesElementWidth;
								
						if(height > RADIO_BUTTON_SIZE || (sumWidth + (fieldSelectChoicesElementWidth + RADIO_BUTTON_SIZE)) > maxLineWidth) {
							
							sumWidth = 0;
							y = (prevElementHeight == RADIO_BUTTON_SIZE) ? y + prevElementHeight : y; 
						}
						
						JRDesignStaticText jrdstFieldSelectChoice = new JRDesignStaticText();
						jrdstFieldSelectChoice.setMarkup(FIELD_MARKUP_HTML);
						jrdstFieldSelectChoice.setFontSize(FONT_SIZE_TEXT_FIELDS);
						jrdstFieldSelectChoice.setText(fieldchoiceElement);
						jrdstFieldSelectChoice.setVerticalTextAlign(VerticalTextAlignEnum.MIDDLE);
						jrdst.setVerticalTextAlign(VerticalTextAlignEnum.MIDDLE);
						jrdstFieldSelectChoice.setWidth(fieldSelectChoicesElementWidth);
						jrdstFieldSelectChoice.setX(xr + sumWidth);
						jrdstFieldSelectChoice.setHeight(height);
						
						jrdstFieldSelectChoice.setY(y);
						
						sumWidth = sumWidth + fieldSelectChoicesElementWidth;
						
						JRDesignImage jrdiUnchecked = new JRDesignImage(new JRDesignStyle().getDefaultStyleProvider());
						JRDesignExpression jrdiexUnchecked = new JRDesignExpression();
						
						jrdiexUnchecked.setText("\""+""+"unchecked.png"+"\"");
						jrdiUnchecked.setExpression(jrdiexUnchecked);
						jrdiUnchecked.setWidth(RADIO_BUTTON_SIZE);
						jrdiUnchecked.setHeight(height);
						jrdiUnchecked.setX(xr + sumWidth);
						jrdiUnchecked.setY(y);
						jrdiUnchecked.setVerticalImageAlign(VerticalImageAlignEnum.MIDDLE);
						
						JRDesignImage jrdiChecked = new JRDesignImage(new JRDesignStyle().getDefaultStyleProvider());
						JRDesignExpression jrdiexChecked = new JRDesignExpression();
						JRDesignExpression jrdiexPrintWhenChecked = new JRDesignExpression();
						
						jrdiexPrintWhenChecked.setText("$F{"+fieldName+"}.trim().equalsIgnoreCase(\""+fieldchoiceElement+"\")");
						jrdiChecked.setPrintWhenExpression(jrdiexPrintWhenChecked);
						jrdiexChecked.setText("\""+""+"checked.png"+"\"");
						jrdiChecked.setExpression(jrdiexChecked);
						jrdiChecked.setWidth(RADIO_BUTTON_SIZE);
						jrdiChecked.setHeight(height);
						jrdiChecked.setX(xr + sumWidth);
						jrdiChecked.setY(y);
						jrdiChecked.setVerticalImageAlign(VerticalImageAlignEnum.MIDDLE);
						
						jdbDetail.addElement(jrdstFieldSelectChoice);
						jdbDetail.addElement(jrdiUnchecked);
						jdbDetail.addElement(jrdiChecked);
						
						sumWidth = sumWidth + RADIO_BUTTON_SIZE;
						
						y = (height > RADIO_BUTTON_SIZE || sumWidth > maxLineWidth) ? y + height : y;
						
						prevElementHeight = height; 
					}
					tfHeight = (tfHeight < bandHeight) ? bandHeight : tfHeight;
					jrdst.setHeight(tfHeight);
				}
				jdbDetail.setHeight(tfHeight);
			}
			jrdBands.add(jdbDetail);
		}
			
		JRDesignSection jds = (JRDesignSection) jd.getDetailSection();
		
		for (JRDesignBand jrdb : jrdBands) {
			
			jds.addBand(jrdb);
		}
		
		jd.setPageHeader(jdbHeader);
		
		JasperReport jr = JasperCompileManager.compileReport(jd);
		
		jr.setSectionType(SectionTypeEnum.BAND);
		
		logger.info("JRXML template generated");
		
		return jr;
	}
	
	
	private int calculateTextFieldHeight(String text, int width, String fontName) {
		
		Font defaultFont = new Font(fontName, Font.PLAIN, 12);
		
		BufferedImage img = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
		
		FontMetrics fm = img.getGraphics().getFontMetrics(defaultFont);
		
		int height  = ((fm.stringWidth(text)/width)+1) * fm.getHeight() + ELEMENT_VERTICAL_SPACING;
		
		return height;
	}
	
	
	private int calculateTextFieldWidth(String text, String fontName) {
		
		Font defaultFont = new Font(fontName, Font.PLAIN, (int)FONT_SIZE_TEXT_FIELDS);
		
		BufferedImage img = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
		
		FontMetrics fm = img.getGraphics().getFontMetrics(defaultFont);
		
		return fm.stringWidth(text);
	}
	
	
	public File writeJRXMLTemplateToFile(JasperReport jr, String jrxmlDestPath) throws JRException {
		
		JRXmlWriter.writeReport(jr, jrxmlDestPath, "UTF-8");
		
		logger.info("JRXML template file written to: " + jrxmlDestPath);
		
		return new File(jrxmlDestPath);
	}
}
