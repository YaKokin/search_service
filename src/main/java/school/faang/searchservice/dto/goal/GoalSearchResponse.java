package school.faang.searchservice.dto.goal;

import java.time.LocalDateTime;
import java.util.List;

public record GoalSearchResponse(
        Long goalId,
        String title,
        String description,
        String status,
        LocalDateTime deadLine,
        LocalDateTime createdAt,
        List<String> skillsToAchieveNames
) {
}
