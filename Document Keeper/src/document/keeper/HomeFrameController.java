/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package document.keeper;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

/**
 * FXML Controller class
 *
 * @author Allan
 */
public class HomeFrameController implements Initializable
{
    @FXML private Button importButton;
    
    @FXML
    void handleImportButton(ActionEvent event){
                Stage stage = (Stage) ((Node)event.getSource()).getScene().getWindow();
        
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Open Resource File");
                fileChooser.showOpenDialog(stage);
    }

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb)
    {
        // TODO
    }    
    
}
