/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package document.keeper;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TextField;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.Label;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javax.crypto.NoSuchPaddingException;


/**
 * FXML Controller class
 *
 * @author Allan
 */
public class HomeFrameController implements Initializable {
    
    DBConnection dbConnection;
    
    @FXML
    private TextField tfSearch;
    
    ArrayList fileList;
    public static ObservableList <Document> obsDocumentList;
       
    @FXML
    private ListView lvDocument;
    
    @FXML
    private Button importButton, exportButton, editButton;
    
    @FXML
    private Label labelChosedFiles, labelMetadata;
    
    Encryption encryption = new Encryption();

    @FXML
    void handleImportButton(ActionEvent event) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IOException, CryptoException {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

        //Opens file chooser
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Import files");

        //File selectedFile = fileChooser.showOpenDialog(stage);
        List<File> list = fileChooser.showOpenMultipleDialog(stage);
        
        if (list != null){
            for (File file : list){
                String filePath = "./DKDocuments/" + file.getName();
                String encryptedFilePath = filePath.substring(0, filePath.lastIndexOf('.'));
                String encryptedFilePathEnding = encryptedFilePath + ".encoded";
                
                File encryptedFile = new File(encryptedFilePathEnding);
                encryption.encrypt("abcdefghijklmnop", file, encryptedFile);
            }          
        }
    }
    
    @FXML
    void handleExportButton(ActionEvent event) {
        
    }
    
     @FXML
    void handleEditButton(ActionEvent event) {
        
    }
    
        @FXML
    private void search() {
        
    }

     
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        
        dbConnection = new DBConnection();
        
        DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String dateString = "2018-02-01";
        Date dateObject = new Date();
        try {
            dateObject = sdf.parse(dateString);
        } catch (ParseException ex) {
            Logger.getLogger(HomeFrameController.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        fileList = new ArrayList();
        fileList.add(new Document(1, "Cooper", ".txt", "54kb", dateObject, dateObject));
        fileList.add(new Document(2, "Rose", ".doc", "100kb", dateObject, dateObject));
        fileList.add(new Document(3, "Magnus", ".jpg", "12kb", dateObject, dateObject));
        
        obsDocumentList = FXCollections.observableArrayList(fileList);
        lvDocument.setItems(obsDocumentList);
        
    }

}
