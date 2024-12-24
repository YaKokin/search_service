package school.faang.searchservice.rpeository;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import school.faang.searchservice.model.user.UserDocument;

import java.util.List;

public interface UserDocumentRepository extends ElasticsearchRepository<UserDocument, Long> {
    List<UserDocument> findAllByResourceIdIn(List<Long> promotedResourceIds);
}
