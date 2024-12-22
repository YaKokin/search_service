package school.faang.searchservice.service.cache;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import school.faang.searchservice.model.user.UserDocument;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
public class SessionResourceServiceTest {

    @Mock
    private RedisTemplate<String, Long> redisTemplate;

    @Mock
    private SetOperations<String, Long> setOperations;

    private SessionResourceService<UserDocument> sessionResourceService;

    private static final String TEST_RESOURCE_PREFIX = "test";

    @BeforeEach
    void setUp() {
        sessionResourceService = new SessionResourceService<UserDocument>(redisTemplate, TEST_RESOURCE_PREFIX) {};

        when(redisTemplate.opsForSet()).thenReturn(setOperations);
    }

    @Test
    void getViewedResources_WhenResourcesExist_ShouldReturnList() {
        String sessionId = "test-session";
        String expectedKey = "session:test:test-session";
        Set<Long> mockViewedResources = new HashSet<>(Arrays.asList(1L, 2L, 3L));

        when(setOperations.members(expectedKey)).thenReturn(mockViewedResources);

        List<Long> result = sessionResourceService.getViewedResources(sessionId);

        assertNotNull(result);
        assertEquals(3, result.size());
        assertTrue(result.containsAll(Arrays.asList(1L, 2L, 3L)));

        verify(redisTemplate).opsForSet();
        verify(setOperations).members(expectedKey);
    }

    @Test
    void getViewedResources_WhenNoResources_ShouldReturnEmptyList() {
        String sessionId = "test-session";
        String expectedKey = "session:test:test-session";

        when(setOperations.members(expectedKey)).thenReturn(null);

        List<Long> result = sessionResourceService.getViewedResources(sessionId);

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(redisTemplate).opsForSet();
        verify(setOperations).members(expectedKey);
    }

    @Test
    void addViewedResources_ShouldAddToRedis() {
        String sessionId = "test-session";
        String expectedKey = "session:test:test-session";
        List<Long> resourceIds = Arrays.asList(1L, 2L, 3L);

        sessionResourceService.addViewedResources(sessionId, resourceIds);

        verify(redisTemplate).opsForSet();
        verify(setOperations).add(eq(expectedKey), any());
    }

    @Test
    void addViewedResources_WithEmptyList_ShouldStillCallRedis() {
        String sessionId = "test-session";
        String expectedKey = "session:test:test-session";
        List<Long> emptyList = Collections.emptyList();

        sessionResourceService.addViewedResources(sessionId, emptyList);

        verify(redisTemplate).opsForSet();
        verify(setOperations).add(eq(expectedKey), any(Long[].class));
    }
}
