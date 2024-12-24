package school.faang.searchservice.controller;

import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import school.faang.searchservice.dto.user.UserSearchRequest;
import school.faang.searchservice.dto.user.UserSearchResponse;
import school.faang.searchservice.exception.DataValidationException;
import school.faang.searchservice.service.search.UserSearchService;

@RestController
@RequestMapping("/api/v1/search/users")
@RequiredArgsConstructor
@Validated
@Slf4j
public class UserSearchV1Controller {

    private final UserSearchService userSearchService;

    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final String DEFAULT_SORT_FIELD = "averageRating";

    @PostMapping
    @ResponseStatus(HttpStatus.OK)
    public Page<UserSearchResponse> searchUsers(
            @RequestParam @NotBlank String sessionId,
            @RequestBody @Validated UserSearchRequest userSearchRequest,

            @PageableDefault(
                    size = DEFAULT_PAGE_SIZE,
                    sort = DEFAULT_SORT_FIELD,
                    page = 0,
                    direction = Sort.Direction.DESC
            )
            Pageable pageable
    ) {
        log.info("Received search request:");
        validateRequest(userSearchRequest);
        return userSearchService.searchUsers(sessionId, userSearchRequest, pageable);
    }

    private static void validateRequest(UserSearchRequest userSearchRequest) {
        if (!userSearchRequest.expBoundsIsNotNull()) {
            throw new DataValidationException("experience bounds not be null");
        }
    }
}
