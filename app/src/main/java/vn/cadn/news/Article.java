package vn.cadn.news;

import java.io.Serializable;

public class Article implements Serializable {
    private String title;
    private String link;
    private String pubDate;
    private String description;
    private String imageUrl;

    public Article() {}

    public Article(String title, String link, String pubDate, String description, String imageUrl) {
        this.title = title;
        this.link = link;
        this.pubDate = pubDate;
        this.description = description;
        this.imageUrl = imageUrl;
    }

    public String getTitle() {
        return title != null ? title : "";
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLink() {
        return link != null ? link : "";
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getPubDate() {
        return pubDate != null ? pubDate : "";
    }

    public void setPubDate(String pubDate) {
        this.pubDate = pubDate;
    }

    public String getDescription() {
        return description != null ? description : "";
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageUrl() {
        return imageUrl != null ? imageUrl : "";
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
