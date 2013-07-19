/** 
 * Unofficial application for Publishing into Pentaho BiServer
 * 
 * General purpouse tool for publishing
 * Intend to fullfill the need of publishing files to the server without the file system access
 * and still keep the integrati of Pentaho by usage of its publishing struture.
 * It comes handi whem using files that dont have its own publishing method like: xaction, cda, cde, etc.. 
 * May also be used for files like: js, css, imgs, html, xml, json, krt, kjb, etc...)
 * 
 *  This app uses the class created by: mdamour e mbatchelor
 * 
 * @Created June 07, 2013
 * @author David da Guia Carvalho <dguiarj at gmail dot com>
 * 
 */

import java.awt.image.BufferedImage;
import java.io.File; // Select de arquivo
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.StringReader;
// import java.io.FileInputStream; //Stream de config
import java.io.IOException; // Erros de IO
import java.util.ArrayList;
import java.util.List;
import java.util.Properties; // Tratamento de config

import javax.imageio.ImageIO;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

// import javax.swing.filechooser.FileNameExtensionFilter;

import org.eclipse.swt.SWT;
// import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Monitor;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/*
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;
*/

import org.apache.commons.logging.Log;

/**
 * 
 * Unofficial Frontend for Pentaho publishing through http request
 * @author David da Guia Carvalho <dguiarj@gmail.com>
 * 
 */
public class Puma {
	
	 static Log errorCrtl;   // ?
	 static int x; 			 // Posicao na tela eixo x, monitor display 
	 static int y;			 // Posicao na tela eixo y, monitor display
	 static String[] filterNames =  new String[] { "Todos (*.*)","Xaction (*.xaction)","Ctools DataAccess (*.cda)", "Ctools Editor(*.cdfde)", "Ctools Editor (*.wcdf)", "XML (*.xml)", "HTML (*.html)", "HTML (*.htm)", "CSS (*.css)", "Kettle Job (*.kjb)", "Kettle transformation (*.ktr)" }; // Tipos de arquivos (com descricao) possiveis de selcionar	 
	 static String[] acceptedFiles = new String[] { "*.*","*.xaction", "*.cda", "*.cdfde", "*.wcdf", "*.xml", "*.html", "*.htm", "*.css", "*.kjb", "*.ktr"}; // Tipos de arquivos possiveis de selcionar
	 static PublisherUtil publisher; 				// Instancia da classe publisher, classe original do pentaho com pequenas modificações para simplificar
	 static String publishURL; 						// variaveis de post - URL do servidor com publisher
	 static String publishPath; 					// variaveis de post - caminho da solucao (solution)
	 static String publishPassword; 				// variaveis de post - Senha de publicacao
	 static String serverUserid;					// variaveis de post - usuario do servidor 
	 static String serverPassword; 					// variaveis de post - senha
	 static Boolean overwrite;						// variaveis de post - sobrescrever arquivos
//	 static FileDialog fileDialog;                  // Seletor de arquivos
	 static FileDialog dlg; 						// Seletor de arquivos
	 static String userHome = System.getProperty( "user.home" ); // Diretorio home
	 static String[] puser;
	 static String[] pserver;
	 static Display display = new Display ();
	 static Shell shell = new Shell(display);
	 static Text tPath;
	 
