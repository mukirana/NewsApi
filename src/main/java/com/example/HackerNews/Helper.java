package com.example.HackerNews;


import com.example.HackerNews.Model.Comment;
import com.example.HackerNews.Model.HackerNewsStory;
import com.example.HackerNews.Model.story;
import com.example.HackerNews.Repository.RedisRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
@Component
public class Helper {
    @Autowired
    RestTemplate restTemplate;
    @Autowired
    private RedisRepository redisRepository;


    Executor executor = Executors.newFixedThreadPool(50);
    public CompletableFuture<story> getTopSortedCity(Object data){
        String Url = Constant.itemsUrl+data.toString()+Constant.json;

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
        String url =Constant.itemsUrl+data+Constant.json;
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
        }

        return count;
    }


}



