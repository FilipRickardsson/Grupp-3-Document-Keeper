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
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class EditFrameController implements Initializable {

    private DBConnection dbConnection;
    private SuggestionProvider<String> provider;
    private List<String> tagSuggestions;
    private List<Document> documentsToEdit;

    @FXML
    Label lblHeader;

    @FXML
    ListView lvTags, lvLinkedDocuments;

    @FXML
    TextField tfAddTag, tfAddLinkedDocument;

    @FXML
    ObservableList<String> obsTagsList, obsLinkedDocumentsList;

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

    private void addTagsToTagsListView() {
        List commonTags = new ArrayList();

        for (int i = 0; i < documentsToEdit.size(); i++) {
            if (i == 0) {
                commonTags.addAll(documentsToEdit.get(i).getTags());
            } else {
                //Visa bara gemensamma taggar
                commonTags.retainAll(documentsToEdit.get(i).getTags());
            }

            obsTagsList.clear();
            obsTagsList.addAll(commonTags);
        }
    }

    private void addLinkedDocumentsToListView() {
        List commonLinkedDocuments = new ArrayList();

        for (int i = 0; i < documentsToEdit.size(); i++) {
            if (i == 0) {
                commonLinkedDocuments.addAll(documentsToEdit.get(i).getLinkedDocuments());
            } else {
                //Visa bara gemensamma document
                commonLinkedDocuments.retainAll(documentsToEdit.get(i).getLinkedDocuments());
            }
        }

        obsLinkedDocumentsList.clear();
        obsLinkedDocumentsList.addAll(commonLinkedDocuments);
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
        }
    }

    @FXML
    private void handleButtonAddLinkedDocument() {
        String titleOfDocumentToLink = tfAddLinkedDocument.getText();
        boolean insertionSuccess = dbConnection.addLinkedDocument(titleOfDocumentToLink, documentsToEdit);
        if (insertionSuccess) {
            obsLinkedDocumentsList.add(titleOfDocumentToLink);
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
        addTagsToTagsListView();
        addLinkedDocumentsToListView();
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        obsTagsList = FXCollections.observableArrayList();
        obsLinkedDocumentsList = FXCollections.observableArrayList();
        lvTags.setItems(obsTagsList);
        lvLinkedDocuments.setItems(obsLinkedDocumentsList);
        tagSuggestions = new ArrayList<>();

        provider = SuggestionProvider.create(tagSuggestions);
        new AutoCompletionTextFieldBinding<>(tfAddTag, provider);
    }

}
