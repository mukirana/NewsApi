package com.example.HackerNews.Model;

import lombok.Getter;

import java.io.Serializable;
import java.util.List;

@Getter
public class HackerNewsStory implements Serializable {
    private String by;
    private Integer descendants;
    private Long id;
    private List<Long> kids;
    private Integer score;
    private Long time;
    private String title;
    private String type;
    private String url;
    private String text;

}
