package school.faang.searchservice.service.cache;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class SessionResourceService {

    private final RedisTemplate<String, Long> redisTemplate;

    private static final String KEY_PREFIX = "session";

    public List<Long> getViewedResources(String sessionId) {
        String key = toKey(sessionId);
        Set<Long> viewedResourceIds = redisTemplate.opsForSet().members(key);
        return viewedResourceIds == null ? new ArrayList<>()
                : new ArrayList<>(viewedResourceIds);
    }

    public void addViewedResources(String sessionId, List<Long> viewedResourceIds) {
        String key = toKey(sessionId);
        redisTemplate.opsForSet().add(key, viewedResourceIds.toArray(new Long[0]));
    }

    private String toKey(String sessionId) {
        return String.format("%s:%s", KEY_PREFIX, sessionId);
    }
}
