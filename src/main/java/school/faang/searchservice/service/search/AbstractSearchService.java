package school.faang.searchservice.service.search;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import school.faang.searchservice.client.PromotionClient;
import school.faang.searchservice.dto.user.UserSearchRequest;
import school.faang.searchservice.model.user.UserDocument;
import school.faang.searchservice.service.cache.SessionResourceService;
import school.faang.searchservice.util.CollectionUtils;

import java.util.List;

@RequiredArgsConstructor
public abstract class AbstractSearchService <REQ, RES> {

    private static final double SHARE_OF_PROMOTIONS = 0.4;

    private final SessionResourceService sessionResourceService;
    private final PromotionClient promotionClient;

    public Page<RES> search(String sessionId, REQ request, Pageable pageable) {
        List<Long> viewedUserIds = sessionResourceService.getViewedResources(sessionId);

        Integer requiredPromotionsCount = (int) Math.floor(pageable.getPageSize() * SHARE_OF_PROMOTIONS);
        List<UserDocument> promotedUserDocs = getPromotedUsers(requiredPromotionsCount, sessionId, request);

        Integer remainingPositionsCount = pageable.getPageSize() - promotedUserDocs.size();
        List<Long> promotedUserIds = promotedUserDocs.stream().map(UserDocument::getUserId).toList();
        List<UserDocument> regularUserDocs = searchUsersByFilter(
                userSearchRequest,
                CollectionUtils.merge(viewedUserIds, promotedUserIds),
                remainingPositionsCount,
                pageable
        );

        List<UserDocument> searchResult = CollectionUtils.merge(regularUserDocs, promotedUserDocs);

        saveUsersAsViewed(sessionId, searchResult);

        return new PageImpl<>(userMapper.toResponseList(searchResult), pageable, searchResult.size());
    }

    private List<UserDocument> getPromotedUsers(Integer requiredPromotionsCount,
                                                String sessionId,
                                                UserSearchRequest userSearchRequest) {

        List<Long> promotedUserIds =
                promotionClient.searchPromotedUsers(requiredPromotionsCount, sessionId, userSearchRequest);

        return userDocumentRepository.findAllByUserIdIn(promotedUserIds);
    }
}
