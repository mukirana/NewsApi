package com.example.HackerNews;


import com.example.HackerNews.Model.Comment;
import com.example.HackerNews.Model.HackerNewsStory;
import com.example.HackerNews.Model.User;
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
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Component
public class Helper {
    @Autowired
    RestTemplate restTemplate;
    @Autowired
    private RedisRepository redisRepository;


    Executor executor = Executors.newFixedThreadPool(50);
    public <T,U,V,W> CompletableFuture<T> getDataAsync(U data, String BaseUrl , Class<T> returnClass , W redisData){
        String Url = BaseUrl+data+Constant.json;
        return CompletableFuture.supplyAsync(()->{
            List<T> cachedData = redisRepository.find(redisData);
            if(cachedData!=null && cachedData.size()!=0){
                return (cachedData.get(0));
            }
            ResponseEntity<T> response = restTemplate.getForEntity(Url, returnClass);
            return response.getBody();
        },executor);

    }

    public <T,U,V,W> CompletableFuture<T> DataAsync(U data, String BaseUrl , Class<T> returnClass , W redisData){
        String Url = BaseUrl+data+Constant.json;
        return CompletableFuture.supplyAsync(()->{
            List<T> cachedData = redisRepository.find(redisData);
            if(cachedData!=null && cachedData.size()!=0){
                return (cachedData.get(0));
            }
            ResponseEntity<T> response = restTemplate.getForEntity(Url, returnClass);
            return response.getBody();
        },executor).handle((T,ex) -> {
            if(ex != null){
                return null;
            }
            return T;
        });

    }

    public <T>  CompletableFuture<List<T>> getDataList(List<CompletableFuture<T>> pageContentFutures){
        CompletableFuture<Void> allFutures = CompletableFuture.allOf(
                pageContentFutures.toArray(new CompletableFuture[pageContentFutures.size()])
        );

        CompletableFuture<List<T>> allPageContentsFuture = allFutures.thenApply(v -> {
            return pageContentFutures.stream()
                    .map(pageContentFuture -> pageContentFuture.join())
                    .collect(Collectors.toList());
        });
        return allPageContentsFuture;
    }

    public List<Comment> getCombinedCommentAndUserData(List<HackerNewsStory> listHackerNewsStory,  HashMap<String,Long> hm ){
        List<Comment> allCommentWithUserDetail = new ArrayList<>();
        for (HackerNewsStory hackerNewsStory:listHackerNewsStory) {
            Comment comment = new Comment();

            comment.setText(hackerNewsStory.getText());
            int kidCount = hackerNewsStory.getKids()==null?0:hackerNewsStory.getKids().size();
            comment.setChildCommentCount(kidCount);
            comment.setUser(hackerNewsStory.getBy());
            Long userAge = hm.getOrDefault(hackerNewsStory.getBy(),0L);
            comment.setUserAge(userAge);
            comment.setId(hackerNewsStory.getId());
            allCommentWithUserDetail.add(comment);
        }
        return allCommentWithUserDetail;
    }


}



