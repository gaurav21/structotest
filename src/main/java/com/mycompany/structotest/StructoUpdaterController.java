/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.structotest;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
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
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextArea;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.io.FileUtils;
import org.codehaus.plexus.archiver.tar.TarGZipUnArchiver;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 *
 * @author gauravsharma
 */
public class StructoUpdaterController implements Initializable {

    //Static variable defined here can be moved to constant file later
    final static int BUFFER_SIZE = 2048;
    final static String BASE_PATH = "/home/structo/";
    final static String VERSION_FILE = "version.txt";

    
    private String versionNumberAvailable;
    
    @FXML
    private Label label;
    
    @FXML
    private Button button;
    
    @FXML
    private TextArea textArea;
    
    @FXML
    private ProgressIndicator downloader;
    
    @FXML
    private ProgressIndicator extracter;
    
    @FXML
    private ProgressIndicator setupIndicator;
    
    @FXML
    private Label downloaderFile;
    
    @FXML
    private Label extractorLabel;
    
    @FXML
    private Label setupLabel;
    
            
    
    
    @FXML
    private void handleButtonAction(ActionEvent event) {
        System.out.println("You clicked me!");
      //  label.setText("Hello World!");
        System.out.println(versionNumberAvailable + " version number to be installed");
        //check for Update
        if (!versionNumberAvailable.isEmpty()) {
        //System.out.println(versionNumberAvailable + " version number to be installed -->!");    
            System.out.println(versionNumberAvailable + " version number to be installed --> 12");    
            System.out.println(versionNumberAvailable + " version number to be installed");
            button.setText("Installing ...");
            button.setDisable(true);
            //check if release file already exists 
            
            
            
            //Download the release file : 
            String URL = "https://s3-ap-southeast-1.amazonaws.com/devstructobuilds/release-";
            String versionFileName = versionNumberAvailable +".tar.gz";
            
            File releaseFile = new File (BASE_PATH  + versionFileName);
            System.out.println("Release file exits " + releaseFile.exists());
            if (!releaseFile.exists()) {
//                downloadUpdateFiles(URL, versionFileName);
                  Thread downloadThread = new Thread();
                  downloadThread = downloadThread(URL,versionFileName, downloader,downloadThread);
                  downloaderFile.setText("Downloading");
                  //downloadThread.start();
                  Thread extractThread = new UnTarThread(downloadThread, versionFileName);
                  extractorLabel.setText("Extracting");
                  extractThread.start();
                  Thread setupThread = new UnTarThread(extractThread, null);
                  setupLabel.setText("Setting Up");
                  setupThread.start();
            } else {
                extractFiles();
            }

        } else // Fresh Install
        {
            extractFiles();
        }
        
    }
    
    
    @Override
    public void initialize(URL location, ResourceBundle resources) { 
        extractFiles ();
        //1.Check if version.txt exits 
        String versionFile = BASE_PATH + VERSION_FILE;
        File f = new File(versionFile);
        
        // Creates the destination folder if doesn't not exists
            File structoSetupFolders = new File(BASE_PATH + ".structo");
            if (!structoSetupFolders.exists()) {
                structoSetupFolders.mkdirs();
            }
            
            
            structoSetupFolders = new File(BASE_PATH + "bin");
            if (!structoSetupFolders.exists()) {
                structoSetupFolders.mkdirs();
            }
        
        if(f.exists() && !f.isDirectory()) {
            // Version File exits so output the current version
            System.out.println("file exists");
            String versionNo = readFile_Directory(versionFile);
            label.setText("Current Version - " + versionNo);
            
            //update logic
        } else {
            /*
                Fresh install
            */
            try {
                String updateResponse = checkUpdateService();
                System.out.println(updateResponse);
                //save the response in version.txt
                try (Writer writer = new BufferedWriter(new OutputStreamWriter(
                  new FileOutputStream(BASE_PATH + "temp-" +VERSION_FILE), "utf-8"))) {
                    writer.write(updateResponse);
                 }
                //read the version
                versionNumberAvailable = readFile_Directory(BASE_PATH + "temp-" +VERSION_FILE);
                
                //
                structoSetupFolders = new File(BASE_PATH + "bin/" + versionNumberAvailable);
                if (!structoSetupFolders.exists()) {
                    structoSetupFolders.mkdirs();
                }
                button.setText("Install - " + versionNumberAvailable);    
            } catch (IOException e) {
                System.out.println(e);
            }
            
        }
    }
    
    /*
    Read Version file and return version number
    */
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
    
    /*
    Console class to be used for showing script output 
    */
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
    
    public void extractTarGZ(InputStream in) throws IOException {
        extractorLabel.setText("Extracting");
        GzipCompressorInputStream gzipIn;
            gzipIn = new GzipCompressorInputStream(in);
        try (TarArchiveInputStream tarIn = new TarArchiveInputStream(gzipIn)) {
            TarArchiveEntry entry = new TarArchiveEntry(BASE_PATH);

            while ((entry = (TarArchiveEntry) tarIn.getNextEntry()) != null) {
                /** If the entry is a directory, create the directory. **/
                if (entry.isDirectory()) {
                    File f = new File(BASE_PATH + entry.getName());
                    boolean created = f.mkdir();
                    if (!created) {
                        System.out.printf("Unable to create directory '%s', during extraction of archive contents.\n",
                                f.getAbsolutePath());
                    }
                } else {    
                    int count;
                    byte data[] = new byte[BUFFER_SIZE];
                    System.out.println(entry.getName());
                    FileOutputStream fos = new FileOutputStream(BASE_PATH + entry.getName(), false);
                    try (BufferedOutputStream dest = new BufferedOutputStream(fos, BUFFER_SIZE)) {
                        while ((count = tarIn.read(data, 0, BUFFER_SIZE)) != -1) {
                            dest.write(data, 0, count);
                            
                        }
                        dest.close();
                        extracter.setProgress(1);
                    }
                }
            }

            tarIn.close();
            System.out.println("Untar completed successfully!");
        }   catch (IOException ex) {
                Logger.getLogger(FXMLController.class.getName()).log(Level.SEVERE, null, ex);
            }
    }
    
