package com.mycompany.structotest;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.Writer;
import static java.lang.System.console;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.io.FileUtils;
import org.codehaus.plexus.archiver.tar.TarGZipUnArchiver;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class FXMLController implements Initializable {
    
    final static int BUFFER_SIZE = 2048;

    
    @FXML
    private Label label;
    
    @FXML
    private TabPane terminalTabPane;
    
    @FXML
    private TextArea textArea;
    
    
    @FXML
    private void handleButtonAction(ActionEvent event) {
        System.out.println("You clicked me!");
        label.setText("Hello World!");
        //check for Update
        
    }
    
    
    
    public class Console extends OutputStream {
        private TextArea console;

        public Console(TextArea console) {
            this.console = console;
        }

        public void appendText(final String valueOf) {
           
            Platform.runLater(new Runnable() {
                public void run() {
                    console.appendText(valueOf);
                }
            });
        }

        public void write(int b) throws IOException {
            appendText(String.valueOf((char)b));
        }
        
        public void write(String b) throws IOException {
            appendText(b);
        }
    }
    
    
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        
        try {
            Console console = new Console(textArea);
            PrintStream ps = new PrintStream(console);
            Runtime.getRuntime().exec("chmod 775 /Users/gauravsharma/structo/g.sh");
           String[] command = {"/Users/gauravsharma/structo/g.sh"};   
           //String[] command = {"pwd"};
            Process process = Runtime.getRuntime().exec(command);
            BufferedReader reader = new BufferedReader(new InputStreamReader(        
                process.getInputStream()));                                          
            String s;                                                                
            while ((s = reader.readLine()) != null) {                                
              System.out.println("Script output: " + s);   
              for (char c : s.toCharArray()) {
                    console.write(c);
                }
              for (char c : "\n".toCharArray()) {
                    console.write(c);
                }
                ps.close();
            }
            
            
        } catch (IOException ex) {
            Logger.getLogger(FXMLController.class.getName()).log(Level.SEVERE, null, ex);
        }
        
//        TerminalBuilder terminalBuilder = new TerminalBuilder();
//        TerminalTab terminal = terminalBuilder.newTerminal();
//
//        terminalTabPane.getTabs().add(terminal);
//        try {
//             terminal.command("pwd");
//            
//            // TODO
//            // Creating /home/structo directory structure 
//        } catch (InterruptedException ex) {
//            Logger.getLogger(FXMLController.class.getName()).log(Level.SEVERE, null, ex);
//        }
        
        /*
        1. Check if version.txt exists
            1.a if yes then check for update from the update api
            1.b if no then create the folder structure -> download the s3 files 
        */
        String versionFile = "/Users/gauravsharma/structo/version.txt";
        File f = new File(versionFile);
        if(f.exists() && !f.isDirectory()) {
            // do something
            System.out.println("file exists");
//            File sourceFile = new File("/Users/gauravsharma/structo/v1.3.8.tar.gz");
//            try {
//                InputStream iStream = new FileInputStream(sourceFile);
//                try {
//                    extractTarGZ(iStream);
//                } catch (IOException ex) {
//                    Logger.getLogger(FXMLController.class.getName()).log(Level.SEVERE, null, ex);
//                }
//            } catch (FileNotFoundException ex) {
//                Logger.getLogger(FXMLController.class.getName()).log(Level.SEVERE, null, ex);
//            }
            
            //readFile_Directory(versionFile);
        } else {
            try {   
            copyFile_Directory("version.txt", "/Users/gauravsharma/structo", "version1.txt");
            //Get request from API to find the current version of the file to be downloaded
            String updateResponse = checkUpdateService();
            System.out.println(updateResponse);
            //save the response in version.txt
            try (Writer writer = new BufferedWriter(new OutputStreamWriter(
              new FileOutputStream("/Users/gauravsharma/structo/version.txt"), "utf-8"))) {
                writer.write(updateResponse);
             }
            //read the version
            String versionNo  = readFile_Directory(versionFile);
            
            //Download the file : 
            String URL = "https://s3-ap-southeast-1.amazonaws.com/devstructobuilds/release-";
            String versionFileName = versionNo +".tar.gz";
            downloadUpdateFiles(URL, versionFileName);
            //untar the archive
            //untarRelease(new File("/Users/gauravsharma/structo/" + versionFileName), new File ("/Users/gauravsharma/structo"));    
            
            
            } catch (IOException e) {
                System.out.println(e);
            }
        }
    }    
    
    
    public void extractTarGZ(InputStream in) throws IOException {
    GzipCompressorInputStream gzipIn;
        gzipIn = new GzipCompressorInputStream(in);
    try (TarArchiveInputStream tarIn = new TarArchiveInputStream(gzipIn)) {
        TarArchiveEntry entry = new TarArchiveEntry("/Users/gauravsharma/structo/");

        while ((entry = (TarArchiveEntry) tarIn.getNextEntry()) != null) {
            /** If the entry is a directory, create the directory. **/
            if (entry.isDirectory()) {
                File f = new File("/Users/gauravsharma/structo/" + entry.getName());
                boolean created = f.mkdir();
                if (!created) {
                    System.out.printf("Unable to create directory '%s', during extraction of archive contents.\n",
                            f.getAbsolutePath());
                }
            } else {    
                int count;
                byte data[] = new byte[BUFFER_SIZE];
                System.out.println(entry.getName());
                FileOutputStream fos = new FileOutputStream("/Users/gauravsharma/structo/" + entry.getName(), false);
                try (BufferedOutputStream dest = new BufferedOutputStream(fos, BUFFER_SIZE)) {
                    while ((count = tarIn.read(data, 0, BUFFER_SIZE)) != -1) {
                        dest.write(data, 0, count);
                    }
                    dest.close();
                }
            }
        }

        tarIn.close();
        System.out.println("Untar completed successfully!");
    }   catch (IOException ex) {
            Logger.getLogger(FXMLController.class.getName()).log(Level.SEVERE, null, ex);
        }
}
    
