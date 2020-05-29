package com.example.HackerNews.Model;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class User implements Serializable {
    String Id;
    Long Created;
}
