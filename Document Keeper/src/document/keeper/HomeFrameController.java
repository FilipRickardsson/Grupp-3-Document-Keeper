package document.keeper;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;


/**
 * FXML Controller class
 *
 * @author Allan
 */
public class HomeFrameController implements Initializable {

    DBConnection dbConnection;
    
    ArrayList<Document> fileList;

    @FXML
    private TextField tfSearch;

    @FXML
    private ObservableList<Document> obsDocumentList;

    @FXML
    private ListView lvDocument;
    
    @FXML
    private Button importButton, exportButton, editButton;

    @FXML
    private Label labelChosedFiles, labelMetadata, lblSelectedDocument;
    
    @FXML private Label lblTitle, lblType, lblFileSize, lblDateImported, lblDateCreated, lblTags,
            lblLinkedDocuments;
    
    @FXML private Pane paneMetadata;
    
    Encryption encryption = new Encryption();

    @FXML
    void handleImportButton(ActionEvent event) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IOException, CryptoException {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

        //Opens file chooser
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Import files");

        //File selectedFile = fileChooser.showOpenDialog(stage);
        List<File> list = fileChooser.showOpenMultipleDialog(stage);

        if (list != null) {
            for (File file : list) {
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
    
    private void copyFile(File file) {
        //Creates destination for copied file based on it's default name
        String destFileName = "./DKDocuments/" + file.getName();
        
        try {
            File dest = new File(destFileName);

            Files.copy(file.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            Logger.getLogger(
                HomeFrameController.class.getName()).log(
                    Level.SEVERE, null, ex
                );
        }
    }
    
    @FXML private void lvDocumentSelected() {
        ObservableList<Document> documentSelected = (ObservableList<Document>)lvDocument.getSelectionModel().getSelectedItems();
        lblSelectedDocument.setText("");
        
        documentSelected.stream().forEach((d) ->
        {
            if (lblSelectedDocument.getText().equals("")) {
                lblSelectedDocument.setText(d.getTitle());
                lblTags.setText(d.getTags() + "\n");
                
                //Hämta alla dokument med en for loop
                String title = fileList.get(d.getLinkedDocuments().get(0)).getTitle();
                lblLinkedDocuments.setText(title + "\n");
                paneMetadata.setVisible(true);
            } else {
                //Visa bara gemensamma länkade dokument och taggar
                lblSelectedDocument.setText(lblSelectedDocument.getText() + ", " + d.getTitle());
                lblTags.setText(lblTags.getText() + d.getTags() + "\n");
                paneMetadata.setVisible(false);
            }
        });
        
        lblTitle.setText("Title: " + documentSelected.get(0).getTitle());
        lblType.setText("Type: " + documentSelected.get(0).getType());
        lblFileSize.setText("File size: " + documentSelected.get(0).getFile_size());
        lblDateImported.setText("Date imported: " + documentSelected.get(0).getDate_imported());
        lblDateCreated.setText("Date created: " + documentSelected.get(0).getDate_created());
    }
    
    @FXML
    private void search() {
        obsDocumentList.clear();
        obsDocumentList.addAll(
        dbConnection.searchForDocumentByTitleOrTag(tfSearch.getText()));
    }

    @FXML
    void handleClearSearchButton() {
        tfSearch.clear();
        obsDocumentList.clear();
        obsDocumentList.addAll(
        dbConnection.getAllDocuments());
    }
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        dbConnection = new DBConnection();
        lvDocument.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        
        DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String dateString = "2018-02-01";
        Date dateObject = new Date();
        try {
            dateObject = sdf.parse(dateString);
        } catch (ParseException ex) {
            Logger.getLogger(HomeFrameController.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        List<Integer> aList = new ArrayList<>();
        aList.add(1);
        aList.add(2);
        List<String> tagList = new ArrayList<>();
        tagList.add("untagged");
        
        fileList = new ArrayList();
        fileList.add(new Document(1, "Cooper", ".txt", "54kb", dateObject, dateObject, aList, tagList));
        fileList.add(new Document(2, "Rose", ".doc", "100kb", dateObject, dateObject, aList, tagList));
        fileList.add(new Document(3, "Magnus", ".jpg", "12kb", dateObject, dateObject, aList, tagList));
        
        System.out.println(fileList.get(1).toString());
        
        List documentList = dbConnection.getAllDocuments();
        
        System.out.println(documentList.toString());

        obsDocumentList = FXCollections.observableArrayList(fileList);
        lvDocument.setItems(obsDocumentList);
    }
}
