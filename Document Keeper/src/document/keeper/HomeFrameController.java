package document.keeper;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.List;
import java.util.ResourceBundle;
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
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import javafx.scene.control.TextField;

/**
 * FXML Controller class
 *
 * @author Allan
 */
public class HomeFrameController implements Initializable {

    DBConnection dbConnection;

    @FXML
    private TextField tfSearch;

    @FXML
    private ObservableList<Document> obsDocumentList;

    @FXML
    private ListView lvDocument;

    @FXML
    private Button importButton, exportButton, editButton;

    @FXML
    private Label labelChosedFiles, labelMetadata;

    @FXML
    private Label lblTitle, lblType, lblFileSize, lblDateImported, lblDateCreated;

    Encryption encryption = new Encryption();

    @FXML
    void handleImportButton(ActionEvent event) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IOException, CryptoException, SQLException {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

        //Opens file chooser
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Import files");

        //File selectedFile = fileChooser.showOpenDialog(stage);
        List<File> list = fileChooser.showOpenMultipleDialog(stage);

        if (list != null) {
            for (File file : list) {
                // creates filepath 
                String filePath = "./DKDocuments/" + file.getName();
                String encryptedFilePath = filePath.substring(0, filePath.lastIndexOf('.'));
                String encryptedFilePathEnding = encryptedFilePath + ".encoded";
                // Creates empty file
                File encryptedFile = new File(encryptedFilePathEnding);
                // Encrypts and copies imported file to newly created file 
                encryption.encrypt("abcdefghijklmnop", file, encryptedFile);
                // Creates document with extracted metadata
                Document documentToDB = extractMetaData(file);
                // Send list with documents to DB
                dbConnection.insertDocument(documentToDB);

            }
        }
    }
    
    
    
    // Metod körs vid varje markering, ska bara köra på dubbelklick             TODO
    @FXML
    private void lvDocumentDoubleClicked() throws CryptoException, IOException {
        Document documentSelected = (Document) lvDocument.getSelectionModel().getSelectedItem();
        
        String title = documentSelected.getTitle();
        String type = documentSelected.getType();
        
        String encryptedFilePath = "./DKDocuments/" + title + ".encoded";
        String decodedFilePath = "./DKDocuments/Temp/" + title + "." + type;
        
        // Decode file and put in Temp dir
        File encryptedFile = new File(encryptedFilePath);
        File decodedFile = new File(decodedFilePath);
        encryption.decrypt("abcdefghijklmnop", encryptedFile, decodedFile);
        
        // Open decoded file with default program
        Desktop.getDesktop().open(decodedFile);
    }
    
    public Document extractMetaData(File file) throws IOException {

        DateFormat df = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        String fileSize = String.valueOf(file.length());
        String currentTime = df.format(Calendar.getInstance().getTime());

        // Extract date created from file
        BasicFileAttributes attr = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
        FileTime date = attr.creationTime();
        String dateCreated = df.format(date.toMillis());

        Document document = new Document(
                // Title
                file.getName().substring(0, file.getName().lastIndexOf('.')),
                // Type
                file.getName().substring(file.getName().lastIndexOf('.') + 1),
                // Size
                fileSize,
                // Date imported
                currentTime,
                // Date created
                dateCreated
        );
        return document;
    }

    @FXML
    void handleExportButton(ActionEvent event) {

    }

    @FXML
    void handleEditButton(ActionEvent event) {

    }

    @FXML
    private void lvDocumentSelected() throws CryptoException, IOException {
        //Dessa är osynliga tills man väljer ett dokument i vyn
        lblTitle.setVisible(true);
        lblType.setVisible(true);
        lblFileSize.setVisible(true);
        lblDateImported.setVisible(true);
        lblDateCreated.setVisible(true);

        Document documentSelected = (Document) lvDocument.getSelectionModel().getSelectedItem();
        lvDocumentDoubleClicked();
        lblTitle.setText("Title: " + documentSelected.getTitle());
        lblType.setText("Type: " + documentSelected.getType());
        lblFileSize.setText("File size: " + documentSelected.getFile_size());
        lblDateImported.setText("Date imported: " + documentSelected.getDate_imported());
        lblDateCreated.setText("Date created: " + documentSelected.getDate_created());
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

        /*
        DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String dateString = "2018-02-01";
        Date dateObject = new Date();
        try {
            dateObject = sdf.parse(dateString);
        } catch (ParseException ex) {
            Logger.getLogger(HomeFrameController.class.getName()).log(Level.SEVERE, null, ex);
        }
         */
        List documentList = dbConnection.getAllDocuments();

        obsDocumentList = FXCollections.observableArrayList(documentList);
        lvDocument.setItems(obsDocumentList);
    }

}
