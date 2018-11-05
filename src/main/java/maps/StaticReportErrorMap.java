package maps;

public class StaticReportErrorMap {
    private String id;
    private String name;
    private String folderName;
    private String webLink;

    public StaticReportErrorMap(String id, String name, String folderName) {
        this.id = id;
        this.name = name;
        this.folderName = folderName;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFolderName() {
        return folderName;
    }

    public void setFolderName(String folderName) {
        this.folderName = folderName;
    }

    public String getWebLink() {
        return webLink;
    }

    public void setWebLink(String webLink) {
        this.webLink = webLink;
    }
}

