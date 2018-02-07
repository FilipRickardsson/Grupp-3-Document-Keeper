package document.keeper;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.stage.DirectoryChooser;

/**
 * FXML Controller class
 *
 * @author Allan
 */
public class HomeFrameController implements Initializable {

    DBConnection dbConnection;
    Encryption encryption = new Encryption();
    List<Document> documentList;

    @FXML
    private TextField tfSearch;

    @FXML
    private ObservableList<Document> obsDocumentList;

    @FXML
    private ListView lvDocument;

    @FXML
    private Button newButton, importButton, exportButton, editButton, openButton;

    @FXML
    private Label labelChosedFiles, labelMetadata, lblSelectedDocument, lblTitle,
            lblType, lblFileSize, lblDateImported, lblDateCreated, lblTags, lblLinkedDocuments,
            lblLinkedDocumentsfeedbackMessage, labelFeedbackMessage, lblLinkedDocumentsGraphic, lblTagsGraphic, labelExportFeedback;

    @FXML
    private Pane paneMetadata;

    @FXML
    void handleImportButton(ActionEvent event) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IOException, CryptoException, SQLException {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

        int documentListSize = documentList.size();

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
            updateListView();

            //Compare list before with list after 
            int importedDocuments = documentList.size() - documentListSize;

            //Message about imported files       
            if (importedDocuments > 1) {
                labelFeedbackMessage.setText("You succesfully imported "
                        + importedDocuments + " documents.");
            } else {
                labelFeedbackMessage.setText("You succesfully imported a document.");
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

    //Creates new textfile with default name, located in temp folder. When saved 
    //decrypts and places file in DKDocuments folder  
    @FXML
    void handleNewButton(ActionEvent event) throws CryptoException, IOException, InterruptedException {

        DateFormat df = new SimpleDateFormat("ddMMyyyy HHmmss");
        Date date = new Date();
        String defaultName = df.format(date);
        File defaultFile = new File("./DKDocuments/Temp/" + defaultName + ".txt");
        defaultFile.createNewFile();

        BasicFileAttributes attr = Files.readAttributes(defaultFile.toPath(), BasicFileAttributes.class);
        attr.creationTime();
        attr.lastModifiedTime();

        try {
            // Open file with default texteditor
            Desktop.getDesktop().open(defaultFile);
        } catch (IOException ex) {
            Logger.getLogger(HomeFrameController.class.getName()).log(Level.SEVERE, null, ex);
        }

        // Thread that checks if the document has changed. On change saves to DB and places in directory
        Runnable runNewDocument = new Runnable() {
            public void run() {
                final Path path = Paths.get("./DKDocuments/Temp/");
                System.out.println("path " + path);
                try (final WatchService watchService = FileSystems.getDefault().newWatchService()) {
                    final WatchKey watchKey = path.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);
                    while (true) {
                        final WatchKey wk = watchService.take();
                        for (WatchEvent<?> event : wk.pollEvents()) {
                            //we only register "ENTRY_MODIFY" so the context is always a Path.
                            final Path changed = (Path) event.context();
                            System.out.println(changed);
                            if (changed.endsWith(defaultName + ".txt")) {
                                // Creates path for encrypted file
                            File encryptedFile = new File("./DKDocuments/" + defaultName + ".encoded");

                            // Encrypts and copies imported file to newly created file 
                            encryption.encrypt("abcdefghijklmnop", defaultFile, encryptedFile);

                            // Creates document with extracted metadata
                            Document documentToDB = extractMetaData(defaultFile);

                            // Send list with documents to DB
                            dbConnection.insertDocument(documentToDB);

                            Platform.runLater(new Runnable() {
                                @Override
                                public void run() {
                                    updateListView();
                                }
                            });
  
                                System.out.println("My file has changed");
                            }
                        }
                        // reset the key
                        boolean valid = wk.reset();
                        if (!valid) {
                            System.out.println("Key has been unregisterede");
                        }
                    }
                } catch (IOException| InterruptedException |CryptoException ex) {
                    Logger.getLogger(HomeFrameController.class.getName()).log(Level.SEVERE, null, ex);
                } 
            }
        };

        Thread thread = new Thread(runNewDocument);
        thread.start();

    }

    @FXML
    void handleOpenButton(ActionEvent event) throws CryptoException, IOException {
        ObservableList<Document> documentsSelected = (ObservableList<Document>) lvDocument.getSelectionModel().getSelectedItems();

        System.out.println("documentsSelected" + documentsSelected);

        documentsSelected.stream().forEach((d)
                -> {
            String title = d.getTitle();
            String type = d.getType();

            String encryptedFilePath = "./DKDocuments/" + title + ".encoded";
            String decodedFilePath = "./DKDocuments/Temp/" + title + "." + type;

            // Decode file and put in Temp dir 
            File encryptedFile = new File(encryptedFilePath);
            File decodedFile = new File(decodedFilePath);
            try {
                encryption.decrypt("abcdefghijklmnop", encryptedFile, decodedFile);
            } catch (CryptoException ex) {
                Logger.getLogger(HomeFrameController.class.getName()).log(Level.SEVERE, null, ex);
            }

            try {
                // Open decoded file with default program
                Desktop.getDesktop().open(decodedFile);
            } catch (IOException ex) {
                Logger.getLogger(HomeFrameController.class.getName()).log(Level.SEVERE, null, ex);
            }

        });

    }

    @FXML
    void handleExportButton(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        // Selected docs
        ObservableList<Document> documentsSelected = (ObservableList<Document>) lvDocument.getSelectionModel().getSelectedItems();

        System.out.println("documentsSelected" + documentsSelected);
        DirectoryChooser choose = new DirectoryChooser();
        File dir = choose.showDialog(stage);
        System.out.println("dir " + dir);

        documentsSelected.stream().forEach((d)
                -> {
            String title = d.getTitle();
            String type = d.getType();

            String encryptedFilePath = "./DKDocuments/" + title + ".encoded";
            String decodedFilePath = dir + "/" + title + "." + type;
            System.out.println("decodedFilePath " + decodedFilePath);
            // Decode file and put in Temp dir 
            File encryptedFile = new File(encryptedFilePath);
            File decodedFile = new File(decodedFilePath);
            try {
                encryption.decrypt("abcdefghijklmnop", encryptedFile, decodedFile);
            } catch (CryptoException ex) {
                System.out.println("****************************************************** " + title + type);
                labelExportFeedback.setText("Could not export " + title + "." + type);
                //Logger.getLogger(HomeFrameController.class.getName()).log(Level.SEVERE, null, ex);
            }

        });

    }

    @FXML
    void handleEditButton(ActionEvent event) {
        switchToEditFrameScene(event);
    }

    @FXML
    private void lvDocumentSelected() {
        ObservableList<Document> documentsSelected = (ObservableList<Document>) lvDocument.getSelectionModel().getSelectedItems();
        lblSelectedDocument.setText("");
        lblLinkedDocuments.setText("");
        lblTags.setText("");
        paneMetadata.setVisible(false);

        if (documentsSelected.size() == 1) {
            System.out.println(documentsSelected.toString());
            Document documentSelected = documentsSelected.get(0);
            displayFileInfo(documentSelected);
        } else if (documentsSelected.size() > 1) {
            displayMultipleFiles(documentsSelected);
        }
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
            controller.setDBConnection(dbConnection);
            controller.initScene();
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

    private void displayFileInfo(Document doc) {
        lblSelectedDocument.setText(doc.toString());
        lblTagsGraphic.setText("Tags:");
        lblLinkedDocumentsGraphic.setText("Linked documents:");
        lblTags.setText("");
        lblLinkedDocuments.setText("");
        paneMetadata.setVisible(true); //Visar metadata

        lblTitle.setText("Title: " + doc.getTitle());
        lblType.setText("Type: " + doc.getType());
        lblFileSize.setText("File size: " + doc.getFile_size());
        lblDateImported.setText("Date imported: " + doc.getDate_imported());
        lblDateCreated.setText("Date created: " + doc.getDate_created());

        if (doc.getTags().size() > 0) {
            doc.getTags().stream().forEach((tag)
                    -> {
                if (lblTags.getText().equals("")) {
                    lblTags.setText(tag);
                } else {
                    lblTags.setText(lblTags.getText() + "\n" + tag);
                }
            });

            doc.getLinkedDocuments().stream().forEach((document)
                    -> {
                System.out.println(document);
                documentList.stream().filter((d) -> (d.getId() == document)).forEach((d)
                        -> {
                    if (lblLinkedDocuments.getText().equals("")) {
                        lblLinkedDocuments.setText(d.getTitle());
                    } else {
                        lblLinkedDocuments.setText(lblLinkedDocuments.getText() + "\n" + d.getTitle());
                    }
                });
            });
        } else {
            lblTagsGraphic.setText("No tags to display");
            lblLinkedDocumentsGraphic.setText("No linked documents");
        }
    }

    private void displayMultipleFiles(List<Document> documentList) {
        List<String> commonTags = new ArrayList<>();
        List<Integer> commonLinkedDocuments = new ArrayList<>();

        lblTagsGraphic.setText("Common tags:");
        lblLinkedDocumentsGraphic.setText("Common linked documents:");
        lblSelectedDocument.setText("");

        documentList.stream().forEach((d)
                -> {
            if (lblSelectedDocument.getText().equals("")) {
                commonTags.addAll(d.getTags());
                commonLinkedDocuments.addAll(d.getLinkedDocuments());

                lblSelectedDocument.setText(d.toString());
            } else {
                //Visa bara gemensamma taggar
                commonTags.retainAll(d.getTags());
                commonLinkedDocuments.retainAll(d.getLinkedDocuments());

                lblSelectedDocument.setText(lblSelectedDocument.getText() + ", " + d.toString());
            }
        });

        commonTags.stream().forEach((t)
                -> {
            if (lblTags.getText().equals("")) {
                lblTags.setText(t);
            } else {
                lblTags.setText(lblTags.getText() + "\n" + t);
            }
        });

        commonLinkedDocuments.stream().forEach((d)
                -> {
            if (lblLinkedDocuments.getText().equals("")) {
                documentList.stream().forEach((doc)
                        -> {
                    if (d.equals(doc.getId())) {
                        lblLinkedDocuments.setText(doc.getTitle());
                    }
                });
            } else {
                documentList.stream().forEach((doc)
                        -> {
                    if (d.equals(doc.getId())) {
                        lblLinkedDocuments.setText(lblLinkedDocuments.getText() + "\n" + doc.getTitle());
                    }
                });
            }
        });
    }

    public void updateListView() {
        documentList = dbConnection.getAllDocuments();

        System.out.println(documentList.toString());

        obsDocumentList = FXCollections.observableArrayList(documentList);
        lvDocument.setItems(obsDocumentList);

        lvDocument.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        System.out.println("debagger HomeFrameController");
        dbConnection = new DBConnection();
        lvDocument.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        updateListView();

    }
}
