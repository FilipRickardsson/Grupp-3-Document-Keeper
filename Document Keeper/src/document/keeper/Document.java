package document.keeper;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 *
 * @author Alexiz
 */
public class Document {

    int id;
    String title;
    String type;
    String file_size;
    
    List<Integer> linkedDocuments;
    List<String> tags;
    
    String date_imported;
    String date_created;

    public Document() {
    }

    public Document(int id, String title, String type, String file_size, String date_imported, String date_created)
    {
        this.id = id;
        this.title = title;
        this.type = type;
        this.file_size = file_size;
        this.date_imported = date_imported;
        this.date_created = date_created;
    }
    
    public Document(String title, String type, String file_size, String date_imported, String date_created)
    {
        this.title = title;
        this.type = type;
        this.file_size = file_size;
        this.date_imported = date_imported;
        this.date_created = date_created;
    }
    
    public Document(int id, String title, String type, String file_size, String date_imported, String date_created, List<Integer> linkedDocuments, List<String> tags)
    {
        this.id = id;
        this.title = title;
        this.type = type;
        this.file_size = file_size;
        this.date_imported = date_imported;
        this.date_created = date_created;
        this.linkedDocuments = new ArrayList<>(linkedDocuments);
        this.tags = new ArrayList<>(tags);
    }

        
    @Override
    public String toString() {
        return title + type;
    }

        
    public int getId()
    {
        return id;
    }

    public void setId(int id)
    {
        this.id = id;
    }

    public String getTitle()
    {
        return title;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    public String getType()
    {
        return type;
    }

    public void setType(String type)
    {
        this.type = type;
    }

    public String getFile_size()
    {
        return file_size;
    }

    public void setFile_size(String file_size)
    {
        this.file_size = file_size;
    }

    public String getDate_imported()
    {
        return date_imported;
    }

    public void setDate_imported(String date_imported)
    {
        this.date_imported = date_imported;
    }

    public String getDate_created()
    {
        return date_created;
    }

    public void setDate_created(String date_created)
    {
        this.date_created = date_created;
    }

    public List<Integer> getLinkedDocuments()
    {
        return linkedDocuments;
    }

    public void setLinkedDocuments(List<Integer> linkedDocuments)
    {
        this.linkedDocuments = linkedDocuments;
    }

    public List<String> getTags()
    {
        return tags;
    }

    public void setTags(List<String> tags)
    {
        this.tags = tags;
    }
}
