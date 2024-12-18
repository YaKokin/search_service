package school.faang.searchservice.dto.event;

import java.time.LocalDateTime;

public record EventSearchResponse(
        Long id,
        String title,
        String description,
        LocalDateTime startDate,
        LocalDateTime endDate,
        String location,
        Integer maxAttendees,
        String usernameOwner,
        String eventType,
        String eventStatus
) {

}
