package school.faang.searchservice.client;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import school.faang.searchservice.config.feign.client.FeignConfig;
import school.faang.searchservice.dto.user.UserSearchRequest;

import java.util.List;

@FeignClient(
        name = "promotion-service",
        url = "${promotion-service.service.url}",
        configuration = FeignConfig.class
)
public interface PromotionClient {

    @PostMapping("/api/v1/promotions/search/users")
    List<Long> searchPromotedUsers(
            @RequestParam("requiredResCount") @Positive Integer requiredResCount,
            @RequestParam("sessionId") @NotBlank String sessionId,
            @RequestBody @Validated UserSearchRequest userSearchRequest
    );
}
