/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package document.keeper;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
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
    private void search() {
        
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        dbConnection = new DBConnection();

    }

}
