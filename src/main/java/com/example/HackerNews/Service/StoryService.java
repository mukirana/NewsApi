package com.example.HackerNews.Service;

import com.example.HackerNews.Constant;
import com.example.HackerNews.Exception.ApiException;
import com.example.HackerNews.Exception.ApiRequestException;
import com.example.HackerNews.Helper;
import com.example.HackerNews.Model.Comment;
import com.example.HackerNews.Model.HackerNewsStory;
import com.example.HackerNews.Model.story;
import com.example.HackerNews.Repository.RedisRepository;
import com.example.HackerNews.Repository.StoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Service
public class StoryService {
    @Autowired
    Helper helper;
    @Autowired
    RedisRepository redisRepository;
    @Autowired
    RestTemplate restTemplate;
    @Autowired
    StoryRepository storyRepository;



    public List<story> getTopStories() throws ApiRequestException {
        ResponseEntity<Long[]> result = restTemplate.getForEntity(Constant.topStoriesUrl, Long[].class);
        if(Objects.isNull((result))){
            throw new ApiRequestException("Error from Server Side");
        }
        List<Long> list = Arrays.asList(result.getBody());
        List<story> topTenStory= getSortedStoriesByScore(list);
        for (story st: topTenStory) {
            List<story> ob = redisRepository.findById(st.getId());
            if(ob==null){
                storyRepository.save(st);
                List<story> storyList = new ArrayList<>();
                storyList.add(st);
                redisRepository.save(st.getId(),storyList,1, TimeUnit.DAYS);
            }
        }
        redisRepository.saveWithValue(Constant.Stories,topTenStory,10,TimeUnit.MINUTES);
        return topTenStory;
    }

    public  List<story> getSortedStoriesByScore(List<Long> data) {
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
        String url = Constant.itemsUrl+id+Constant.json;
        ResponseEntity<HackerNewsStory> result = restTemplate.getForEntity(url, HackerNewsStory.class);
        HackerNewsStory response = result.getBody();
        List<Long> kids = response.getKids();
        List<Comment> comments = new ArrayList<>();

        for (Long value: kids) {
            Comment comment = new Comment();
            comment.setId(value);
            comment.setChildCommentCount( helper.getCount(value));
            comments.add(comment);
        }
        return comments;
    }

    public List<story> getAllPreviousData() throws ApiRequestException
    {
            List<story> previousData = storyRepository.findAll();
            if(Objects.isNull(previousData) || previousData.size()==0){
                throw new ApiRequestException("Data not found");
            }
            return previousData;

    }

}
