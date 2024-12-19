package school.faang.searchservice.dto.user;

import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Data
@RequiredArgsConstructor
public class UserAbstractSearchRequest extends AbstractSearchRequest {

    private String query;
    private List<String> skillNames;

    @Positive
    private Integer experienceFrom;

    @Positive
    private Integer experienceTo;

    public boolean expBoundsIsNotNull() {
        return experienceFrom != null && experienceTo != null;
    }
}
