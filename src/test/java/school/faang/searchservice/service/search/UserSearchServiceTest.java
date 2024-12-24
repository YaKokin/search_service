package school.faang.searchservice.service.search;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.ShardStatistics;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.core.search.HitsMetadata;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;
import school.faang.searchservice.builder.SearchQueryBuilder;
import school.faang.searchservice.dto.user.UserSearchRequest;
import school.faang.searchservice.dto.user.UserSearchResponse;
import school.faang.searchservice.mapper.UserMapper;
import school.faang.searchservice.model.user.UserDocument;
import school.faang.searchservice.service.cache.SessionResourceService;
import school.faang.searchservice.service.search.promotions.ResourcePromotionService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserSearchServiceTest {

    @Mock
    private ElasticsearchClient elasticsearchClient;

    @Mock
    private SessionResourceService<UserDocument> sessionResourceService;

    @Mock
    private ResourcePromotionService<UserDocument, UserSearchRequest> resourcePromotionService;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserSearchService userSearchService;

    @Value("${spring.elasticsearch.indexes.user_search.name}")
    private String userSearchIndex;

    private String sessionId;
    private UserSearchRequest request;
    private Pageable pageable;
    private SearchQueryBuilder queryBuilder;
    private List<Long> viewedResourceIds;
    private List<UserDocument> documents;
    private UserDocument userDocument1;
    private UserDocument userDocument2;
    private Hit<UserDocument> hit1;
    private Hit<UserDocument> hit2;
    private List<Hit<UserDocument>> hits;
    private HitsMetadata<UserDocument> hitsMetadata;
    private SearchResponse<UserDocument> searchResponse;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(userSearchService, "userSearchIndex", "user_search_index");
        sessionId = "test-session";
        request = new UserSearchRequest();
        pageable = PageRequest.of(0, 10);
        queryBuilder = new SearchQueryBuilder();

        viewedResourceIds = List.of(1L, 2L);

        userDocument1 = UserDocument.builder().resourceId(5L).build();
        userDocument2 = UserDocument.builder().resourceId(6L).build();

        documents = List.of(
                userDocument1,
                userDocument2
        );

        hit1 = new Hit.Builder<UserDocument>()
                .id("id")
                .index("index")
                .source(userDocument1)
                .build();
        hit2 = new Hit.Builder<UserDocument>()
                .id("id")
                .index("index")
                .source(userDocument2)
                .build();
        hits = Arrays.asList(hit1, hit2);

        hitsMetadata = new HitsMetadata.Builder<UserDocument>()
                .hits(hits)
                .build();
        searchResponse = new SearchResponse.Builder<UserDocument>()
                .hits(hitsMetadata)
                .took(1000L)
                .timedOut(true)
                .shards(new ShardStatistics.Builder()
                        .failed(0)
                        .successful(1)
                        .total(1)
                        .build())
                .build();
    }

    @Test
    void searchUsers_ShouldReturnPagedResults() throws IOException {

        UserSearchResponse response1 = UserSearchResponse.builder()
                .userId(1L)
                .build();
        UserSearchResponse response2 = UserSearchResponse.builder()
                .userId(2L)
                .build();
        List<UserSearchResponse> responses = List.of(response1, response2);

        when(elasticsearchClient.search(any(SearchRequest.class), eq(UserDocument.class)))
                .thenReturn(searchResponse);

        when(userMapper.toResponseList(documents)).thenReturn(responses);

        Page<UserSearchResponse> result = userSearchService.searchUsers(sessionId, request, pageable);

        assertNotNull(result);
        assertEquals(responses.size(), result.getContent().size());
        assertEquals(0, result.getNumber());
        assertEquals(10, result.getSize());

        verify(elasticsearchClient).search(any(SearchRequest.class), eq(UserDocument.class));
        verify(userMapper).toResponseList(documents);
    }

    @Test
    void searchUsers_WithEmptyResult_ShouldReturnEmptyPage() throws IOException {
        HitsMetadata<UserDocument> hitsMetadata1 = new HitsMetadata.Builder<UserDocument>()
                .hits(new ArrayList<>())
                .build();
        SearchResponse<UserDocument> emptySearchResponse = new SearchResponse.Builder<UserDocument>()
                .hits(hitsMetadata1)
                .took(1000L)
                .timedOut(true)
                .shards(new ShardStatistics.Builder()
                        .failed(0)
                        .successful(1)
                        .total(1)
                        .build())
                .build();

        when(elasticsearchClient.search(any(SearchRequest.class), eq(UserDocument.class)))
                .thenReturn(emptySearchResponse);

        when(userMapper.toResponseList(Collections.emptyList())).thenReturn(Collections.emptyList());

        Page<UserSearchResponse> result = userSearchService.searchUsers(sessionId, request, pageable);

        assertNotNull(result);
        assertTrue(result.getContent().isEmpty());
        assertEquals(0, result.getTotalElements());
    }
}
