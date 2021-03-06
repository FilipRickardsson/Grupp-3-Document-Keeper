package document.keeper;

import impl.org.controlsfx.autocompletion.AutoCompletionTextFieldBinding;
import impl.org.controlsfx.autocompletion.SuggestionProvider;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class EditFrameController implements Initializable {

    private DBConnection dbConnection;
    private SuggestionProvider<String> provider;
    private List<String> tagSuggestions;
    private List<Document> documentsToEdit;
    private List<Document> allDocuments;

    @FXML Label lblHeader;

    @FXML ListView lvTags, lvLinkedDocuments;

    @FXML TextField tfAddTag, tfAddDocument;

    @FXML ObservableList<String> obsTagsList;
    
    @FXML ObservableList<Document> obsDocumentList;

    public void setDocumentsToEdit(List documentsToEdit) {
        this.documentsToEdit = documentsToEdit;
    }

    public void setDBConnection(DBConnection dbConnection) {
        this.dbConnection = dbConnection;
    }

    private void setHeaderText() {
        String headerTxt = "Editing: ";
        for (int i = 0; i < documentsToEdit.size(); i++) {
            headerTxt = headerTxt + documentsToEdit.get(i).getTitle();
            if (i < documentsToEdit.size() - 1) {
                headerTxt = headerTxt + documentsToEdit.get(i).getTitle() + ", ";
            }
        }
        lblHeader.setText(headerTxt);
    }

    private void addListViewItems() {
        List commonTags = new ArrayList();
        List commonDocuments = new ArrayList();
        List documents = new ArrayList();

        for (int i = 0; i < documentsToEdit.size(); i++) {
            if (i == 0) {
                commonTags.addAll(documentsToEdit.get(i).getTags());
                commonDocuments.addAll(documentsToEdit.get(i).getLinkedDocuments());
            } else {
                //Visa bara gemensamma taggar
                commonTags.retainAll(documentsToEdit.get(i).getTags());
                commonDocuments.retainAll(documentsToEdit.get(i).getLinkedDocuments());
            }          
        }
        
        //This for-loop is to write out the correct documents, since we only have
        //their id
        for (int i = 0; i < commonDocuments.size(); i++) {
            for (int j = 0; j < allDocuments.size(); j++) {
                if (commonDocuments.get(i).equals(allDocuments.get(j).getId())) {
                    documents.add(allDocuments.get(j));
                }
            }
        }
        
        obsTagsList.clear();
        obsTagsList.addAll(commonTags);
        
        obsDocumentList.clear();
        obsDocumentList.addAll(documents);
    }

    @FXML
    private void handleTagOnKeyReleased() {
        tagSuggestions = dbConnection.getTagsuggestions(tfAddTag.getText());
        provider.clearSuggestions();
        provider.addPossibleSuggestions(tagSuggestions);
    }

    @FXML
    private void handleButtonAddTag() {
        String tag = tfAddTag.getText().toLowerCase();

        if (tag.matches("[a-zA-Z0-9]*") && tag.contains(" ") == false && tag.length() > 0) {
            dbConnection.addTag(tag);
            boolean insertionSuccess = dbConnection.addTagToDocument(tag, documentsToEdit);
            if (insertionSuccess) {
                obsTagsList.add(tag);
            }

            obsTagsList.stream().forEach((t) -> {
                if (t.equals("untagged")) {
                    obsTagsList.remove(t);
                }
            });
            
            tfAddTag.setText("");
        }
    }
    
    @FXML private void handleButtonAddDocument() {
        String documentName = tfAddDocument.getText();
        Document documentToAdd;
        
        if (documentName.matches("[a-zA-z0-9]*") && !documentName.contains(" ") && documentName.length() > 0) {
            for (Document d : allDocuments) {
                if (d.getTitle().equals(documentName)) {
                    documentToAdd = d;
                    dbConnection.addLinkedDocument(documentsToEdit, documentToAdd);
                    obsDocumentList.add(documentToAdd);
                    tfAddDocument.setText("");
                } else {
                    if (!tfAddDocument.getText().equals("")) {
                        Alert alert = new Alert(AlertType.NONE, "No document found, please enter the title of "
                                + "an existing document", ButtonType.OK);
                        alert.showAndWait();
                        tfAddDocument.setText("");
                    }
                }
            }
        }
    }

    @FXML
    private void handleButtonDone(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("HomeFrame.fxml"));
            Parent root = (Parent) loader.load();
            Scene scene = new Scene(root);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void initScene() {
        setHeaderText();
        addListViewItems();
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        dbConnection = new DBConnection();
        obsTagsList = FXCollections.observableArrayList();
        obsDocumentList = FXCollections.observableArrayList();
        lvTags.setItems(obsTagsList);
        lvLinkedDocuments.setItems(obsDocumentList);
        tagSuggestions = new ArrayList<>();
        allDocuments = dbConnection.getAllDocuments();

        provider = SuggestionProvider.create(tagSuggestions);
        new AutoCompletionTextFieldBinding<>(tfAddTag, provider);
    }

}
