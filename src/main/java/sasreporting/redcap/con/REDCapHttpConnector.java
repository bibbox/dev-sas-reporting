package sasreporting.redcap.con;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class REDCapHttpConnector {

	private static final Logger logger = LogManager.getLogger(REDCapHttpConnector.class);
	
	public final static String FIELD_URL = "url";
	public final static String FIELD_TOKEN = "token";
	public final static String FIELD_CONTENT = "content";
	public final static String FIELD_FORMAT = "format";
	public final static String FIELD_TYPE = "type";
	public final static String FIELD_RAW_OR_LABEL = "rawOrLabel";
	public final static String FIELD_RAW_OR_LABEL_HEADERS = "rawOrLabelHeaders";
	public final static String FIELD_EXPORT_CHECKBOX_LABEL = "exportCheckboxLabel";
	public final static String FIELD_RECORDS = "records";
	public final static String FIELD_FIELDS = "fields";
	
	private String url, tokenID;
	
	
	public REDCapHttpConnector(String url, String tokenID) {
		
		this.url=url;
		this.tokenID=tokenID;
	}
	
	
	public File writeMetadataFromREDCapProjectToFile(String exportFileFormat, String absolutePath)
			throws IOException, UnsupportedOperationException, KeyManagementException, NoSuchAlgorithmException, KeyStoreException {
			
		String output = new StringBuffer(absolutePath).append(".").append(exportFileFormat).toString();
			
		ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair(FIELD_TOKEN, this.tokenID));
		params.add(new BasicNameValuePair(FIELD_CONTENT, "metadata"));
		params.add(new BasicNameValuePair(FIELD_FORMAT, exportFileFormat));
		params.add(new BasicNameValuePair(FIELD_TYPE, "flat"));
			
		logger.info("Writing metadata to file: " + output);
			
		HttpResponse response = getREDCapHttpResponse(params);
		
		writeHttpResponseToFile(response, output);
		
		logger.info("Written metadata file to: " + output);
		
		EntityUtils.consume(response.getEntity());
		
		return new File(output);
	}
	
	
	public InputStream getInfoFromREDCapProject(String fields, String exportFormat) throws ClientProtocolException, IOException, KeyManagementException, NoSuchAlgorithmException, KeyStoreException {
		
		ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair(FIELD_TOKEN, this.tokenID));
		params.add(new BasicNameValuePair(FIELD_CONTENT, "project"));
		params.add(new BasicNameValuePair(FIELD_FORMAT, exportFormat));
		
		if(fields != null && !("".equals(fields))) {
			
			params.add(new BasicNameValuePair(FIELD_FIELDS, fields));
		}
		
		logger.info("Getting record input stream...");
				
		HttpResponse response = getREDCapHttpResponse(params);
	
		return response.getEntity().getContent();
	}
	
	
	public InputStream getMetadataFromREDCapProject(String exportFormat)
			throws IOException, UnsupportedOperationException, KeyManagementException, NoSuchAlgorithmException, KeyStoreException {
			
		ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair(FIELD_TOKEN, this.tokenID));
		params.add(new BasicNameValuePair(FIELD_CONTENT, "metadata"));
		params.add(new BasicNameValuePair(FIELD_FORMAT, exportFormat));
		params.add(new BasicNameValuePair(FIELD_TYPE, "flat"));
			
		logger.info("Getting metadata input stream ...");
			
		HttpResponse response = getREDCapHttpResponse(params);
		
		return response.getEntity().getContent();
	}
	
	
	public File writeRecordsFromREDCapProjectToFile(String url, String tokenID, String recordIDs, String fields, String format, String absolutePath)
			throws IOException, UnsupportedOperationException, KeyManagementException, NoSuchAlgorithmException, KeyStoreException {
			
		String output = new StringBuffer(absolutePath).append(".").append(format).toString();
				
		ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair(FIELD_TOKEN, tokenID));
		params.add(new BasicNameValuePair(FIELD_CONTENT, "record"));
		params.add(new BasicNameValuePair(FIELD_FORMAT, format));
		params.add(new BasicNameValuePair(FIELD_TYPE, "flat"));
		params.add(new BasicNameValuePair(FIELD_RAW_OR_LABEL, "label"));
		params.add(new BasicNameValuePair(FIELD_RAW_OR_LABEL_HEADERS, "raw"));
		params.add(new BasicNameValuePair(FIELD_EXPORT_CHECKBOX_LABEL, "true"));
		params.add(new BasicNameValuePair("exportSurveyFields", "true"));
		params.add(new BasicNameValuePair(FIELD_RECORDS, recordIDs));
		
		if(fields != null && !("".equals(fields))) {
			
			params.add(new BasicNameValuePair(FIELD_FIELDS, fields));
		}	
		
		logger.info("Writing records to file: " + output);
				
		HttpResponse response = getREDCapHttpResponse(params);
			
		writeHttpResponseToFile(response, output);
		
		logger.info("Written records file to: " + output);
		
		EntityUtils.consume(response.getEntity());
		
		return new File(output);
	}
	
	
	public InputStream getRecordsFromREDCapProject(String recordIDs, String fields, String format)
			throws IOException, UnsupportedOperationException, KeyManagementException, NoSuchAlgorithmException, KeyStoreException {
			
		ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair(FIELD_TOKEN, this.tokenID));
		params.add(new BasicNameValuePair(FIELD_CONTENT, "record"));
		params.add(new BasicNameValuePair(FIELD_FORMAT, format));
		params.add(new BasicNameValuePair(FIELD_TYPE, "flat"));
		params.add(new BasicNameValuePair(FIELD_RAW_OR_LABEL, "label"));
		params.add(new BasicNameValuePair(FIELD_RAW_OR_LABEL_HEADERS, "raw"));
		params.add(new BasicNameValuePair(FIELD_EXPORT_CHECKBOX_LABEL, "true"));
		
		if(recordIDs != null && !("".equals(recordIDs))) {
			
			params.add(new BasicNameValuePair(FIELD_RECORDS, recordIDs));
		}
			
		if(fields != null && !("".equals(fields))) {
			
			params.add(new BasicNameValuePair(FIELD_FIELDS, fields));
		}
		
		logger.info("Getting record input stream...");
				
		HttpResponse response = getREDCapHttpResponse(params);
	
		return response.getEntity().getContent();
	}
	
	
	public File writeUsersFromREDCapProjectToFile(String recordIDs, String fields, String format, String absolutePath)
			throws IOException, UnsupportedOperationException, KeyManagementException, NoSuchAlgorithmException, KeyStoreException {
			
		String output = new StringBuffer(absolutePath).append(".").append(format).toString();
				
		ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair(FIELD_TOKEN, this.tokenID));
		params.add(new BasicNameValuePair(FIELD_CONTENT, "user"));
		params.add(new BasicNameValuePair(FIELD_FORMAT, format));
		
		logger.info("Writing users to file: " + output);
				
		HttpResponse response = getREDCapHttpResponse(params);
			
		writeHttpResponseToFile(response, output);
		
		logger.info("Written users file to: " + output);
		
		EntityUtils.consume(response.getEntity());
		
		return new File(output);
	}
	
	
	public File writeParticipantsFromREDCapProjectToFile(String format, String absolutePath, String instrumentName)
			throws IOException, UnsupportedOperationException, KeyManagementException, NoSuchAlgorithmException, KeyStoreException {
			
		String output = new StringBuffer(absolutePath).append(".").append(format).toString();
				
		ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair(FIELD_TOKEN, this.tokenID));
		params.add(new BasicNameValuePair(FIELD_CONTENT, "participantList"));
		params.add(new BasicNameValuePair("instrument", instrumentName));
		params.add(new BasicNameValuePair(FIELD_FORMAT, format));
			
		logger.info("Writing participant list to file: " + output);
		
		HttpResponse response = getREDCapHttpResponse(params);
			
		writeHttpResponseToFile(response, output);
			
		logger.info("Written participant list file to: " + output);
		
		return new File(output);
	}
	
	
	private HttpResponse getREDCapHttpResponse(ArrayList<NameValuePair> params) throws ClientProtocolException, IOException, KeyManagementException, NoSuchAlgorithmException, KeyStoreException {
		
		HttpClient client = HttpClients.custom()
		    .setSSLSocketFactory(new SSLConnectionSocketFactory(SSLContexts.custom()
		    .loadTrustMaterial(null, new TrustSelfSignedStrategy())
		    .build()))
		    .build();
		
		HttpPost request = new HttpPost(this.url); 
		
		HttpResponse response;
		
		request.setEntity(new UrlEncodedFormEntity(params));
		response = client.execute(request);
		
		request.completed();
		
		return response;
	}
	
	
	public void writeInputStreamToFile(InputStream is, String destPath) throws UnsupportedOperationException, IOException {
		
		OutputStream os = new FileOutputStream(new File(destPath));
			
		int read = 0;
		byte[] bytes = new byte[1024];
			
		while((read = is.read(bytes)) != -1) {
				
			os.write(bytes, 0, read);
		}
		
		logger.info("File written: " + destPath);
		
		is.close();
		os.close();
	}
	
	
	private void writeHttpResponseToFile(HttpResponse httpRes, String destPath) throws UnsupportedOperationException, IOException {
		
		InputStream is = httpRes.getEntity().getContent();
		
		writeInputStreamToFile(is, destPath);
	}
}