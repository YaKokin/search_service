package school.faang.searchservice.service.cache;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import school.faang.searchservice.model.user.UserDocument;

@Service
public class SessionUserService extends SessionResourceService<UserDocument> {

    private static final String USER_PREFIX = "user";

    public SessionUserService(RedisTemplate<String, Long> redisTemplate) {
        super(redisTemplate, USER_PREFIX);
    }
}
