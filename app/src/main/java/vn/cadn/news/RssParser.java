package vn.cadn.news;

import android.util.Xml;
import org.xmlpull.v1.XmlPullParser;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RssParser {
    private static final Pattern IMG_SRC_PATTERN = Pattern.compile("<img[^>]+src=[\"']([^\"']+)[\"']", Pattern.CASE_INSENSITIVE);
    private static final Pattern HTML_TAG_PATTERN = Pattern.compile("<[^>]+>");

    public static List<Article> parse(InputStream inputStream) throws Exception {
        List<Article> articles = new ArrayList<>();
        XmlPullParser parser = Xml.newPullParser();
        parser.setInput(inputStream, "UTF-8");

        int eventType = parser.getEventType();
        Article currentArticle = null;
        String currentTag = "";

        while (eventType != XmlPullParser.END_DOCUMENT) {
            switch (eventType) {
                case XmlPullParser.START_TAG:
                    currentTag = parser.getName();
                    if ("item".equalsIgnoreCase(currentTag)) {
                        currentArticle = new Article();
                    } else if (currentArticle != null) {
                        if ("enclosure".equalsIgnoreCase(currentTag)) {
                            String url = parser.getAttributeValue(null, "url");
                            if (url != null && !url.isEmpty()) {
                                currentArticle.setImageUrl(url);
                            }
                        }
                    }
                    break;

                case XmlPullParser.TEXT:
                    if (currentArticle != null && currentTag != null) {
                        String text = parser.getText();
                        if (text != null) {
                            text = text.trim();
                            if ("title".equalsIgnoreCase(currentTag)) {
                                if (currentArticle.getTitle().isEmpty()) {
                                    currentArticle.setTitle(cleanText(text));
                                }
                            } else if ("link".equalsIgnoreCase(currentTag)) {
                                if (currentArticle.getLink().isEmpty()) {
                                    currentArticle.setLink(text);
                                }
                            } else if ("pubDate".equalsIgnoreCase(currentTag)) {
                                if (currentArticle.getPubDate().isEmpty()) {
                                    currentArticle.setPubDate(text);
                                }
                            } else if ("description".equalsIgnoreCase(currentTag)) {
                                if (currentArticle.getDescription().isEmpty()) {
                                    // Extract image URL if present
                                    Matcher imgMatcher = IMG_SRC_PATTERN.matcher(text);
                                    if (imgMatcher.find()) {
                                        String img = imgMatcher.group(1);
                                        if (currentArticle.getImageUrl().isEmpty() && img != null) {
                                            currentArticle.setImageUrl(img);
                                        }
                                    }
                                    // Clean description
                                    String clean = HTML_TAG_PATTERN.matcher(text).replaceAll("").trim();
                                    currentArticle.setDescription(cleanText(clean));
                                }
                            }
                        }
                    }
                    break;

                case XmlPullParser.END_TAG:
                    String endTag = parser.getName();
                    if ("item".equalsIgnoreCase(endTag) && currentArticle != null) {
                        if (!currentArticle.getTitle().isEmpty() && !currentArticle.getLink().isEmpty()) {
                            articles.add(currentArticle);
                        }
                        currentArticle = null;
                    }
                    currentTag = "";
                    break;
            }
            eventType = parser.next();
        }

        return articles;
    }

    private static String cleanText(String input) {
        if (input == null) return "";
        return input.replace("&nbsp;", " ")
                    .replace("&amp;", "&")
                    .replace("&quot;", "\"")
                    .replace("&apos;", "'")
                    .replace("&#39;", "'")
                    .replace("&lt;", "<")
                    .replace("&gt;", ">")
                    .replaceAll("\\s+", " ")
                    .trim();
    }
}
