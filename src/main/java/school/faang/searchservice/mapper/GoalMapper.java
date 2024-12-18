package school.faang.searchservice.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import school.faang.searchservice.dto.goal.GoalSearchResponse;
import school.faang.searchservice.model.goal.GoalStatus;
import school.faang.searchservice.model.user.GoalNested;

@Mapper(componentModel = "spring",
        unmappedSourcePolicy = ReportingPolicy.IGNORE)
public interface GoalMapper {

    GoalSearchResponse toSearchResponse(GoalNested goal);

    default String mapStatus(GoalStatus status) {
        return status != null ? status.name() : null;
    }
}
