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

    public void initScene() {
        setHeaderText();
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {

    }

}
