package document.keeper;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DBConnection {

    private final String dbURL = "jdbc:derby://localhost:1527/DocumentKeeperDB/";
    private Connection conn;
    private Statement stmt;

    public DBConnection() {
        createConnection();
        getAllDocuments();
    }

    private void createConnection() {
        try {
            Class.forName("org.apache.derby.jdbc.ClientDriver").newInstance();
            conn = DriverManager.getConnection(dbURL);
        } catch (Exception e) {
            Logger.getLogger(DBConnection.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    public List getAllDocuments() {
        List allDocuments = new ArrayList();
        try {
            stmt = conn.createStatement();
            ResultSet results = stmt.executeQuery("SELECT * FROM APP.DOCUMENT");

            allDocuments = createDocuments(results);
            results.close();
            stmt.close();
        } catch (SQLException sqlExcept) {
            sqlExcept.printStackTrace();
        }
        return allDocuments;
    }

    public List searchForDocumentByTitleOrTag(String searchStr) {
        List searchResult = new ArrayList();
        try {
            stmt = conn.createStatement();
            ResultSet results = stmt.executeQuery(""
                    + "SELECT DISTINCT id, title, type, file_size, date_imported, date_created FROM APP.DOCUMENT\n"
                    + "JOIN APP.DOCUMENT_HAS_TAGS\n"
                    + "ON APP.DOCUMENT.ID = APP.DOCUMENT_HAS_TAGS.DOCUMENTID\n"
                    + "WHERE \n"
                    + "LOWER(title) LIKE LOWER('%" + searchStr + "%')\n"
                    + "OR\n"
                    + "LOWER(tagname) LIKE LOWER('%" + searchStr + "%')\n");

            searchResult = createDocuments(results);
            printDocuments(searchResult);
            results.close();
            stmt.close();
        } catch (SQLException sqlExcept) {
            sqlExcept.printStackTrace();
        }
        return searchResult;
    }

    private void printDocuments(List<Document> documentsToPrint) throws SQLException {
        System.out.println("--------------------");
        for(Document d : documentsToPrint) {
            System.out.println(d.toString());
        }
    }

    private List createDocuments(ResultSet results) throws SQLException {
        List fetchedDocuments = new ArrayList();
        while (results.next()) {
            fetchedDocuments.add(new Document(
                    results.getInt(1),
                    results.getString(2),
                    results.getString(3),
                    results.getString(4),
                    results.getString(5),
                    results.getString(6)));
        }
        return fetchedDocuments;
    }

}
