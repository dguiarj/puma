/**
 * Pentaho Metadata reader 
 * Sources:
 * - ServiceAction;
 * - SolutionRepositoryService;
 * - Ctools resources ??? (* in the future); 
 * @author David da Guia Carvalho <david.carvalho@serpro.gov.br>
 */

import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
//
import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
//
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
//
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
//
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/**
 * Metadata reading from BiServer through http request
 * @author David da Guia Carvalho <dguiarj@gmail.com>
 */
public class RepReader {
	
	static String action;
	static String details;
	static String solutionPath;
	static String solution;
	static String serverUrl;
	static String serverUserid;
	static String serverPassword;

	  protected static final Log logger = LogFactory.getLog(RepReader.class);
	  
  private static void setSysProp (){
	    System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.SimpleLog"); //$NON-NLS-1$ //$NON-NLS-2$
	    System.setProperty("org.apache.commons.logging.simplelog.showdatetime", "true"); //$NON-NLS-1$ //$NON-NLS-2$
	    System.setProperty("org.apache.commons.logging.simplelog.log.httpclient.wire.header", "warn");//$NON-NLS-1$ //$NON-NLS-2$
	    System.setProperty("org.apache.commons.logging.simplelog.log.org.apache.commons.httpclient", "warn");//$NON-NLS-1$ //$NON-NLS-2$	  
  }

