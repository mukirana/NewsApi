package com.example.HackerNews.Controller;

import com.example.HackerNews.Helper;
import com.example.HackerNews.Model.Comment;
import com.example.HackerNews.Model.story;
import com.example.HackerNews.Repository.RedisRepository;
import com.example.HackerNews.Repository.StoryRepository;
import com.example.HackerNews.Service.StoryService;
import org.springframework.beans.factory.annotation.Autowired;
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

    @GetMapping("/home")
    public List getMessage() throws ExecutionException, InterruptedException {
        List<story> cachedTopStory = redisRepository.findByValue("Stories");
        if(cachedTopStory!=null && cachedTopStory.size()!=0){
            return cachedTopStory;
        }
        ResponseEntity<Long[]> result = restTemplate.getForEntity(baseUrl+"topstories.json", Long[].class);
        List<Long> list = Arrays.asList(result.getBody());

        List<story> topTenStory= storyService.getSortedStoriesByScore(list,Math.min(10,list.size()));

        for (story st: topTenStory) {
             List<story> ob = redisRepository.findById(st.getId());
            if(ob==null){
                storyRepository.save(st);
                List<story> storyList = new ArrayList<>();
                storyList.add(st);
                redisRepository.save(st.getId(),storyList,1, TimeUnit.DAYS);
            }
        }
        redisRepository.saveWithValue("Stories",topTenStory,10,TimeUnit.MINUTES);
        return topTenStory;

    }

    @GetMapping("/comments/{id}")
    public List<Comment> getTopComments(@PathVariable("id")String id){
       // ResponseEntity<story> givenStory = restTemplate.getForEntity(baseUrl+"item/"+id+".json", story.class);
       return storyService.getTopComments(id);

    }

    @GetMapping("/pastStories")
    public List<story> getOldStories(){
        return storyRepository.findAll();
    }
}
