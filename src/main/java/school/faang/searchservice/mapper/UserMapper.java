package school.faang.searchservice.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import school.faang.searchservice.dto.user.UserSearchResponse;
import school.faang.searchservice.model.user.UserDocument;

import java.util.List;

@Mapper(componentModel = "spring", uses = {GoalMapper.class, EventMapper.class},
        unmappedSourcePolicy = ReportingPolicy.IGNORE)
public interface UserMapper {

    @Mapping(source = "resourceId", target = "userId")
    UserSearchResponse toSearchResponse(UserDocument userDocument);

    List<UserSearchResponse> toResponseList(List<UserDocument> userDocuments);
}
