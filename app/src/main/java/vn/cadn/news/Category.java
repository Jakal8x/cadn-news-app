package vn.cadn.news;

public class Category {
    private final String name;
    private final String rssUrl;

    public Category(String name, String rssUrl) {
        this.name = name;
        this.rssUrl = rssUrl;
    }

    public String getName() {
        return name;
    }

    public String getRssUrl() {
        return rssUrl;
    }
}
