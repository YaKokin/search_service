package school.faang.searchservice.service.search;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import school.faang.searchservice.builder.SearchQueryBuilder;
import school.faang.searchservice.exception.SearchServiceExceptions;
import school.faang.searchservice.model.BaseDocument;
import school.faang.searchservice.service.cache.SessionResourceService;
import school.faang.searchservice.service.search.filter.Filter;
import school.faang.searchservice.service.search.promotions.ResourcePromotionService;
import school.faang.searchservice.util.CollectionUtils;

import java.io.IOException;
import java.util.List;

@RequiredArgsConstructor
@Slf4j
public abstract class AbstractSearchService<DOC extends BaseDocument, REQ> {

    @Value("${promotion.share}")
    private double shareOfPromotions;

    private final ElasticsearchClient elasticsearchClient;
    private final SessionResourceService<DOC> sessionResourceService;
    private final ResourcePromotionService<DOC, REQ> resourcePromotionService;

    @RequiredArgsConstructor
    @SuppressWarnings("InnerClassMayBeStatic")
    private class ExcludeItemsFilter implements Filter {

        private final List<Long> excludedUserIds;

        private static final String RESOURCE_ID_FIELD = "resourceId";

        @Override
        public void apply(BoolQuery.Builder boolQuery) {
            if (CollectionUtils.isNotEmpty(excludedUserIds)) {
                List<FieldValue> fieldValues = excludedUserIds.stream()
                        .map(FieldValue::of)
                        .toList();

                Query excludeQuery = Query.of(query -> query
                        .terms(t -> t
                                .field(RESOURCE_ID_FIELD)
                                .terms(t2 -> t2.value(fieldValues))
                        ));
                boolQuery.mustNot(excludeQuery);
            }
        }
    }

    public List<DOC> searchResources(String sessionId,
                                     REQ request,
                                     Pageable pageable,
                                     SearchQueryBuilder queryBuilder,
                                     Class<DOC> docType) {

        List<Long> viewedUserIds = sessionResourceService.getViewedResources(sessionId);

        Integer requiredPromotionsCount = (int) Math.floor(pageable.getPageSize() * shareOfPromotions);
        List<DOC> promotedUserDocs =
                resourcePromotionService.getPromotedResources(requiredPromotionsCount, sessionId, request);

        int remainingPositionsCount = pageable.getPageSize() - promotedUserDocs.size();
        List<Long> promotedUserIds = promotedUserDocs.stream().map(BaseDocument::getResourceId).toList();

        List<Long> resourceIdsToExclude = CollectionUtils.merge(viewedUserIds, promotedUserIds);
        queryBuilder.addFilter(new ExcludeItemsFilter(resourceIdsToExclude));
        queryBuilder.size(remainingPositionsCount);
        List<DOC> regularUserDocs = searchUsersByFilter(queryBuilder.build(), docType);

        List<DOC> searchResult = CollectionUtils.merge(regularUserDocs, promotedUserDocs);

        saveUsersAsViewed(sessionId, searchResult);

        return searchResult;
    }

    private void saveUsersAsViewed(String sessionId, List<DOC> searchResult) {
        if (!searchResult.isEmpty()) {
            List<Long> newViewedResourceIds = searchResult.stream()
                    .map(BaseDocument::getResourceId)
                    .toList();
            sessionResourceService.addViewedResources(sessionId, newViewedResourceIds);
        }
    }

    private List<DOC> searchUsersByFilter(SearchRequest searchRequest, Class<DOC> docType) {
        try {
            SearchResponse<DOC> searchResponse =
                    elasticsearchClient.search(searchRequest, docType);

            return searchResponse.hits().hits().stream()
                    .map(Hit::source)
                    .toList();
        } catch (IOException e) {
            throw new SearchServiceExceptions(e, docType);
        }
    }
}
