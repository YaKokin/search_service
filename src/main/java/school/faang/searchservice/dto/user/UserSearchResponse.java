package school.faang.searchservice.dto.user;

import lombok.Builder;
import school.faang.searchservice.dto.event.EventSearchResponse;
import school.faang.searchservice.dto.goal.GoalSearchResponse;

import java.util.List;

@Builder
public record UserSearchResponse(
        Long userId,
        String username,
        String country,
        String city,
        Integer experience,
        List<GoalSearchResponse> goals,
        List<String> skillNames,
        List<EventSearchResponse> events,
        Double averageRating
) {
}