      public static void downloadUpdateFiles(String urlToDownload, String versionFileName) {
            File dstFile = new File(BASE_PATH + versionFileName);
            // check the directory for existence.
            try {
                URL url = new URL(urlToDownload +  versionFileName);
                FileUtils.copyURLToFile(url, dstFile);
                TarGZipUnArchiver ua = new TarGZipUnArchiver();
                ua.setSourceFile(dstFile);
                ua.setDestDirectory(new File(BASE_PATH));
                ua.extract();
            } catch (Exception e) {
                System.err.println(e);
            }
    }
      
      
    public class UnTarThread extends Thread {

        private Thread predecessor;
        private String fileName;

        public UnTarThread(Thread predecessor, String fileName) {
            this.predecessor = predecessor;
            this.fileName = fileName;
        }

        public void run() {
            if (predecessor != null && predecessor.isAlive()) {
                try {
                    predecessor.join();
                } catch (InterruptedException e) {}
            }
            //do your stuff
            if (fileName != null) {
                System.out.println("untar started");
                unTarGZip(fileName);
            } else {
                System.out.println("setup started");
                extractFiles();
            }   
        }
    }
    
    public void unTarGZip(String versionFileName) {
        //extract the release files 
                File sourceFile = new File(BASE_PATH + versionFileName);
                try {
                    InputStream iStream = new FileInputStream(sourceFile);
                    try {
                        extractTarGZ(iStream);
                    } catch (IOException ex) {
                        Logger.getLogger(FXMLController.class.getName()).log(Level.SEVERE, null, ex);
                    }
                } catch (FileNotFoundException ex) {
                    Logger.getLogger(FXMLController.class.getName()).log(Level.SEVERE, null, ex);
                }
                
    }
    
    
    public void extractFiles () {
         // execute the release files
         //setupLabel.setText("Setting Up");
       //  System.out.println("sjfbsdmfsbfs");
                try {
                    Console console = new Console(textArea);
                    PrintStream ps = new PrintStream(console);
                    InputStream setupProcess = Runtime.getRuntime().exec("chmod 775 " + BASE_PATH +"setup.sh").getInputStream();
                    InputStreamReader isr = new InputStreamReader(setupProcess);
                    BufferedReader buff = new BufferedReader (isr);

                    String line;
                    while((line = buff.readLine()) != null)
                        System.out.print(line);
                    
                    
                    
                    
                    
                    setupProcess = Runtime.getRuntime().exec("ls -ltra " + BASE_PATH).getInputStream();
                    isr = new InputStreamReader(setupProcess);
                    buff = new BufferedReader (isr);

                    
                    while((line = buff.readLine()) != null)
                        System.out.print(line + "\n");
                    
                   String[] command = { BASE_PATH +"setup.sh"};   
                   
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
                        setupIndicator.setProgress(1);
                    }


                } catch (IOException ex) {
                    Logger.getLogger(FXMLController.class.getName()).log(Level.SEVERE, null, ex);
                    System.out.println(ex);
                }
    }
    
    
      public Thread downloadThread(final String fileURL, final String fileName, final ProgressIndicator downloader, Thread downloadThread) {
        downloadThread = new Thread( new Runnable() {
            public void run() {
                try {
                    System.out.println("download started");    
                    URL url = new URL(fileURL + fileName);
                    HttpURLConnection httpConnection = (HttpURLConnection) (url.openConnection());
                    final long completeFileSize = httpConnection.getContentLength();
                    System.out.println(completeFileSize + " file to be downloaded");
                            
                    java.io.BufferedInputStream in = new java.io.BufferedInputStream(httpConnection.getInputStream());
                    java.io.FileOutputStream fos = new java.io.FileOutputStream(
                           BASE_PATH +  fileName);
                    java.io.BufferedOutputStream bout = new BufferedOutputStream(
                            fos, 1024);
                    byte[] data = new byte[1024];
                    long downloadedFileSize = 0;
                    int x = 0;
                    while ((x = in.read(data, 0, 1024)) >= 0) {
                        downloadedFileSize += x;

                        // calculate progress
                        final double currentProgress =  ((((double)downloadedFileSize) / ((double)completeFileSize)));
                       // System.out.println(currentProgress + " current progress file to be downloaded");        
                        // update progress bar
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                downloader.setProgress(currentProgress);
                            }
                        });

                        bout.write(data, 0, x);
                    }
                    bout.close();
                    in.close();
                } catch (FileNotFoundException e) {
                    System.out.println(e);
                } catch (IOException e) {
                    System.out.println(e);
                }
            }
        });
        downloadThread.start();
        return downloadThread;
      }
    
}
