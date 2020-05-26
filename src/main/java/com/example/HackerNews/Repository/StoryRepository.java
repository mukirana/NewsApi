package com.example.HackerNews.Repository;

import com.example.HackerNews.Model.story;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface StoryRepository extends MongoRepository<story,String> {
}
