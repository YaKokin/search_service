package school.faang.searchservice.service.search.promotions;

import java.util.List;

public interface ResourcePromotionService<DOC, REQ> {

    List<DOC> getPromotedResources(Integer requiredPromotionsCount,
                                   String sessionId,
                                   REQ userSearchRequest);
}
