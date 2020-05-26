package com.example.HackerNews.Repository;

import com.example.HackerNews.Model.story;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Repository
public class RedisRepository {
    private ValueOperations valueOperations;

    private RedisTemplate redisTemplate;

    public RedisRepository(RedisTemplate redisTemplate){
        this.redisTemplate = redisTemplate;
        this.valueOperations = this.redisTemplate.opsForValue();
    }

    public <T> void saveWithValue(String value,List<T> sto,int duration,TimeUnit time){
        valueOperations.set(value,sto,duration,time);
    }
    public <T> T findByValue(String id){
        return (T)valueOperations.get(id);
    }

      public <T> void save(Long id,List<T> sto,int duration,TimeUnit time){
        valueOperations.set(id,sto,duration, time);
     }


    public <T> T findById(Long id){
        return (T)valueOperations.get(id);
    }

    public void delete(Long id){
        redisTemplate.delete(id);
    }
}
