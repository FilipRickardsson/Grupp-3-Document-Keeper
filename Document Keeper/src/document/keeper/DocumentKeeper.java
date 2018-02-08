/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package document.keeper;

import java.io.File;
import java.io.IOException;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;

/**
 *
 * @author Allan
 */
public class DocumentKeeper extends Application
{

    @Override
    public void start(Stage primaryStage) throws IOException
    {
        Parent root = FXMLLoader.load(getClass().getResource("HomeFrame.fxml"));

        Scene scene = new Scene(root);

        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.setTitle("Document Keeper");
        primaryStage.show();

        // Don't allow close if decrypted files are open/ not deleted
        primaryStage.setOnCloseRequest(evt ->
        {
            // prevent window from closing
            evt.consume();

            // call shutdown() to check and delete files
            shutdown(primaryStage);
        });
    }

    private void shutdown(Stage mainWindow)
    {
        File tempFolder = new File("./DKDocuments/Temp/");
        File[] filesInFolder = tempFolder.listFiles();

        // Is there are files in TEMP
        if (filesInFolder.length > 0)
        {
            Boolean closeApp = true;
            Boolean wasDeleted = true;

            // Delete if possible/file not open
            for (File f : filesInFolder)
            {
                System.out.println("delete filesInFolder" + f);
                wasDeleted = f.delete();
                if (!wasDeleted)
                {
                    closeApp = wasDeleted;
                }
            }

            if (!closeApp)
            {
                Alert alert = new Alert(Alert.AlertType.NONE, "You need to close other applications using decrypted files!", ButtonType.OK);
                if (alert.showAndWait().orElse(ButtonType.NO) == ButtonType.OK)
                {

                }
            } else
            {
                mainWindow.close();
            }
        } else
        {
            mainWindow.close();
        }
    }

    public static void deleteFolder(File folder)
    {
        File[] files = folder.listFiles();
        if (files != null)
        { //some JVMs return null for empty dirs
            for (File f : files)
            {
                if (f.isDirectory())
                {
                    //deleteFolder(f);
                } else
                {
                    f.delete();
                }
            }
        }
        folder.delete();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args)
    {
        launch(args);
    }

}
