package com.example.HackerNews;


import com.example.HackerNews.Model.Comment;
import com.example.HackerNews.Model.HackerNewsStory;
import com.example.HackerNews.Model.story;
import com.example.HackerNews.Repository.RedisRepository;
import com.example.HackerNews.Service.StoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.json.JsonParser;
import org.springframework.boot.json.JsonParserFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import static com.example.HackerNews.Constant.baseUrl;
@Component
public class Helper {
    @Autowired
    RestTemplate restTemplate;
    @Autowired
    private RedisRepository redisRepository;

    Executor executor = Executors.newFixedThreadPool(50);
    public CompletableFuture<story> getTopSortedCity(Object data){
        String Url = baseUrl+"item/"+data.toString()+".json";

        return CompletableFuture.supplyAsync(()->{
            List<story> cachedData = redisRepository.findById((Long)data);
            if(cachedData!=null && cachedData.size()!=0){
                return (cachedData.get(0));
            }
            ResponseEntity<story> response = restTemplate.getForEntity(Url, story.class);
            return response.getBody();
        },executor);

    }


    public int getCount(long data){
        String url = baseUrl+"item/"+data+".json";
        Comment comment = new Comment();
        ResponseEntity<HackerNewsStory> result = restTemplate.getForEntity(url, HackerNewsStory.class);
        HackerNewsStory response = result.getBody();
        List<Long> kids = response.getKids();
        if(kids==null){
            return 0;
        }
        int count =0;
        for (Long value: kids) {
           count = 1+getCount(value);
          //  comment.setChildCommentCount(1+getCount(value));
        }
//        comment.setText(response.getText());
//        comment.setUser(response.getBy());
        return count;
    }


}