  /* Metodo de autenticacao */
  /*
  private static int authPentaho (final String serverUserid, final String serverPassword, final HttpClient client) {
	    // Server Auth 
	    if ((serverUserid != null) && (serverUserid.length() > 0) && (serverPassword != null)
	            && (serverPassword.length() > 0)) {
	          Credentials creds = new UsernamePasswordCredentials(serverUserid, serverPassword);
	          client.getState().setCredentials(AuthScope.ANY, creds);
	          client.getParams().setAuthenticationPreemptive(true);    
	    }
	  return 1;
  }
  */
  /**
   * Get security info from server
   * @param serverUrl
   * @param serverUserid
   * @param serverPassword
   * @return String with a list of users and roles
   */
  public static String getSecurity (final String serverUrl, final String serverUserid, final String serverPassword) {
	  setSysProp();
	  action = "securitydetails";
	  details = "all";	    
	    String fullURL = null;
	          
	    try {
	    	fullURL = serverUrl + "/ServiceAction?action=" + URLEncoder.encode(action, "UTF-8")+"&details="+URLEncoder.encode(details, "UTF-8"); 
	    } catch(UnsupportedEncodingException e) {
	    	fullURL = serverUrl + "/ServiceAction?action=" + action +"&details=" + details;
	    	System.out.println("Erro na construcao da URL!" + e);
	    	// throw new Exception("Erro de publicação na classe publisher!" + e);
	    }
	    
	    System.out.println("url: "+fullURL );
	    HttpClient client = new HttpClient();    
	    HttpMethod method = new GetMethod(fullURL);
	    
	    ///
	    // Server Auth
	    ///
	    if ((serverUserid != null) && (serverUserid.length() > 0) && (serverPassword != null)
	            && (serverPassword.length() > 0)) {
	          Credentials creds = new UsernamePasswordCredentials(serverUserid, serverPassword);
	          client.getState().setCredentials(AuthScope.ANY, creds);
	          client.getParams().setAuthenticationPreemptive(true);    
	    }
	    
	    try {
	    //byte[] responseBody = method.getResponseBody();
	    	client.executeMethod(method);
	    	if (method.getStatusCode() == HttpStatus.SC_OK) {
	    		String response = method.getResponseBodyAsString();
	    		  //// -->
	    		  DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	    		  DocumentBuilder builder = factory.newDocumentBuilder();
	    	      InputSource is = new InputSource(new StringReader(response));
	    	      Document doc = builder.parse(is);
	    	      doc.getDocumentElement().normalize();
	    	      System.out.println("Root: "+doc.getDocumentElement().getNodeName());
	    	      NodeList userLst = doc.getElementsByTagName("user");
	    	      NodeList roleLst = doc.getElementsByTagName("role");
	    	      // NodeList nodeLst = doc.getElementsByTagName("file");	    	      
	    	      System.out.println("Quantidade de itens: "+userLst.getLength()+" - "+roleLst.getLength());
	    		  
	    	      // Usuarios
	    	      // List<List<String>> lstSecurity = new ArrayList<List<String>>();
	    	      // List<String> lstSecurity = new ArrayList<String>();

	    	      for (int s = 0; s < userLst.getLength(); s++) {
	    	 	        Node userNode = userLst.item(s);
	    	 	        // System.out.println("Dir : "+fstNode.getTextContent());        	    	 	        
	    	 	        if (userNode.getNodeType() == Node.ELEMENT_NODE) {	    	 	        	
	    	 	          // Element fstElmnt = (Element) userNode;
	    	 	          System.out.println("Usuario: "+ userNode.getTextContent());
	    	 	         // lstSecurity.add(userNode.getTextContent());
//	    	 	         lstSecurity.add(new ArrayList<String>("user",userNode.getTextContent()));
	    	 	        }
	    	      }
	    	      // Grupos
	    	      for (int s = 0; s < roleLst.getLength(); s++) {
	    	 	        Node roleNode = roleLst.item(s);
	    	 	        // System.out.println("Dir : "+fstNode.getTextContent());        	    	 	        
	    	 	        if (roleNode.getNodeType() == Node.ELEMENT_NODE) {	    	 	        	
	    	 	          // Element fstElmnt = (Element) userNode;
	    	 	          System.out.println("Grupos: "+ roleNode.getTextContent());
	    	 	        }
	    	      }
	    		// System.out.println("\n\n ---------------------------> \n Response = " + response);
	    		return response;
	    	}
	    } catch (Exception err) {
	    	
	    	System.out.println("Error: "+err);
	    }
	    
	    return "0";
	
  }
/**
 * Get pentaho solution structure from BiServer
 * @param serverUrl
 * @param serverUserid
 * @param serverPassword
 * @return xml string with pentaho solution directory structure (under the user permission)
 */
  public static String getSolutionsDir(final String serverUrl, final String serverUserid, final String serverPassword){
	  
	  	String action = "SolutionRepositoryService?component=getSolutionRepositoryDoc";	  
	  	String fullURL = serverUrl+"/"+action;
	    HttpClient client = new HttpClient();    
	    HttpMethod method = new GetMethod(fullURL);
	    
	    /* Server Auth */
	    if ((serverUserid != null) && (serverUserid.length() > 0) && (serverPassword != null)
	            && (serverPassword.length() > 0)) {
	          Credentials creds = new UsernamePasswordCredentials(serverUserid, serverPassword);
	          client.getState().setCredentials(AuthScope.ANY, creds);
	          client.getParams().setAuthenticationPreemptive(true);    
	    }
	    
	    try {
	    //byte[] responseBody = method.getResponseBody();
	    	client.executeMethod(method);
	    	if (method.getStatusCode() == HttpStatus.SC_OK) {
	    		String response = method.getResponseBodyAsString();
	    		System.out.println("\n\n\n--------------------------------------------\n Response = " + response);
	    		return response;
	    	}
	    } catch (Exception err) {
	    	System.out.println("Error: "+err);
	    }
	  
	  return "0";
	  
  }

/**
 * Remove server file! Under devel... 
 * @param serverUrl
 * @param path
 * @param serverUserid
 * @param serverPassword
 * @return
 */
public static int deleteSolutionFile (final String serverUrl, final String path, final String serverUserid, final String serverPassword) {
	String action = "SolutionRepositoryService?component=delete";
	String solution = "&solution="+"DM_SUPOP";
	String path1 = "&path=%2F";
	String fileName = "&name=pentahoxml_picker.xaction";
	String fullURL = serverUrl+"/"+action+solution+path1+fileName;
	HttpClient client = new HttpClient();    
    HttpMethod method = new GetMethod(fullURL);	
	
	
	/* Server Auth */
    if ((serverUserid != null) && (serverUserid.length() > 0) && (serverPassword != null)
            && (serverPassword.length() > 0)) {
          Credentials creds = new UsernamePasswordCredentials(serverUserid, serverPassword);
          client.getState().setCredentials(AuthScope.ANY, creds);
          client.getParams().setAuthenticationPreemptive(true);    
    }
    
    try {
    //byte[] responseBody = method.getResponseBody();
    	// client.executeMethod(method);
    	if (method.getStatusCode() == HttpStatus.SC_OK) {
    		String response = method.getResponseBodyAsString();
    		System.out.println("\n\n\n--------------------------------------------\n Response = " + response);
    		// return response;
    	}
    } catch (Exception err) {
    	System.out.println("Error: "+err);
    }
	
	return 0;
}
  
/**
 * Sem uso no momento... resolvi não utilizar o sprite do pentaho pq o custo é elevado para o benefico em um app desktop  
 * @param bufferedImage
 * @return
 */
/*
  public static ImageData convertToSWT(BufferedImage bufferedImage) {
	    if (bufferedImage.getColorModel() instanceof DirectColorModel) {
	        DirectColorModel colorModel = (DirectColorModel) bufferedImage.getColorModel();
	        PaletteData palette = new PaletteData(
	            colorModel.getRedMask(),
	            colorModel.getGreenMask(),
	            colorModel.getBlueMask()
	        );
	        ImageData data = new ImageData(
	            bufferedImage.getWidth(),
	            bufferedImage.getHeight(), colorModel.getPixelSize(),
	            palette
	        );
	        WritableRaster raster = bufferedImage.getRaster();
	        int[] pixelArray = new int[3];
	        for (int y = 0; y < data.height; y++) {
	            for (int x = 0; x < data.width; x++) {
	                raster.getPixel(x, y, pixelArray);
	                int pixel = palette.getPixel(
	                    new RGB(pixelArray[0], pixelArray[1], pixelArray[2])
	                );
	                data.setPixel(x, y, pixel);
	            }
	        }
	        return data;
	    } else if (bufferedImage.getColorModel() instanceof IndexColorModel) {
	        IndexColorModel colorModel = (IndexColorModel) bufferedImage.getColorModel();
	        int size = colorModel.getMapSize();
	        byte[] reds = new byte[size];
	        byte[] greens = new byte[size];
	        byte[] blues = new byte[size];
	        colorModel.getReds(reds);
	        colorModel.getGreens(greens);
	        colorModel.getBlues(blues);
	        RGB[] rgbs = new RGB[size];
	        for (int i = 0; i < rgbs.length; i++) {
	            rgbs[i] = new RGB(reds[i] & 0xFF, greens[i] & 0xFF, blues[i] & 0xFF);
	        }
	        PaletteData palette = new PaletteData(rgbs);
	        ImageData data = new ImageData(
	            bufferedImage.getWidth(),
	            bufferedImage.getHeight(),
	            colorModel.getPixelSize(),
	            palette
	        );
	        data.transparentPixel = colorModel.getTransparentPixel();
	        WritableRaster raster = bufferedImage.getRaster();
	        int[] pixelArray = new int[1];
	        for (int y = 0; y < data.height; y++) {
	            for (int x = 0; x < data.width; x++) {
	                raster.getPixel(x, y, pixelArray);
	                data.setPixel(x, y, pixelArray[0]);
	            }
	        }
	        return data;
	    }
	    return null;
	}
  */
}
