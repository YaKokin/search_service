package school.faang.searchservice.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import school.faang.searchservice.dto.event.EventSearchResponse;
import school.faang.searchservice.model.event.EventStatus;
import school.faang.searchservice.model.event.EventType;
import school.faang.searchservice.model.user.EventNested;

@Mapper(componentModel = "spring", unmappedSourcePolicy = ReportingPolicy.IGNORE)
public interface EventMapper {

    EventSearchResponse toSearchResponse(EventNested searchResponse);

    default String mapEventType(EventType eventType) {
        return eventType != null ? eventType.name() : null;
    }

    default String mapEventStatus(EventStatus eventStatus) {
        return eventStatus != null ? eventStatus.name() : null;
    }
}