	static Image imgFile;
	static BufferedImage  imgSprite;
	static Image imgReport;
	static Image imgADHOC;
	static Image imgADHOCSaiku;
	static Image imgAnalysis;
	static Image imgXaction;	
	static Image imgCDA;
	static Image imgCDE;
	static Image imgDir;
	static Image imgReporti;
	static Image imgXanalyzer;
	static Image imgUrl;	
	 
/**
 *  
 * @param args
 */
public static void main (String [] args) {

    // Define shell and position
	shell.setText("Pentaho Unified Management Archiver");
	GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
	final Composite composite = new Composite(shell, SWT.NULL | SWT.BORDER);
    composite.setLayout(new GridLayout(2,false));
    
    // Get screen position 
    Monitor primary = display.getPrimaryMonitor();
    Rectangle bounds = primary.getBounds();
    Rectangle rect = shell.getBounds();
    x = bounds.x + (bounds.width - rect.width) / 2;
    y = bounds.y + (bounds.height - rect.height) / 2;
    shell.setLocation(x, y);	
	
    ///
	// Config file load
    ///
    Properties prop = new Properties();
	try { 
		prop.load(Puma.class.getClassLoader().getResourceAsStream("config.properties"));
	
		final String pservers = prop.getProperty("pserver").toString();
		final String pusers = prop.getProperty("puser").toString();
		puser = pusers.split(";");
		pserver = pservers.split(";");
		
	} catch (IOException ex) {
		 errDiag("ERROR - unable to read config file!", "Unable to read 'config.properties' \n"+ex + " \n"+ errorCrtl+"\n", shell);
		 ex.printStackTrace();
	} catch (NullPointerException ex) {	
		 errDiag("ERROR - unable to read config file!", "Unable to read 'config.properties' \n"+ ex + " \n"+ errorCrtl+"\n", shell);
		 ex.printStackTrace();
	}      
    // MenuBar 
    Label label = new Label(shell, SWT.CENTER);
    label.setBounds(shell.getClientArea());
    Menu menuBar = new Menu(shell, SWT.BAR);
    
    // Menu file 
    MenuItem fileMenuHeader = new MenuItem(menuBar, SWT.CASCADE);
    fileMenuHeader.setText("&File");

    Menu fileMenu = new Menu(shell, SWT.DROP_DOWN);
    fileMenuHeader.setMenu(fileMenu);
    
    // Menu Security    
    MenuItem SecurityItem = new MenuItem(fileMenu, SWT.PUSH);
    SecurityItem.setText("&Security");

    // Menu Explorer (pentaho solution tree view);
    MenuItem ExplorerItem = new MenuItem(fileMenu, SWT.PUSH);
    ExplorerItem.setText("&Explorer");
    
    // Menu Publish
    MenuItem fileSaveItem = new MenuItem(fileMenu, SWT.PUSH);
    fileSaveItem.setText("&Publish");

    // Menu exite - close application
    MenuItem fileExitItem = new MenuItem(fileMenu, SWT.PUSH);
    fileExitItem.setText("E&xit");
    fileExitItem.addListener(SWT.Selection, new Listener() {
    	public void handleEvent(Event event) {
    		shell.close();
    		display.dispose();
    	}
    });
    
    // Menu config 
    MenuItem configMenuHeader = new MenuItem(menuBar, SWT.CASCADE);
    configMenuHeader.setText("&Config");
    
    Menu configMenu = new Menu(shell, SWT.DROP_DOWN);
    configMenuHeader.setMenu(configMenu);
    
    // Menu add certificate
    MenuItem certItem = new MenuItem(configMenu, SWT.PUSH);
    certItem.setText("&Add certificate");
    
    // Menu Help
    MenuItem helpMenuHeader = new MenuItem(menuBar, SWT.CASCADE);
    helpMenuHeader.setText("&Help");

    Menu helpMenu = new Menu(shell, SWT.DROP_DOWN);
    helpMenuHeader.setMenu(helpMenu);

    //Menu about
    MenuItem AboutHelpItem = new MenuItem(helpMenu, SWT.PUSH);
    AboutHelpItem.setText("&About");
    AboutHelpItem.addListener(SWT.Selection, new Listener() {
    	public void handleEvent(Event event) {
    		   final Shell dialog =
    			          new Shell(shell, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
    			        // dialog.setLayout(new RowLayout());
    		   final Text AboutHelpText = new Text(dialog, SWT.NONE | SWT.WRAP);
    		   AboutHelpText.setText("Unofficial General purpose publishing tool for Pentaho BI Server \n" +
    		   		"Its based on Pentaho publisher API \n" +
    		   		"Intended to avoid filesystem access in the process of: \n" +
    		   		"- 'xaction';\n" +
    		   		"- CTools (CDA, CDF, CDE); \n" +
    		   		"- CSS,HTML,XML; \n" +
    		   		"- Image files (jpg,gif,png); \n" +
    		   		"Tested on Bi Server 3.10, 4.x, CE and EE \n\n" +    		  
    		   		"Developed by: David da Guia Carvalho <david.carvalho@serpro.gov.br> \n" +
    		   		"ver. 0.6-alpha \n");
    		   GridData	data = new GridData();
    			data.grabExcessHorizontalSpace = true;
    			data.minimumWidth = 300;
    			AboutHelpText.setLayoutData(data);
    			AboutHelpText.setEditable(false);
    					AboutHelpText.pack();
    					dialog.setLocation(x, y);
    					dialog.setText("About: ");
    			        dialog.pack();
    			        dialog.open();    			        

    	}
    });
    
    ///
    // Main Form
    // Server, user and password
    ///
	  Label SrvLabel = new Label (composite, SWT.NONE);
	  SrvLabel.setText ("Server: ");	  
      final Combo tServerURL = new Combo(composite, SWT.DROP_DOWN);
      // Add server from conf.
      for (int i=0; i < pserver.length; i++){
    	  tServerURL.add("http://"+pserver[i]);
      }
      Label SrvsampleLabel = new Label (composite, SWT.NONE);
      SrvsampleLabel.setText ("Exemplo: http://localhost:8080/pentaho");
      data = new GridData();
	  data.grabExcessHorizontalSpace = true;
	  data.minimumWidth = 300;
	  data.horizontalSpan = 2;
      SrvsampleLabel.setLayoutData(data);
      	  
	  Label UserLabel = new Label (composite, SWT.NONE);
	  UserLabel.setText ("User: ");	  
	  final Combo tServerUserId  = new Combo(composite, SWT.DROP_DOWN);
	   for (int i=0; i < puser.length; i++){
		   tServerUserId.add(puser[i]);
	      }
	  data = new GridData();
	  data.grabExcessHorizontalSpace = true;
	  data.minimumWidth = 250;
	  tServerUserId.setLayoutData(data);
	  
	  Label PassLabel = new Label (composite, SWT.NONE);
	  PassLabel.setText ("Password: ");	  
	  final Text tServerPassword = new Text (composite, SWT.BORDER | SWT.PASSWORD);
	  data = new GridData();
	  data.grabExcessHorizontalSpace = true;
	  data.minimumWidth = 200;
	  tServerPassword.setLayoutData (data);

	  final Button buttonClose = new Button(composite, SWT.PUSH);
	  buttonClose.setText("C&lose");
	  buttonClose.addListener(SWT.Selection, new Listener() {
	      public void handleEvent(Event event) {
	    	  shell.close();
	      }
	      });
	///
	// Listener
	///
    ExplorerItem.addListener(SWT.Selection, new Listener() {
    	public void handleEvent(Event event) {
    		 if(tServerURL.getText().equals("") || tServerURL.getText().isEmpty() 
    		    		|| tServerUserId.getText().equals("") || tServerUserId.getText().isEmpty()
    		    		|| tServerPassword.getText().equals("") || tServerPassword.getText().isEmpty()
    		    	   ) {
    		    		  warningDiag("Warning! Fields required","Please fill in the fields!\n * Server;\n * User;\n * Password;", shell);
    		    	  } else {
    		    		  	dirTree(shell, tServerURL.getText(), tServerUserId.getText(), tServerPassword.getText(), false);
    		    	  }    		
    	}
    });
	
    fileSaveItem.addListener(SWT.Selection, new Listener() {
    	public void handleEvent(Event event) {
   		 if(tServerURL.getText().equals("") || tServerURL.getText().isEmpty() 
   		    		|| tServerUserId.getText().equals("") || tServerUserId.getText().isEmpty()
   		    		|| tServerPassword.getText().equals("") || tServerPassword.getText().isEmpty()
   		    	   ) {
   		    		  warningDiag("Warning! Fields required","Please fill in the fields!\n * Server;\n * User;\n * Password;", shell);
   		    	  } else {
   		    		publishFront(tServerURL.getText(), tServerUserId.getText(), tServerPassword.getText());
   		    	  }    		
    	}
    });
	
    SecurityItem.addListener(SWT.Selection, new Listener() {
    	public void handleEvent(Event event) {
    		if(tServerURL.getText().equals("") || tServerURL.getText().isEmpty() 
   		    		|| tServerUserId.getText().equals("") || tServerUserId.getText().isEmpty()
   		    		|| tServerPassword.getText().equals("") || tServerPassword.getText().isEmpty()
   		    	   ) {
   		    		  warningDiag("Warning! Fields required","Please fill in the fields!\n * Server;\n * User;\n * Password;", shell);
   		    	  } else {    		
   		    		  String response = RepReader.getSecurity(tServerURL.getText(), tServerUserId.getText(), tServerPassword.getText());
   		    		  System.out.println(response);   		    		  
    		}
    	}
    });
    certItem.addListener(SWT.Selection, new Listener() {
    	public void handleEvent(Event event) {
    	warningDiag("Warning UNDER DEVELOPMENT"," [UNDER DEVELOPMENT]\n Add SSL certificate to the KeyStore\n Allow for easy usage on https!\n result in global Java ssl change, effect all client apps!\n This feature alter the java keystore use keytools for validation\nExample: .../jdk/jre/lib/security keytool -list -keystore cacerts\n", shell);
    	certFront(shell);	
    	}
    });
    
	///
    // Pack it up 
    ///
    shell.setMenuBar(menuBar);
	composite.pack();
	label.pack();
	shell.pack();
	shell.open ();
	while (!shell.isDisposed ()) {
		if (!display.readAndDispatch ()) display.sleep ();
	}
	display.dispose ();
}

/**
 * Publish screen with form fields: 
 * 
 * - publish password; 
 * 
 * - Solution dir selection (over tree);
 * 
 * - local file selection (multiple file in a single dir);
 * 
 * @param tServerURL - Bi Server URL 
 * @param tServerUserId - Bi Server user
 * @param tServerPassword - password
 * @see {@link #dirTree(Shell, String, String, String, boolean)} for references over solution selection
 * @see {@link #getChldTree(Node, TreeItem, String, boolean)} for references over node child list
 * 
 */
private static void publishFront(final String tServerURL, final String tServerUserId, final String tServerPassword) {
	  final Shell shellPublish = new Shell(shell);
	  shellPublish.setLayout(new GridLayout());
	  shellPublish.setLocation(x, y);
	  shellPublish.setText("PUMA - "+tServerURL);
	  GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
		final Composite composite = new Composite(shellPublish, SWT.NULL | SWT.BORDER);
	    composite.setLayout(new GridLayout(3,false));

	  Label separator = new Label(composite, SWT.HORIZONTAL | SWT.SEPARATOR);
	  separator.setText ("teste");
	  data = new GridData(GridData.FILL_HORIZONTAL);	  
	  data.horizontalSpan = 3;
	  separator.setLayoutData(data);
	  
	  Label PublisherLabel = new Label (composite, SWT.NONE);
	  PublisherLabel.setText ("Publicação: ");	
	  data = new GridData();
	  data.grabExcessHorizontalSpace = true;
	  data.horizontalSpan = 3;
	  PublisherLabel.setLayoutData(data);
	  	  
	  Label PathLabel = new Label (composite, SWT.NONE);
	  PathLabel.setText ("Path (Solution): ");	  
	  tPath = new Text (composite, SWT.BORDER);
	  data = new GridData();
	  data.grabExcessHorizontalSpace = true;
	  data.widthHint = 200;
	  tPath.setLayoutData(data);	  
	  Image image = new Image(display,"img/magnifier.png"); 
	  Button buttonSearchPath = new Button(composite, SWT.PUSH | SWT.ICON_SEARCH);
	  buttonSearchPath.setImage(image);
	  data = new GridData();
	  data.grabExcessHorizontalSpace = true;
	  data.horizontalAlignment = GridData.HORIZONTAL_ALIGN_BEGINNING;
	  buttonSearchPath.setLayoutData(data);
	  buttonSearchPath.addListener(SWT.Selection, new Listener() {
	      public void handleEvent(Event event) {
	    		 if(tServerURL.equals("") || tServerURL.isEmpty() 
	    		    		|| tServerUserId.equals("") || tServerUserId.isEmpty()
	    		    		|| tServerPassword.equals("") || tServerPassword.isEmpty()
	    		    	   ) {
	    		    		  warningDiag("Warning! Fields required","Please fill in the fields!\n * Server;\n * User;\n * Password;", shellPublish);
	    		    	  } else {
	    		    		  dirTree(shellPublish, tServerURL, tServerUserId, tServerPassword,true);
	    		    	  }  
	      }
	  	});
	  	  
	  Label PubLabel = new Label (composite, SWT.NONE);
	  PubLabel.setText ("Publisher password: ");	  
	  final Text tPublishPassword = new Text (composite, SWT.BORDER | SWT.PASSWORD);
	  tPublishPassword.setText("publisher");
	  data = new GridData();
	  data.grabExcessHorizontalSpace = true;
	  data.minimumWidth = 200;
	  data.horizontalSpan = 2;
	  tPublishPassword.setLayoutData (data);
	    
		final Table table = new Table(composite, SWT.MULTI | SWT.BORDER | SWT.FULL_SELECTION);
		table.setLinesVisible (true);
		table.setHeaderVisible (true);
		GridData ldata = new GridData(SWT.FILL, SWT.FILL, true, true);
		ldata.horizontalSpan = 3;
		ldata.heightHint = 100;
		ldata.widthHint = 400;
		table.setLayoutData(ldata);			
		String[] titles = {"Caminho","Arquivos"};
		for (int i=0; i<titles.length; i++) {
			TableColumn column = new TableColumn (table, SWT.NONE);
			column.setText (titles[i]);
		}	
		for (int i=0; i<titles.length; i++) {
			table.getColumn(i).pack();
		}
  
	  Button buttonSelectFile = new Button(composite, SWT.PUSH);
	  	buttonSelectFile.setText("Select a file");
	  	buttonSelectFile.addListener(SWT.Selection, new Listener() {
	      public void handleEvent(Event event) {
	    	  
	    	  ////
	          // Files selection
	    	  ////
	          dlg = new FileDialog(shellPublish, SWT.MULTI);
	          dlg.setFilterNames(filterNames);
	          dlg.setFilterExtensions(acceptedFiles);
	          String fn = dlg.open();
	          if (fn != null) {
	            StringBuffer buf = new StringBuffer();
	            String[] files = dlg.getFileNames();
	            table.clearAll();
	            table.removeAll();
	            // labelFile.setText("");
	            for (int i = 0, n = files.length; i < n; i++) {
	              buf.append(dlg.getFilterPath());
	              if (buf.charAt(buf.length() - 1) != File.separatorChar) {
	                buf.append(File.separatorChar);
	              }
	              buf.append(files[i]);
	              buf.append(" ");
	              
	              TableItem item = new TableItem (table, SWT.NONE);
	    			item.setText (0, dlg.getFilterPath());
	    			item.setText (1, files[i]);	              
	            }
	            // labelFile.setText(buf.toString());
	          }
	    	 ///	    	  	    	  
	    	    while (!shellPublish.isDisposed()) {
	    	      if (!display.readAndDispatch())
	    	        display.sleep();
	    	    }
	    	    display.dispose();
	      }	    	  
	    });
	  	data = new GridData();
	  	data.horizontalSpan = 3;
	  	buttonSelectFile.setLayoutData(data);
	
	final Button buttonPublisher = new Button(composite, SWT.PUSH);
	buttonPublisher.setText("Publish");
	buttonPublisher.addListener(SWT.Selection, new Listener() {
      public void handleEvent(Event event) {
    	  /* seleciona os dados do form */
    	  String Url = tServerURL;//.getText();
     	  publishURL = Url+"/"+"RepositoryFilePublisher";
    	  publishPath = tPath.getText();
    	  serverUserid = tServerUserId;//.getText();
    	  serverPassword = tServerPassword;//.getText();
    	  publishPassword = tPublishPassword.getText();
    	      	  
    	  File[] publishFiles = new File[500];
    	  String[] files = dlg.getFileNames();    	
    	  
    	  for(int i=0; i < files.length;i++) {    	  
    		  System.out.println("Arquivos: "+dlg.getFilterPath()+File.separator+files[i]+";\n");

    		  try { 
    		 publishFiles[i] = new File( dlg.getFilterPath()+File.separator+files[i] );
    		  } catch (Exception ex) {
    			  errDiag("ERROR - File read!", "Error during file read! "+ ex + " \n"+ errorCrtl, shell);        		      		  
        	  }
        	  
    	  }
    	  System.out.println("Tamanho de lista de arquivos"+publishFiles.length);

    	    List<File> listFiles = new ArrayList<File>();

    	    for(File s : publishFiles) {
    	       if(s != null && s.isFile()) {
    	    	   listFiles.add(s);
    	       }
    	    }

    	    publishFiles = listFiles.toArray(new File[listFiles.size()]);
    	  
    	  ///
    	  // Executa envio para o pentaho
    	  // Inicia processo de publicacao propriamente dito
    	  ///
    	  try {    	
    		 if (	!publishURL.isEmpty() &&
    				!publishPath.isEmpty() &&
    				publishFiles[0].isFile() &&
    				!publishPassword.isEmpty() &&
    				!serverUserid.isEmpty() &&
    				!serverPassword.isEmpty()
    		  ){ // Verifica se campos estao preenchidos! 
    			 @SuppressWarnings("static-access")
			 	String response = publisher.publish(publishURL, publishPath, publishFiles, publishPassword, serverUserid, serverPassword, true, false);
        	 	System.out.println("Final da operacao de publicacao: \n"+response);
        	 	if ( response.equals("3") ) {
        	 		warningDiag("Success on file publish","Success!\nFile :"+publishFiles[0]+"\n Was published at: "+Url+"\n", shellPublish);
        	 	} else {
        	 		errDiag("ERROR - Publishing files!","Error publishing File! \n Response: "+response+"\n", shellPublish);
        	 	}
    		 } else {
    			 warningDiag("Warning! Fields required","Please fill in the fields!\n * Server;\n * User;\n * Password;\n * Publisher password; \n * File;", shellPublish);
    		 }
    	  	} catch (Exception ex) {  
    	  		errDiag("ERROR - Publishing files!", "publishing error: \n"+ ex + " \n"+ errorCrtl+"\n", shell);    		      		
    	  	}
      }
     
    });
	
	  final Button buttonClose = new Button(composite, SWT.PUSH);
	  buttonClose.setText("C&lose");
	  buttonClose.addListener(SWT.Selection, new Listener() {
	      public void handleEvent(Event event) {
	    	  shellPublish.close();
	      }
	      });
	
  shellPublish.pack();
  shellPublish.open ();
  
}
	  
/**
* popUp for Pentaho solution tree (Icones baseados na API do pentaho [CE / EE]) 		
* @param serverUrl
* @param serverUserid
* @param serverPassword
* @param selecao true/false retorna se ativo retorna valor selecionado!
* @see {@link #getChldTree(Node, TreeItem, String, boolean)} for references of child node list
* 
*/	 
public static void dirTree(Shell parentShell, final String serverUrl,final String serverUserid, final String serverPassword, final boolean selecao) {
	    final Shell treeShell = new Shell(parentShell);
	    treeShell.setText("Browse at: "+serverUrl);
	    final Composite composite = new Composite(treeShell, SWT.NULL | SWT.BORDER | SWT.RESIZE);
	    composite.setLayout(new GridLayout());
	    
	// Images
	try {
	   imgSprite = ImageIO.read(new File("img/icons.png")); // Nao deu tempo de implementar leitura de icones pelo sprite... nem sei se vale o custo 
	   imgFile = new Image(null, new FileInputStream("img/folderIcon.png"));
	   imgCDA =  new Image(null, new FileInputStream("img/cdaFileType.png"));
	   imgCDE =  new Image(null, new FileInputStream("img/wcdfFileType.png"));
	   imgAnalysis =  new Image(null, new FileInputStream("img/analysisview.png"));
	   imgXaction =   new Image(null, new FileInputStream("img/xaction.png"));
	   imgADHOC =  new Image(null, new FileInputStream("img/report.png"));
	   imgADHOCSaiku =  new Image(null, new FileInputStream("img/saiku_16.png"));
	   imgReport =  new Image(null, new FileInputStream("img/prptFileType.png"));
	   imgDir = new Image(null, new FileInputStream("img/folderIcon.png"));
	   imgReporti =  new Image(null, new FileInputStream("img/prptiFileType.png"));
	   imgXanalyzer = new Image(null, new FileInputStream("img/analysis_report_file_icon.png"));
	   imgUrl = new Image(null, new FileInputStream("img/url.png"));
	   // imgDir = new Image(display,convertToSWT(imgSprite.getSubimage( 500, 60, 16, 16)));	   
	} catch (FileNotFoundException exFile) {
		errDiag("ERROR - file not found!", "Error loading image"+exFile, treeShell);
		exFile.printStackTrace();
	} catch (IOException exIo) {
		errDiag("ERROR - file not found!", "Error loading image"+exIo, treeShell);
		exIo.printStackTrace();
	}
	 
		// Estrutura do objeto arvore em colunas	 
	    final Tree tree = new Tree(composite, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.RESIZE);
	    tree.setHeaderVisible(true);
	    
	    // action do tree	    
	    final Menu treeMenu = new Menu(treeShell, SWT.POP_UP);
    if (selecao) {
		MenuItem treeMenuItemOpen = new MenuItem(treeMenu, SWT.PUSH);		
		treeMenuItemOpen.setText("Open");		      
		treeMenuItemOpen.addListener(SWT.Selection, new Listener() {
		    	public void handleEvent(Event event) {	    		
		    		 for (int i = 0; i < +tree.getSelection() .length; i++) { 
		 	          String myValue = tree.getSelection()[i].getText(3); // Pega terceira coluna (path)
		 	        	Puma.tPath.setText(myValue);	
		 	        	treeShell.setVisible(false);
		 	        	// treeShell.close(); // error disposed widget
		 	          }	 	        
		    	}
		    });  
    }
		
		MenuItem treeMenuItemDel = new MenuItem(treeMenu, SWT.SEPARATOR);
		treeMenuItemDel = new MenuItem(treeMenu, SWT.PUSH);
		treeMenuItemDel.setText("Delete");
		treeMenuItemDel.addListener(SWT.Selection, new Listener() {
	    	public void handleEvent(Event event) {	    	
	    	treeShell.close();	
	    	}
		});
				
		tree.addListener(SWT.MenuDetect, new Listener() {
		      public void handleEvent(Event event) {
		    	  tree.setMenu(treeMenu);
		      }
			});

	    // Formato do tree
	    GridData data = new GridData();
		data.grabExcessHorizontalSpace = true;
		data.minimumWidth = 600;
		data.minimumHeight = 700;
		data.heightHint = 700;
	    tree.setLayoutData(data);
		// colunas
	    TreeColumn column0 = new TreeColumn(tree, SWT.LEFT);
	    column0.setText("File");
	    column0.setWidth(300);
	    TreeColumn column1 = new TreeColumn(tree, SWT.LEFT);
	    column1.setText("Meta Name");
	    column1.setWidth(400);
	    TreeColumn column2 = new TreeColumn(tree, SWT.CENTER);
	    column2.setText("Is Directory");
	    column2.setWidth(60);
	    TreeColumn column3 = new TreeColumn(tree, SWT.RIGHT);
	    column3.setText("Visible");
	    column3.setWidth(50);
	    TreeColumn column4 = new TreeColumn(tree, SWT.RIGHT);
	    column4.setText("Path");
	    column4.setWidth(300);
	    
	  // Adiciona itens na arvore! Origem XML pentaho SOAP	    	 	  
	  String xml = RepReader.getSolutionsDir(serverUrl, serverUserid, serverPassword);
	  DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
   try {
		DocumentBuilder builder = factory.newDocumentBuilder();
	      InputSource is = new InputSource(new StringReader(xml));
	      Document doc = builder.parse(is);
	      doc.getDocumentElement().normalize();
	      NodeList nodeLst = doc.getElementsByTagName("file");	      
	     for (int s = 0; s < nodeLst.getLength(); s++) {
	        Node fstNode = nodeLst.item(s);       	        
	        if (fstNode.getNodeType() == Node.ELEMENT_NODE) {	        	
	           Element fstElmnt = (Element) fstNode;
	           Element parentElmnt = (Element) fstNode.getParentNode();
	           String isDir = fstElmnt.getAttributeNode("isDirectory").getValue().toString();
	           String isVisible = fstElmnt.getAttributeNode("visible").getValue().toString();
	           String dirMetaName = fstElmnt.getAttributeNode("localized-name").getValue().toString();
	           String dirName = fstElmnt.getAttributeNode("name").getValue().toString();
	           String parentOne = parentElmnt.getAttribute("name");
	           if ( isDir.equals("true") && parentOne.equals("") ) {
	        	   TreeItem item = new TreeItem(tree, SWT.NONE);
	        	   item.setText(new String[] { dirName, dirMetaName, isDir, isVisible, dirName});
	        	   item.setImage(imgDir);
	        	   /* add children */
	        	   getChldTree(fstElmnt,item,"", selecao);	        	 
	        	   }	        
	           }
	           	           
	        }	    	      
	} catch (ParserConfigurationException e) {
		errDiag("ERROR - file not found!", "Error loading image"+e, treeShell);
		e.printStackTrace();		
	} catch (SAXException e) {
		errDiag("ERROR - file not found!", "Error loading image"+e, treeShell);
		e.printStackTrace();		
	} catch (IOException e) {
		errDiag("ERROR - file not found!", "Error loading image"+e, treeShell);
		e.printStackTrace(); 
	}	     
   
    tree.pack();
	composite.pack();
	treeShell.pack();
	treeShell.open();	  
}

/**
* Recursive operation to deal with node child
* @param currNode Current node
* @param currItem Current item 
* @param fullItem full path of the item
* @param selecao Is folder selection 
* @return NodeList All child nodes
*/
private static NodeList getChldTree (final Node currNode, TreeItem currItem, String fullItem, boolean selecao) {
	  NodeList chldLst = currNode.getChildNodes();
	  if (chldLst.getLength() > 0) {
		  for ( int s = 0; s < chldLst.getLength(); s++) {
		   Node chldNode = chldLst.item(s); //Obtem posicao atual de node
		   Node parentChldNode = chldNode.getParentNode();
		   if (chldNode.getNodeType() == Node.ELEMENT_NODE) {
			   // getChldTree(chldNode, subitem,fullItem); // Chamada recursiva
			   Element chldElmnt = (Element) chldNode;
			   Element parentElmntNode = (Element) parentChldNode; 
			   String isDir = chldElmnt.getAttributeNode("isDirectory").getValue().toString();   			  
	           String isVisible = chldElmnt.getAttributeNode("visible").getValue().toString();
	           String itemMetaName = chldElmnt.getAttributeNode("localized-name").getValue().toString();
	           String itemName = chldElmnt.getAttributeNode("name").getValue().toString();
	        if (selecao == false || (selecao == true && isDir.equals("true"))){
			   TreeItem subitem = new TreeItem(currItem, SWT.NONE); // Gera subitem
	           // gerar path
	           String itemParentName = parentElmntNode.getAttributeNode("name").getValue().toString();
	           String[] separada = fullItem.split("/");
	           // System.out.println("Objeto: "+itemName+" - FullItem: "+fullItem);
	           String fullPath = "";

	           for ( int j = 0; j < separada.length; j++) { // Remove path de irma
	        	   if (itemParentName.equals(separada[j])) {
	        		//itemParentName = "";
	        		fullPath += separada[j]+"/";
	        		break;
	        	   } else {
	        		    fullPath += separada[j]+"/";
	        	   }
	           }
	           fullPath += itemName;
	           
	           if(fullItem.equals("")) {
	        	   fullItem = itemParentName + "/" + itemName;
	           } else {
	        	fullItem = fullPath;
	           }
	           
	           getChldTree(chldNode, subitem,fullItem, selecao); // recursive
	           subitem.setText(new String[] { itemName, itemMetaName, isDir, isVisible, fullItem});
	           ///
	           // Add image
	           ///
	           if (itemName.contains(".cda")){
	        	   subitem.setImage(imgCDA);
	           } else if(itemName.contains(".wcdf") || itemName.contains(".xcdf")) {	        	   
	        	   subitem.setImage(imgCDE);
	           } else if(itemName.contains(".analysisview.xaction") || itemName.contains("xdash")) {
	        	   subitem.setImage(imgAnalysis);
	           } else if(itemName.contains(".waqr.xaction")) {	        	   
	        	   subitem.setImage(imgADHOC);	   
	           } else if(itemName.contains(".adhoc") || itemName.contains(".saiku")) {	        	   
	        	   subitem.setImage(imgADHOCSaiku);	        	  	        	   
	           } else if(itemName.contains(".xaction")) {
	        	   subitem.setImage(imgXaction);
	           } else if(itemName.contains(".prpti")) {	        	   
	        	   subitem.setImage(imgReporti);		        	   
	           } else if(itemName.contains(".prpt")) {	        	   
	        	   subitem.setImage(imgReport);        	   
	           } else if(itemName.contains(".url") || itemName.contains(".html") || itemName.contains(".htm")) {
	        	   subitem.setImage(imgUrl);
	           } else if(itemName.contains(".xanalyzer")) {	        	   
	        	   subitem.setImage(imgXanalyzer);       	        	   	        	   
	           } else { 
	        	   subitem.setImage(imgFile);
	           }
	        }// if seleco && isdir
		   }	// element 		  
	   	} // for	        	   			
	  } // chldlist >0 
	  return null;
}

/**
 * GUI for add ssl certificate to jvm (keystore) as trusted 
 * @param shell
 */
private static void certFront(Shell shell) {
	  final Shell sh = new Shell(shell);
	  	sh.setText("Warning UNDER DEVELOPMENT");
	  	sh.setLayout(new GridLayout());
	  	sh.setLocation(x, y);
	  final Composite composite = new Composite(sh, SWT.NULL | SWT.BORDER);
	  	composite.setLayout(new GridLayout(2,false));
	  GridData data;
	  
	  Label HostLabel = new Label (composite, SWT.NONE);
	  	HostLabel.setText ("Host: ");	  
	  final Text tHost = new Text (composite, SWT.BORDER);
	  	data = new GridData();
	  	data.grabExcessHorizontalSpace = true;
	  	data.minimumWidth = 200;
	  	tHost.setLayoutData (data);
	  
	  Label PortLabel = new Label (composite, SWT.NONE);
	  	PortLabel.setText ("Port: ");	  
	  final Text tPort = new Text (composite, SWT.BORDER);
	  	data = new GridData();
	  	data.grabExcessHorizontalSpace = true;
	  	data.minimumWidth = 200;
	  	tPort.setLayoutData (data);
	  	
	  Label PassLabel = new Label (composite, SWT.NONE);
	  	PassLabel.setText ("Password: ");	  
	  final Text tStorePassword = new Text (composite, SWT.BORDER | SWT.PASSWORD);
	  	data = new GridData();
	  	data.grabExcessHorizontalSpace = true;
	  	data.minimumWidth = 200;
	  	tStorePassword.setLayoutData (data);
	  
	  // Fim
	  final Button buttonAdd = new Button(composite, SWT.PUSH);
	  	buttonAdd.setText("Add");
	  	buttonAdd.addListener(SWT.Selection, new Listener() {
	      public void handleEvent(Event event) {
	    	  //
	    	  String host = tHost.getText();
	    	  int port;
	    	  try {
	    	  port = Integer.parseInt(tPort.getText());
	    	  } catch (NumberFormatException ex) {
	    		  System.out.println("Expected error at number validation on field port!");
	    		 port = -1;
	    	  }
	    	  char[] passphrase = tStorePassword.getText().toCharArray();	    	  
	    	  //
	    	  try {
	    		  if (	host.isEmpty() ||
	    				port < 0 || 
	      				passphrase == null
	      		  ){ // Verifica se campos estao preenchidos!
	    			  warningDiag("Warning! Fields required","Please fill in curretly the fields!\n * Server;\n * Port;\n * Password (java store - suggestion changeit);\n", sh);	    			  
	    			  // InstallCert.addCert(host, port, passphrase);
	    		  } else {
	    			  
	    			  int response = InstallCert.addCert(host, port, passphrase);
	    			 // int response = 2;
	    			 switch (response) {
	    			 case 1: 
	    				 infoDiag("Success! certificate added","SUCCESS - Certificate added!\n", sh);
	    				 break;
	    			 case 2:
	    				 warningDiag("Already exist!","Certificate is already trusted\n",sh);
	    				 break;
	    			 case 3:
	    				  errDiag("ERROR - Add certificate","ERROR Adding certificate\n",sh);
	    				  break;
	    			 }
	    			  //// warningDiag("Warning! Fields required","Please fill in the fields!\n * Server;\n * Port;\n * Password (java store - sugestion changeit);\n", sh);
	    		  }
	    	  } catch (Exception ex) {
	    		  errDiag("ERROR - Add certificate","ERROR Adding certificate\n"+ex+"\n",sh);
	    	  }
	      }
	    });
	  final Button buttonClose = new Button(composite, SWT.PUSH);
	  	buttonClose.setText("C&lose");
	  	buttonClose.addListener(SWT.Selection, new Listener() {
	      public void handleEvent(Event event) {
	    	  sh.close();
	      }
	     });
	  composite.pack();
	  sh.pack();
	  sh.open ();
	  
}
///
// Dialogs
///
/**
 * Info dialog (popup) 
 * @param title
 * @param msg
 * @param shell
 */
public static void infoDiag(String title, String msg, Shell shell){
	final MessageBox box = new MessageBox(shell, SWT.ICON_INFORMATION | SWT.OK);
	  box.setText(title);
	  box.setMessage(msg);
	  box.open ();	  
}
/**
 * Warning dialog (popup)
 * @param title
 * @param msg
 * @param shell
 */
public static void warningDiag(String title, String msg, Shell shell){
	final MessageBox box = new MessageBox(shell, SWT.ICON_WARNING | SWT.OK);
	  box.setText(title);
	  box.setMessage(msg);
	  box.open();	  
}

/**
 * Error dialog (popup)
 * @param title
 * @param msg
 * @param shell
 */
public static void errDiag(String title, String msg, Shell shell){
	final MessageBox box = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
	  box.setText(title);
	  box.setMessage(msg);
	  box.open ();	  
}

}// Class