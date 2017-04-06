package sasreporting.redcap.spark;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.URL;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class SPARKServiceMain {

	
	private static final Logger logger = LogManager.getLogger(SPARKServiceMain.class);
	
	private static String FIELD_ACTION = "action";
	private static String FIELD_ACTION_START = "start";
	private static String FIELD_ACTION_STOP = "stop";
	private static String FIELD_TOKEN_SHUTDOWN = "shutdown_token";
	private static String FIELD_PORT = "jetty_port";
	
	private static final Options options = new Options()
			.addOption(FIELD_ACTION, true, "start, stop")
			.addOption(FIELD_PORT,true,"Jetty port")
			.addOption(FIELD_TOKEN_SHUTDOWN, true, "Stop token");
	
	/**
	 * Start, Stop command of the REDCap SPARK service
	 * @param argv single argument to start and stop the SPARK service
	 */
	
	public static void main(String[] argv) {
		
		REDCapSPARKService service = new REDCapSPARKService();
		service.init();
    }
	
	/**
	 * Stops the SPARK service
	 */
	
	private static void stopService(String port, String shutdownToken) {
		
		try {
			
			Socket s = new Socket(InetAddress.getByName("127.0.0.1"), Integer.parseInt(port));
		    
			OutputStream out = s.getOutputStream();
		    out.write((shutdownToken + "\r\nstop\r\n").getBytes());
		    out.flush();
		    
		    s.close();
		
		} catch (IOException e) {
			
			logger.error(e);
		}
	}
}
