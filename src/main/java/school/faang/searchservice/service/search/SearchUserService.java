package school.faang.searchservice.service.search;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import school.faang.searchservice.client.PromotionClient;
import school.faang.searchservice.dto.user.UserSearchRequest;
import school.faang.searchservice.dto.user.UserSearchResponse;
import school.faang.searchservice.exception.SearchServiceExceptions;
import school.faang.searchservice.mapper.UserMapper;
import school.faang.searchservice.model.user.UserDocument;
import school.faang.searchservice.rpeository.UserDocumentRepository;
import school.faang.searchservice.service.cache.SessionResourceService;
import school.faang.searchservice.service.search.filter.impl.ExcludeItemsFilter;
import school.faang.searchservice.service.search.filter.impl.RangeFilter;
import school.faang.searchservice.service.search.filter.impl.SkillFuzzyFilter;
import school.faang.searchservice.service.search.filter.impl.TextMatchFilter;
import school.faang.searchservice.util.CollectionUtils;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SearchUserService {

    private static final double SHARE_OF_PROMOTIONS = 0.4;
    private static final String USERS_INDEX = "user_indexing_topic";
    private static final String SEARCHING_USERS_ERROR = "Error while searching users";

    private final ElasticsearchClient elasticsearchClient;
    private final PromotionClient promotionClient;
    private final UserDocumentRepository userDocumentRepository;
    private final SessionResourceService sessionResourceService;
    private final UserMapper userMapper;

    public Page<UserSearchResponse> searchUsers(String sessionId, UserSearchRequest userSearchRequest,
                                                Pageable pageable) {

        //получение просмотренных
        List<Long> viewedUserIds = sessionResourceService.getViewedResources(sessionId);

        //получение промоутированных
        Integer requiredPromotionsCount = (int) Math.floor(pageable.getPageSize() * SHARE_OF_PROMOTIONS);
        List<UserDocument> promotedUserDocs = getPromotedUsers(requiredPromotionsCount, sessionId, userSearchRequest);

        //получение оставшихся
        Integer remainingPositionsCount = pageable.getPageSize() - promotedUserDocs.size();
        List<Long> promotedUserIds = promotedUserDocs.stream().map(UserDocument::getUserId).toList();
        List<UserDocument> regularUserDocs = searchUsersByFilter(
                userSearchRequest,
                CollectionUtils.merge(viewedUserIds, promotedUserIds),
                remainingPositionsCount,
                pageable
        );

        //мерж результата
        List<UserDocument> searchResult = CollectionUtils.merge(regularUserDocs, promotedUserDocs);

        //сохранение как просмотренных
        saveUsersAsViewed(sessionId, searchResult);

        //возвращаем ответ
        return new PageImpl<>(userMapper.toResponseList(searchResult), pageable, searchResult.size());
    }

    private void saveUsersAsViewed(String sessionId, List<UserDocument> searchResult) {
        if (!searchResult.isEmpty()) {
            List<Long> newViewedResourceIds = searchResult.stream()
                    .map(UserDocument::getUserId)
                    .toList();
            sessionResourceService.addViewedResources(sessionId, newViewedResourceIds);
        }
    }

    private List<UserDocument> getPromotedUsers(Integer requiredPromotionsCount,
                                                String sessionId,
                                                UserSearchRequest userSearchRequest) {

        List<Long> promotedUserIds =
                promotionClient.searchPromotedUsers(requiredPromotionsCount, sessionId, userSearchRequest);

        return userDocumentRepository.findAllByUserIdIn(promotedUserIds);
    }

    private List<UserDocument> searchUsersByFilter(UserSearchRequest searchRequest,
                                                   List<Long> excludedUsersIds,
                                                   Integer maxResults,
                                                   Pageable pageable) {

        int from = pageable.getPageNumber() * pageable.getPageSize();
        SearchRequest request = new SearchQueryBuilder()
                .indexName(USERS_INDEX)
                .size(maxResults)
                .from(from)
                .addFilter(new ExcludeItemsFilter(excludedUsersIds))
                .addFilter(new TextMatchFilter(searchRequest.query()))
                .addFilter(new SkillFuzzyFilter(searchRequest.skillNames()))
                .addFilter(new RangeFilter(searchRequest.experienceFrom(), searchRequest.experienceTo()))
                .sortOptions(pageable)
                .build();

        try {
            SearchResponse<UserDocument> searchResponse =
                    elasticsearchClient.search(request, UserDocument.class);

            return searchResponse.hits().hits().stream()
                    .map(Hit::source)
                    .toList();
        } catch (IOException e) {
            log.error(SEARCHING_USERS_ERROR, e);
            throw new SearchServiceExceptions(SEARCHING_USERS_ERROR, e);
        }
    }
}
