package document.keeper;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
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
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
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
    List<Document> documentList;

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
        switchToEditFrameScene(event);
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
        
        System.out.println(documentSelected.get(0).getTitle());
        
        documentSelected.stream().forEach((d) ->
        {
            if (lblSelectedDocument.getText().equals("")) {
                lblSelectedDocument.setText(d.getTitle());
                lblTags.setText(d.getTags() + "\n");
                
                //Hämta alla dokument med en for loop
                String title = d.getTitle();
                lblLinkedDocuments.setText(title + "\n");
                paneMetadata.setVisible(true);
            } else {
                //Visa bara gemensamma länkade dokument och taggar
                lblSelectedDocument.setText(lblSelectedDocument.getText() + ", " + d.getTitle());
                lblTags.setText(lblTags.getText() + d.getTags() + "\n");
                paneMetadata.setVisible(false);
            }
        });
        
        System.out.println(documentSelected.get(0).getTitle());
        
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

    private void switchToEditFrameScene(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("EditFrame.fxml"));
            Parent root = (Parent) loader.load();
            EditFrameController controller = (EditFrameController) loader.getController();
            controller.setDocumentsToEdit(getSelectedDocuments());
            Scene scene = new Scene(root);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private List getSelectedDocuments() {
        List<Document> selectedDocuments = new ArrayList();
        ObservableList<Document> obsSelectedItems = lvDocument.getSelectionModel().getSelectedItems();
        for (Document d : obsSelectedItems) {
            selectedDocuments.add(d);
        }
        return selectedDocuments;
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        System.out.println("debagger HomeFrameController");
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
        fileList.add(new Document(1, "Cooper", ".txt", "54kb", "test", "test", aList, tagList));
        fileList.add(new Document(2, "Rose", ".doc", "100kb", "test", "test", aList, tagList));
        fileList.add(new Document(3, "Magnus", ".jpg", "12kb", "test", "test", aList, tagList));
        
        System.out.println(fileList.get(1).toString());
        
        documentList = dbConnection.getAllDocuments();
        
        System.out.println(documentList.toString());

        obsDocumentList = FXCollections.observableArrayList(documentList);
        lvDocument.setItems(obsDocumentList);

        lvDocument.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    }
}
