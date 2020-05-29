package com.example.HackerNews.Model;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class Comment implements Serializable, Comparable<Comment>{
    private  Long id;
    private int childCommentCount;
    private String text;
    private String user;
    private Long userAge;
    // compareTo override for comparable
    @Override
    public int compareTo(Comment comment) {
        return comment.getChildCommentCount()-this.getChildCommentCount();
    }
}