//    public static void untarRelease(File sourceFile, File destDir) {
//        TarGZipUnArchiver ua = new TarGZipUnArchiver();
//        ua.setSourceFile(sourceFile);
//        //destDir.mkdirs();
//        ua.setDestDirectory(destDir);
//        ua.extract();
//        System.err.println("untar success");
//    }
    
    public static void downloadUpdateFiles(String urlToDownload, String versionFileName) {
            File dstFile = new File("/Users/gauravsharma/structo/" + versionFileName);
            // check the directory for existence.
//            String dstFolder = "/Users/gauravsharma/structo/latest.";
//            if(!(dstFolder.endsWith(File.separator) || dstFolder.endsWith("/")))
//                dstFolder += File.separator;
//
//            // Creates the destination folder if doesn't not exists
//            dstFile = new File(dstFolder);
//            if (!dstFile.exists()) {
//                dstFile.mkdirs();
//            }
            try {
                URL url = new URL(urlToDownload +  versionFileName);
                FileUtils.copyURLToFile(url, dstFile);
                TarGZipUnArchiver ua = new TarGZipUnArchiver();
                ua.setSourceFile(dstFile);
                ua.setDestDirectory(new File("/Users/gauravsharma/structo/"));
                ua.extract();
            } catch (Exception e) {
                System.err.println(e);
            }
    }
    
    public static String readFile_Directory(String filePath) {
        
        JSONParser parser = new JSONParser();

        try {

            Object obj = parser.parse(new FileReader(filePath));

            JSONObject jsonObject = (JSONObject) obj;
            System.out.println(jsonObject);

            String name = (String) jsonObject.get("version");
            System.out.println(name);
            return name;

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }   
        return null;
    }
    public static void copyFile_Directory(String origin, String destDir, String destination) throws IOException {

    Path FROM = Paths.get(origin);
    Path TO = Paths.get(destination);
    File directory = new File(String.valueOf(destDir));

    if (!directory.exists()) {
        directory.mkdir();
        System.out.println("make directory");
    }
}
    
    public static String checkUpdateService() {
          try {

		URL url = new URL("http://dev.cloud.structo3d.com/versions/latest");
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setRequestMethod("GET");
		conn.setRequestProperty("Accept", "application/json");

		if (conn.getResponseCode() != 200) {
			throw new RuntimeException("Failed : HTTP error code : "
					+ conn.getResponseCode());
		}

		BufferedReader br = new BufferedReader(new InputStreamReader(
			(conn.getInputStream())));

		String output;
		System.out.println("Output from Server .... \n");
		while ((output = br.readLine()) != null) {
			//System.out.println(output);
                        return output;
		}

		conn.disconnect();

	  } catch (MalformedURLException e) {

		e.printStackTrace();

	  } catch (IOException e) {

		e.printStackTrace();

	  }
         return null;   
    }
    
}
