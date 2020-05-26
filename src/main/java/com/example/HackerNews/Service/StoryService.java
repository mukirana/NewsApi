package com.example.HackerNews.Service;

import com.example.HackerNews.Model.Comment;
import com.example.HackerNews.Model.story;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import redis.clients.jedis.Response;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static com.example.HackerNews.Constant.baseUrl;

@Service
public class StoryService {
    @Autowired
    RestTemplate restTemplate;

    public story getTopSortedCity(Object data){
       String Url = baseUrl+"item/"+data.toString()+".json";
       ResponseEntity<story> response = restTemplate.getForEntity(Url, story.class);
       return response.getBody();
    }

    public Comment getTopComments(){

        return new Comment();
    }
}
