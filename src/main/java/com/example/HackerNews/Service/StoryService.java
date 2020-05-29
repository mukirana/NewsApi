package com.example.HackerNews.Service;

import com.example.HackerNews.Constant;
import com.example.HackerNews.Exception.ApiException;
import com.example.HackerNews.Exception.ApiRequestException;
import com.example.HackerNews.Helper;
import com.example.HackerNews.Model.Comment;
import com.example.HackerNews.Model.HackerNewsStory;
import com.example.HackerNews.Model.User;
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
        List<story> topTenStory=   getSortedStoriesByScore(list);
        for (story st: topTenStory) {
            List<story> ob = redisRepository.find(st.getId());
            if(ob==null){
                storyRepository.save(st);
                List<story> storyList = new ArrayList<>();
                storyList.add(st);
                redisRepository.save(st.getId(),storyList,1, TimeUnit.DAYS);
            }
        }
        redisRepository.save(Constant.Stories,topTenStory,10,TimeUnit.MINUTES);
        return topTenStory;
    }




    public  List<story> getSortedStoriesByScore(List<Long> data) {
        List<story> list = new ArrayList<>();
        String Url = Constant.itemsUrl+data.toString()+Constant.json;
        List<CompletableFuture<story>> pageContentFutures = data.stream()
                .map(val -> helper.getDataAsync(val,Constant.itemsUrl,story.class,Constant.Stories+val))
                .collect(Collectors.toList());
        CompletableFuture<List<story>> completeData = helper.getDataList(pageContentFutures);
        list = completeData.join();
        Collections.sort(list);
        for (story st: list) {
            List<story> storyList = new ArrayList<>();
            storyList.add(st);
            redisRepository.save(Constant.Stories+st.getId(),storyList,1, TimeUnit.DAYS);
        }

        return list.stream().limit(10).collect(Collectors.toList());
    }



    public List<Comment> getTopComments(String id){
        String url = Constant.itemsUrl+id+Constant.json;
        // to get the given story data........
        ResponseEntity<HackerNewsStory> result = restTemplate.getForEntity(url, HackerNewsStory.class);
        HackerNewsStory response = result.getBody();
        List<Long> kids = response.getKids();


        // to get the list of parent comments......
        List<CompletableFuture<HackerNewsStory>> pageContentFutures = kids.stream()
                .map(comment ->  helper.getDataAsync(comment,Constant.itemsUrl,HackerNewsStory.class,Constant.Comment+comment))
                .collect(Collectors.toList());


        CompletableFuture<List<HackerNewsStory>> completeData = helper.getDataList(pageContentFutures);

        List<HackerNewsStory> listHackerNewsStory= completeData.join();

         listHackerNewsStory = listHackerNewsStory.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        for (HackerNewsStory comment: listHackerNewsStory) {
            List<HackerNewsStory> storyList = new ArrayList<>();
            storyList.add(comment);
            redisRepository.save(Constant.Comment+comment.getId(),storyList,1, TimeUnit.DAYS);
        }

        // to get the  list of Users
        List<CompletableFuture<User>> userDetail = listHackerNewsStory.stream()
                .map( hackerNewsStory->
                {
                    if(hackerNewsStory.getBy()==null){
                       return null;
                    }
                    return helper.DataAsync(hackerNewsStory.getBy(), Constant.userUrl, User.class, hackerNewsStory.getBy());
                })
                .collect(Collectors.toList());

         userDetail = userDetail.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        CompletableFuture<List<User>> completeUserData = helper.getDataList(userDetail);

        List<User> userData = new ArrayList<>();
        userData = completeUserData.join();

        userData = userData.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        for (User user: userData) {
            if(user==null){
                continue;
            }
            List<User> userList = new ArrayList<>();
            userList.add(user);
            redisRepository.save(user.getId(),userList,1, TimeUnit.DAYS);
        }


        HashMap<String,Long> hm = new HashMap<>();
        for (User user:userData) {
            if(user==null){
                continue;
            }
            hm.put(user.getId(),user.getCreated());
        }



        // It will combine the data of user and comments and then return  collection in sorted order.
        List<Comment> allCommentWithUserDetailList = helper.getCombinedCommentAndUserData(listHackerNewsStory,hm);

        Collections.sort(allCommentWithUserDetailList);

       return allCommentWithUserDetailList.stream().limit(10).collect(Collectors.toList());
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
