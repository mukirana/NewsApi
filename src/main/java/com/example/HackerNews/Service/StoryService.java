package com.example.HackerNews.Service;

import com.example.HackerNews.Helper;
import com.example.HackerNews.Model.Comment;
import com.example.HackerNews.Model.HackerNewsStory;
import com.example.HackerNews.Model.story;
import com.example.HackerNews.Repository.RedisRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.mongodb.core.aggregation.ArrayOperators;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import redis.clients.jedis.Response;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import static com.example.HackerNews.Constant.baseUrl;

@Service
public class StoryService {
    @Autowired
    Helper helper;
    @Autowired
    RedisRepository redisRepository;
    @Autowired
    RestTemplate restTemplate;


    public  List<story> getSortedStoriesByScore(List<Long> data,int len) throws ExecutionException, InterruptedException {
        List<story> list = new ArrayList<>();
        List<CompletableFuture<story>> pageContentFutures = data.stream()
                .map(story -> helper.getTopSortedCity(story))
                .collect(Collectors.toList());

        CompletableFuture<Void> allFutures = CompletableFuture.allOf(
                pageContentFutures.toArray(new CompletableFuture[pageContentFutures.size()])
        );

        CompletableFuture<List<story>> allPageContentsFuture = allFutures.thenApply(v -> {
            return pageContentFutures.stream()
                    .map(pageContentFuture -> pageContentFuture.join())
                    .collect(Collectors.toList());
        });
        list = allPageContentsFuture.join();
        Collections.sort(list);

        return list.stream().limit(10).collect(Collectors.toList());
    }

    public List<Comment> getTopComments(String id){
        String url = baseUrl+"item/"+id+".json";
        ResponseEntity<HackerNewsStory> result = restTemplate.getForEntity(url, HackerNewsStory.class);
        HackerNewsStory response = result.getBody();
        List<Long> kids = response.getKids();
        List<Comment> comments = new ArrayList<>();

//        List<CompletableFuture<Integer>> pageContentFutures = kids.stream()
//                .map(data -> getCount(data,0))
//                .collect(Collectors.toList());


        for (Long value: kids) {
            Comment comment = new Comment();
            comment.setId(value);
            comment.setChildCommentCount( helper.getCount(value));
            comments.add(comment);
        }
        return comments;
    }
//    @Async
//    public int getCount(long data){
//        String url = baseUrl+"item/"+data+".json";
//      //  ResponseEntity<HackerNewsStory> result = restTemplate.getForEntity(url, HackerNewsStory.class);
//        CompletableFuture<HackerNewsStory> result = Cal(url,HackerNewsStory.class);
//        HackerNewsStory response = result.join();
//        List<Long> kids = response.getKids();
//        if(kids==null){
//            return 0;
//        }
//        int count =0;
//        for (Long value: kids) {
//            count= 1+getCount(value);
//        }
//        return count;
//    }
//    @Async
//    public CompletableFuture<HackerNewsStory> Cal(String url,Object obj){
//        return CompletableFuture.supplyAsync(() -> {
//            ResponseEntity<HackerNewsStory> result = restTemplate.getForEntity(url, HackerNewsStory.class);
//            return result.getBody();
//        });
//    }

}
