package school.faang.searchservice.model;

import jakarta.persistence.MappedSuperclass;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

@Data
@NoArgsConstructor
@AllArgsConstructor
@MappedSuperclass
public class BaseDocument {
    @Id
    protected Long resourceId;
}
