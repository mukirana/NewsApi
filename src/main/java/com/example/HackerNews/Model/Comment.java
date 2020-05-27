package com.example.HackerNews.Model;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class Comment implements Serializable {
    private long id;
    private int childCommentCount;
    private String text;
    private String user;
    private String userAge;
}
