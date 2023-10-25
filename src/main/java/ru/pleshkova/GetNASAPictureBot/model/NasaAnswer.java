package ru.pleshkova.GetNASAPictureBot.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class NasaAnswer {
    String copyright;
    String date;
    String explanation;
    String hdurl;
    String media_type;
    String service_version;
    String  title;
    String  url;

    public NasaAnswer(@JsonProperty("date") String date, @JsonProperty("explanation") String explanation,
                      @JsonProperty("hdurl") String hdurl, @JsonProperty("media_type") String media_type,
                      @JsonProperty("service_version") String service_version, @JsonProperty("title") String title,
                      @JsonProperty("url") String url, @JsonProperty("copyright") String copyright) {
        this.date = date;
        this.explanation = explanation;
        this.hdurl = hdurl;
        this.media_type = media_type;
        this.service_version = service_version;
        this.title = title;
        this.url = url;
        this.copyright = copyright;
    }
}
