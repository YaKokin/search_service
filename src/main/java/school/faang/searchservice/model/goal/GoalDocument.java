package school.faang.searchservice.model.goal;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.Setting;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Document(indexName = "goals")
@Setting(settingPath = "elasticsearch/settings.json")
public class GoalDocument {

    @Id
    private Long goalId;

    @Field(type = FieldType.Text)
    private String title;

    @Field(type = FieldType.Text)
    private String description;

    @Field(type = FieldType.Keyword)
    private GoalStatus status;

    @Field(type = FieldType.Date)
    private LocalDateTime deadline;

    @Field(type = FieldType.Date)
    private LocalDateTime createdAt;

    @Field(type = FieldType.Nested)
    private List<String> skillsToAchieveNames;
}
