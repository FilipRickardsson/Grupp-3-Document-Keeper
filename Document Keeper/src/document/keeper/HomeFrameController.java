/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package document.keeper;

import java.awt.Desktop;
import java.io.Closeable;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
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
public class HomeFrameController implements Initializable {

    @FXML
    private Button importButton;
    
    private Desktop desktop = Desktop.getDesktop();

    @FXML
    void handleImportButton(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

        //Opens file chooser
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Import files");

        File selectedFile = fileChooser.showOpenDialog(stage);
        System.out.println("selected file " + selectedFile);

        //Copies selected file(s) to directory
        if (selectedFile != null) {
            //copy(selectedFile.getAbsolutePath(), "./DKDocuments");
            System.out.println("Skriver något?");
            openFile(selectedFile);
           
        }

    }

    private void openFile(File file) {
        try {
            //desktop.open(file);
            File dest = new File("./DKDocuments/bild");
            System.out.println("dest " + dest);

            Files.copy(file.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            Logger.getLogger(
                HomeFrameController.class.getName()).log(
                    Level.SEVERE, null, ex
                );
        }
    }
    
    public void copy(String from, String to) {
        FileReader fileReader = null;
        FileWriter fileWriter = null;
        try {
            fileReader = new FileReader(from);
            fileWriter = new FileWriter(to);
            int c = fileReader.read();
            while (c != -1) {
                fileWriter.write(c);
                c = fileReader.read();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            close(fileReader);
            close(fileWriter);
        }
    }

    public static void close(Closeable stream) {
        try {
            if (stream != null) {
                stream.close();
            }
        } catch (IOException e) {
            //...
        }
    }

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }

}
