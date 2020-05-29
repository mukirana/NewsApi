package com.example.HackerNews.Controller;

import com.example.HackerNews.Constant;
import com.example.HackerNews.Exception.ApiRequestException;
import com.example.HackerNews.Helper;
import com.example.HackerNews.Model.Comment;
import com.example.HackerNews.Model.HackerNewsStory;
import com.example.HackerNews.Model.story;
import com.example.HackerNews.Repository.RedisRepository;
import com.example.HackerNews.Repository.StoryRepository;
import com.example.HackerNews.Service.StoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static com.example.HackerNews.Constant.baseUrl;

@RestController
public class HomeController {

    @Autowired
    StoryRepository storyRepository;
    @Autowired
    private RedisRepository redisRepository;
    @Autowired
    StoryService storyService;
    @Autowired
    RestTemplate restTemplate;

    @GetMapping("/topStories")
    public ResponseEntity<List> getMessage() throws ExecutionException, InterruptedException, ApiRequestException {
        List<story> cachedTopStory = redisRepository.find(Constant.Stories);
        if(cachedTopStory!=null && cachedTopStory.size()!=0){
            return new ResponseEntity<>(cachedTopStory,HttpStatus.OK);
        }
        List<story> topStory = storyService.getTopStories();
        return new ResponseEntity<>(topStory,HttpStatus.OK);

    }

    @GetMapping("/comments/{id}")
    public ResponseEntity<List<Comment>> getTopComments(@PathVariable("id")String id){
        List<Comment> cachedTopStory = redisRepository.find(Constant.Comment+id);
        if(cachedTopStory!=null && cachedTopStory.size()!=0){
            return new ResponseEntity<>(cachedTopStory,HttpStatus.OK);
        }
       return  new ResponseEntity<>(storyService.getTopComments(id), HttpStatus.OK);

    }

    @GetMapping("/pastStories")
    public ResponseEntity<List<story>> getOldStories() throws ApiRequestException {
        List<story> allStories = storyService.getAllPreviousData();
        return new ResponseEntity<>(allStories,HttpStatus.OK);
    }
}
