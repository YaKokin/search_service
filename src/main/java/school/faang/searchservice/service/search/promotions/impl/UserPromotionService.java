package school.faang.searchservice.service.search.promotions.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import school.faang.searchservice.client.PromotionClient;
import school.faang.searchservice.dto.user.UserAbstractSearchRequest;
import school.faang.searchservice.model.user.UserDocument;
import school.faang.searchservice.rpeository.UserDocumentRepository;
import school.faang.searchservice.service.search.promotions.ResourcePromotionService;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserPromotionService implements ResourcePromotionService<UserDocument, UserAbstractSearchRequest> {

    private final PromotionClient promotionClient;
    private final UserDocumentRepository userDocumentRepository;

    @Override
    public List<UserDocument> getPromotedResources(Integer requiredPromotionsCount,
                                                   String sessionId,
                                                   UserAbstractSearchRequest userSearchRequest) {
        List<Long> promotedUserIds =
                promotionClient.searchPromotedUsers(requiredPromotionsCount, sessionId, userSearchRequest);

        return userDocumentRepository.findAllByUserIdIn(promotedUserIds);
    }

    @Override
    public Class<UserDocument> getDocType() {
        return UserDocument.class;
    }
}
