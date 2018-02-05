package document.keeper;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;

public class EditFrameController implements Initializable {

    private List<Document> documentsToEdit;

    @FXML
    Label lblHeader;

    public void setDocumentsToEdit(List documentsToEdit) {
        this.documentsToEdit = documentsToEdit;
        setHeaderText();
        getDocumentsTags();
    }

    private void setHeaderText() {
        String headerTxt = "Editing: ";
        for (int i = 0; i < documentsToEdit.size(); i++) {
            if (i == documentsToEdit.size() - 1) {
                headerTxt = headerTxt + documentsToEdit.get(i).getTitle();
            } else {
                headerTxt = headerTxt + documentsToEdit.get(i).getTitle() + ", ";
            }
        }
        lblHeader.setText(headerTxt);
    }

    private void getDocumentsTags() {
        Get tags from DB
    }
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {

    }

}
