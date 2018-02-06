package document.keeper;

import impl.org.controlsfx.autocompletion.AutoCompletionTextFieldBinding;
import impl.org.controlsfx.autocompletion.SuggestionProvider;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import org.controlsfx.control.textfield.TextFields;

public class EditFrameController implements Initializable {

    private DBConnection dbConnection;
    private SuggestionProvider<String> provider;
    private List<String> tagSuggestions;
    private List<Document> documentsToEdit;

    @FXML
    Label lblHeader;

    @FXML
    ListView lvTags;

    @FXML
    TextField tfAddTag;

    @FXML
    ObservableList<String> obsTagsList;

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

        List documentsToEditTags = new ArrayList();
        for (Document d : documentsToEdit) {
            for (String tag : d.getTags()) {
                if (!documentsToEditTags.contains(tag)) {
                    documentsToEditTags.add(tag);
                }
            }
        }
        obsTagsList.addAll(documentsToEditTags);
    }

    @FXML
    private void handleTagOnKeyReleased() {

        tagSuggestions = dbConnection.getTagsuggestions(tfAddTag.getText());
        provider.clearSuggestions();
        provider.addPossibleSuggestions(tagSuggestions);
    }

    @FXML
    private void handleButtonAddTag() {
        if (tfAddTag.getText().matches("[a-zA-Z0-9]*") && tfAddTag.getText().contains(" ") == false && tfAddTag.getText().length() > 0) {
            boolean tagAdded = dbConnection.addTag(tfAddTag.getText());
            
            if (tagAdded) {
                obsTagsList.add(tfAddTag.getText());
                tfAddTag.clear();
            }
        }
    }

    public void initScene() {
        setHeaderText();
        addTagsToTagsListView();
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        obsTagsList = FXCollections.observableArrayList();
        lvTags.setItems(obsTagsList);
        tagSuggestions = new ArrayList<>();
        tagSuggestions.add("Bosse");
        tagSuggestions.add("Sossa");

        provider = SuggestionProvider.create(tagSuggestions);
        new AutoCompletionTextFieldBinding<>(tfAddTag, provider);
    }

}
