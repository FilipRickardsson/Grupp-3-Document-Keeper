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
            results.close();
            stmt.close();
        } catch (SQLException sqlExcept) {
            sqlExcept.printStackTrace();
        }
        return searchResult;
    }

    private List createDocuments(ResultSet results) throws SQLException {
        List fetchedDocuments = new ArrayList();
        while (results.next()) {
            Document newDocument = new Document(
                    results.getInt(1),
                    results.getString(2),
                    results.getString(3),
                    results.getString(4),
                    results.getString(5),
                    results.getString(6));
            newDocument.setTags(getDocumentTags(newDocument.getId()));
            newDocument.setLinkedDocuments(getDocumentLinkedDocuments(newDocument.getId()));

            fetchedDocuments.add(newDocument);
        }
        return fetchedDocuments;
    }

    private List<String> getDocumentTags(int documentId) {
        List<String> tags = new ArrayList();
        try {
            stmt = conn.createStatement();
            ResultSet results = stmt.executeQuery(""
                    + "SELECT DISTINCT tagname FROM APP.DOCUMENT\n"
                    + "JOIN APP.DOCUMENT_HAS_TAGS\n"
                    + "ON APP.DOCUMENT.ID = APP.DOCUMENT_HAS_TAGS.DOCUMENTID\n"
                    + "WHERE \n"
                    + "id = " + documentId);

            while (results.next()) {
                tags.add(results.getString(1));
            }

            results.close();
            stmt.close();
        } catch (SQLException sqlExcept) {
            sqlExcept.printStackTrace();
        }
        return tags;
    }

    private List<Document> getDocumentLinkedDocuments(int documentId) {
        List<Document> linkedDocuments = new ArrayList();
        try {
            stmt = conn.createStatement();
            ResultSet results = stmt.executeQuery(""
                    + "SELECT DISTINCT id, title, type, file_size, date_imported, date_created  "
                    + "FROM APP.DOCUMENT "
                    + "JOIN APP.DOCUMENT_HAS_DOCUMENTS "
                    + "ON APP.DOCUMENT.ID = APP.DOCUMENT_HAS_DOCUMENTS.DOCUMENTID2 "
                    + "WHERE APP.DOCUMENT_HAS_DOCUMENTS.DOCUMENTID1 = " + documentId);

            linkedDocuments = createDocuments(results);
            results.close();
            stmt.close();
        } catch (SQLException sqlExcept) {
            sqlExcept.printStackTrace();
        }
        return linkedDocuments;
    }

    public boolean insertDocument(Document newDocument) {
        try {
            stmt = conn.createStatement();
            stmt.executeUpdate("INSERT INTO APP.DOCUMENT (title, type, file_size, date_created, date_imported) VALUES ('"
                    + newDocument.getTitle() + "','"
                    + newDocument.getType() + "','"
                    + newDocument.getFile_size() + "','"
                    + newDocument.getDate_created() + "','"
                    + newDocument.getDate_imported() + "')", Statement.RETURN_GENERATED_KEYS);

            ResultSet generatedKeys = stmt.getGeneratedKeys();

            if (generatedKeys != null && generatedKeys.next()) {
                int insertedDocumentId = generatedKeys.getInt(1);

                stmt.executeUpdate("INSERT INTO APP.DOCUMENT_HAS_TAGS "
                        + "(documentid, tagname) VALUES ("
                        + insertedDocumentId + ","
                        + "'untagged')");
            }

            stmt.close();
            return true;
        } catch (SQLException sqlExcept) {
            sqlExcept.printStackTrace();
            return false;
        }
    }

    public boolean tagDocument(int documentId, String tagname) {
        try {
            stmt.executeUpdate("INSERT INTO APP.DOCUMENT_HAS_TAGS "
                    + "(documentid, tagname) VALUES ("
                    + documentId + ",'"
                    + tagname + "')");

            stmt.close();
            return true;
        } catch (SQLException sqlExcept) {
            sqlExcept.printStackTrace();
            return false;
        }
    }

    public List<String> getTagsuggestions(String searchStr) {
        List<String> tagSuggestions = new ArrayList();
        try {
            stmt = conn.createStatement();
            stmt.setMaxRows(5);
            ResultSet results = stmt.executeQuery(""
                    + "SELECT NAME FROM APP.TAG \n"
                    + "WHERE \n"
                    + "LOWER(NAME)LIKE LOWER('" + searchStr + "%')"
            );

            while (results.next()) {
                tagSuggestions.add(results.getString(1));

            }

            results.close();
            stmt.close();
        } catch (SQLException sqlExcept) {
            sqlExcept.printStackTrace();
        }
        return tagSuggestions;
    }

    public boolean addTag(String tagName) {
        try {
            stmt = conn.createStatement();
            stmt.executeUpdate("INSERT INTO APP.TAG VALUES ('" + tagName + "')");

            stmt.close();
            return true;
        } catch (SQLException sqlExcept) {
            return false;
        }
    }

    public boolean addTagToDocument(String tagName, List<Document> documentsToEdit) {
        int failedInsertions = 0;
        for (Document document : documentsToEdit) {
            try {
                stmt = conn.createStatement();
                stmt.executeUpdate("INSERT INTO APP.DOCUMENT_HAS_TAGS "
                        + "VALUES (" + document.getId() + ",'" + tagName + "')");

                stmt = conn.createStatement();
                stmt.executeUpdate("DELETE FROM APP.DOCUMENT_HAS_TAGS "
                        + "WHERE documentid = " + document.getId()
                        + "AND tagname = 'untagged'");

                stmt.close();
            } catch (SQLException sqlExcept) {
                System.out.println("debagger failedInsertions");
                failedInsertions++;
            }
        }
        return failedInsertions != documentsToEdit.size();
    }

    public boolean addLinkedDocument(String titleOfDocumentToLink, List<Document> documentsToEdit) {
        int failedInsertions = 0;
        for (Document document : documentsToEdit) {
            try {
                stmt = conn.createStatement();
//                stmt.executeUpdate("INSERT INTO APP.DOCUMENT_HAS_DOCUMENTS "
//                        + "VALUES (" + document.getId() + "," + idOfDocumentToLink + ")");

                stmt.close();
            } catch (SQLException sqlExcept) {
                failedInsertions++;
            }
        }
        return failedInsertions != documentsToEdit.size();
    }

    private Document searchForDocumentByTitle(String documentTitle) {
        Document searchResult = null;
        try {
            stmt = conn.createStatement();
            stmt.setMaxRows(1);
            ResultSet results = stmt.executeQuery(""
                    + "SELECT DISTINCT id, title, type, file_size, date_imported, date_created FROM APP.DOCUMENT\n"
                    + "WHERE \n"
                    + "title = '" + documentTitle + "'");

            while (results.next()) {
                searchResult = new Document(
                        results.getInt(1),
                        results.getString(2),
                        results.getString(3),
                        results.getString(4),
                        results.getString(5),
                        results.getString(6));
            }

            results.close();
            stmt.close();
        } catch (SQLException sqlExcept) {
            sqlExcept.printStackTrace();
        }
        return searchResult;
    }

}
