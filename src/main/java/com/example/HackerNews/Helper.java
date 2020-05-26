package com.example.HackerNews;


import com.example.HackerNews.Model.story;
import com.example.HackerNews.Repository.RedisRepository;
import com.example.HackerNews.Service.StoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.json.JsonParser;
import org.springframework.boot.json.JsonParserFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.concurrent.CompletableFuture;

import static com.example.HackerNews.Constant.baseUrl;
@Component
public class Helper {
    @Autowired
    StoryService storyService;
    @Autowired
    private RedisRepository redisRepository;

    public  List<story> getSortedStoriesByScore(List<Long> data,int len){
        List<story> list = new ArrayList<>();
        for(int i=0;i<len;i++){
            List<story> cachedData = redisRepository.findById(data.get(i));
            if(cachedData!=null && cachedData.size()!=0){
                 list.add(cachedData.get(0));
                 continue;
            }
            story response =storyService.getTopSortedCity(data.get(i));
            list.add(response);
        }
        Collections.sort(list);


       return list;
    }
}



