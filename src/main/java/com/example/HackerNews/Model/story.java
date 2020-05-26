package com.example.HackerNews.Model;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;


@Getter
public class story implements Comparable<story> , Serializable{
   // private static final long serialVersionUID = 6529685098267757690L;

 private String by;
 //private Integer descendants;
 private Long id;
 //private List<Long> kids;
 private Integer score;
 private Long time;
 private String title;
 //private String type;
 private String url;

    // compareTo override for comparable
    @Override
    public int compareTo(story story) {
        return story.getScore()-this.getScore();
    }

}
