package br.ufpe.cin.if710.podcast.domain;

import java.io.Serializable;

public class ItemFeed implements Serializable{
    private final String title;
    private final String link;
    private final String pubDate;
    private final String description;
    private final String downloadLink;
    private String fileUri;
    private Integer timePaused;


    public ItemFeed(String title, String link, String pubDate, String description, String downloadLink) {
        this.title = title;
        this.link = link;
        this.pubDate = pubDate;
        this.description = description;
        this.downloadLink = downloadLink;
        this.fileUri = "";
        this.timePaused = 0;
    }

    public String getTitle() {
        return title;
    }

    public String getLink() {
        return link;
    }

    public String getPubDate() {
        return pubDate;
    }

    public String getDescription() {
        return description;
    }

    public String getDownloadLink() {
        return downloadLink;
    }

    public String getFileUri() {
        return fileUri;
    }

    public void setFileUri(String fileUri){
        this.fileUri = fileUri;
    }

    @Override
    public String toString() {
        return title;
    }

    public Integer getTimePaused() {
        return timePaused;
    }

    public void setTimePaused(Integer timePaused) {
        this.timePaused = timePaused;
    }
}