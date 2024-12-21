package school.faang.searchservice.service.search;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import school.faang.searchservice.builder.SearchQueryBuilder;
import school.faang.searchservice.dto.user.UserSearchRequest;
import school.faang.searchservice.dto.user.UserSearchResponse;
import school.faang.searchservice.mapper.UserMapper;
import school.faang.searchservice.model.user.UserDocument;
import school.faang.searchservice.service.cache.SessionResourceService;
import school.faang.searchservice.service.search.filter.impl.RangeFilter;
import school.faang.searchservice.service.search.filter.impl.SkillFuzzyFilter;
import school.faang.searchservice.service.search.filter.impl.TextMatchFilter;
import school.faang.searchservice.service.search.promotions.ResourcePromotionService;

import java.util.List;

@Service
public class UserSearchService extends AbstractSearchService<UserDocument, UserSearchRequest> {

    @Value("${spring.elasticsearch.indexes.user_search.name}")
    private String user_search_index;

    private final UserMapper userMapper;

    public UserSearchService(ElasticsearchClient elasticsearchClient,
                             SessionResourceService<UserDocument> sessionResourceService,
                             ResourcePromotionService<UserDocument, UserSearchRequest> resourcePromotionService,
                             UserMapper userMapper) {
        super(elasticsearchClient, sessionResourceService, resourcePromotionService);
        this.userMapper = userMapper;
    }

    public Page<UserSearchResponse> searchUsers(String sessionId,
                                                UserSearchRequest request,
                                                Pageable pageable) {
        int from = pageable.getPageNumber() * pageable.getPageSize();
        SearchQueryBuilder queryBuilder = new SearchQueryBuilder()
                .indexName(user_search_index)
                .from(from)
                .addFilter(new TextMatchFilter(request.getQuery()))
                .addFilter(new SkillFuzzyFilter(request.getSkillNames()))
                .addFilter(new RangeFilter(request.getExperienceFrom(), request.getExperienceTo()))
                .sortOptions(pageable);

        List<UserDocument> result = searchResources(sessionId, request, pageable, queryBuilder, UserDocument.class);
        return new PageImpl<>(userMapper.toResponseList(result), pageable, result.size());
    }
}
