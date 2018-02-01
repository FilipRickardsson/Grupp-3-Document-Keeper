/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package document.keeper;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.logging.Level;
import java.util.logging.Logger;



public class DBConnection {
    private final String dbURL = "jdbc:derby://localhost:1527/test/";
    private Connection conn;
    
    public DBConnection() {
        createConnection();
    }
    
    
    
    private void createConnection(){
        try {
            Class.forName("org.apache.derby.jdbc.ClientDriver").newInstance();
            conn = DriverManager.getConnection(dbURL);  
        } catch (Exception e) {
            Logger.getLogger(DBConnection.class.getName()).log(Level.SEVERE, null, e);
        }
    }
}
