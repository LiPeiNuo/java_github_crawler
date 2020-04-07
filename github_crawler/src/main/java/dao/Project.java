package dao;

public class Project {
    private String name;
    private  String url;
    private String description;

    private int starCount;
    private int forkCount;
    private int openedIssueCount;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getStarCount() {
        return starCount;
    }

    public void setStarCount(int starCount) {
        this.starCount = starCount;
    }

    public int getForkCount() {
        return forkCount;
    }

    public void setForkCount(int forkCount) {
        this.forkCount = forkCount;
    }

    public int getOpenedIssueCount() {
        return openedIssueCount;
    }

    public void setOpenedIssueCount(int openedIssueCount) {
        this.openedIssueCount = openedIssueCount;
    }

    @Override
    public String toString() {
        return "Project{" +
                "name='" + name + '\'' +
                ", url='" + url + '\'' +
                ", description='" + description + '\'' +
                ", starCount=" + starCount +
                ", forkCount=" + forkCount +
                ", openIssueCount=" + openedIssueCount +
                '}';
    }
}
