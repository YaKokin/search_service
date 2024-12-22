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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;
import school.faang.searchservice.builder.SearchQueryBuilder;
import school.faang.searchservice.dto.user.UserSearchRequest;
import school.faang.searchservice.exception.SearchServiceExceptions;
import school.faang.searchservice.model.user.UserDocument;
import school.faang.searchservice.service.cache.SessionResourceService;
import school.faang.searchservice.service.search.promotions.ResourcePromotionService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AbstractSearchServiceTest {

    @Mock
    private ElasticsearchClient elasticsearchClient;

    @Mock
    private SessionResourceService<UserDocument> sessionResourceService;

    @Mock
    private ResourcePromotionService<UserDocument, UserSearchRequest> resourcePromotionService;

    @InjectMocks
    private UserSearchService searchService;

    private String sessionId;
    private UserSearchRequest request;
    private Pageable pageable;
    private SearchQueryBuilder queryBuilder;
    private List<Long> viewedResourceIds;
    private List<UserDocument> promotedDocs;
    private UserDocument userDocument1;
    private UserDocument userDocument2;
    private Hit<UserDocument> hit1;
    private Hit<UserDocument> hit2;
    private List<Hit<UserDocument>> hits;
    private HitsMetadata<UserDocument> hitsMetadata1;
    private SearchResponse<UserDocument> searchResponse1;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(searchService, "shareOfPromotions", 0.3);
        sessionId = "test-session";
        request = new UserSearchRequest();
        pageable = PageRequest.of(0, 10);
        queryBuilder = new SearchQueryBuilder();

        viewedResourceIds = List.of(1L, 2L);
        promotedDocs = List.of(
                UserDocument.builder().resourceId(3L).build(),
                UserDocument.builder().resourceId(4L).build()
        );

        userDocument1 = UserDocument.builder().resourceId(5L).build();
        userDocument2 = UserDocument.builder().resourceId(6L).build();


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

        hitsMetadata1 = new HitsMetadata.Builder<UserDocument>()
                .hits(hits)
                .build();
        searchResponse1 = new SearchResponse.Builder<UserDocument>()
                .hits(hitsMetadata1)
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
    void searchResources_Success() throws IOException {

        when(sessionResourceService.getViewedResources(sessionId)).thenReturn(viewedResourceIds);
        when(resourcePromotionService.getPromotedResources(3, sessionId, request)).thenReturn(promotedDocs);
        doReturn(searchResponse1).when(elasticsearchClient).search(any(SearchRequest.class), eq(UserDocument.class));

        List<UserDocument> result = searchService.searchResources(
                sessionId, request, pageable, queryBuilder, UserDocument.class
        );

        assertEquals(4, result.size());
        verify(sessionResourceService).addViewedResources(eq(sessionId), anyList());
        verify(elasticsearchClient).search(any(SearchRequest.class), eq(UserDocument.class));
    }

    @Test
    void searchResources_EmptyResult() throws IOException {
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

        when(sessionResourceService.getViewedResources(sessionId)).thenReturn(Collections.emptyList());
        when(resourcePromotionService.getPromotedResources(3, sessionId, request))
                .thenReturn(Collections.emptyList());
        when(elasticsearchClient.search(any(SearchRequest.class), eq(UserDocument.class)))
                .thenReturn(emptySearchResponse);

        List<UserDocument> result = searchService.searchResources(
                sessionId, request, pageable, queryBuilder, UserDocument.class
        );

        assertTrue(result.isEmpty());
        verify(sessionResourceService, never()).addViewedResources(anyString(), anyList());
    }

    @Test
    void searchResources_ElasticsearchException() throws IOException {
        when(sessionResourceService.getViewedResources(sessionId)).thenReturn(Collections.emptyList());
        when(resourcePromotionService.getPromotedResources(3, sessionId, request))
                .thenReturn(Collections.emptyList());
        when(elasticsearchClient.search(any(SearchRequest.class), eq(UserDocument.class)))
                .thenThrow(new IOException("Test exception"));

        assertThrows(SearchServiceExceptions.class, () ->
                searchService.searchResources(sessionId, request, pageable, queryBuilder, UserDocument.class)
        );
    }
}
