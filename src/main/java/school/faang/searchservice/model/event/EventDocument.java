package school.faang.searchservice.model.event;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.Setting;
import school.faang.searchservice.model.BaseDocument;

import java.time.LocalDateTime;

@Data
@Document(indexName = "events")
@Setting(settingPath = "elasticsearch/settings.json")
@EqualsAndHashCode(callSuper = true)
public class EventDocument extends BaseDocument {

    @Field(type = FieldType.Text)
    private String tittle;

    @Field(type = FieldType.Text)
    private String description;

    @Field(type = FieldType.Date)
    private LocalDateTime startDate;

    @Field(type = FieldType.Date)
    private LocalDateTime endDate;

    @Field(type = FieldType.Text)
    private String location;

    @Field(type = FieldType.Integer)
    private Integer maxAttendees;

    @Field(type = FieldType.Integer)
    private String usernameOwner;

    @Field(type = FieldType.Keyword)
    private EventType eventType;

    @Field(type = FieldType.Keyword)
    private EventStatus status;
}
