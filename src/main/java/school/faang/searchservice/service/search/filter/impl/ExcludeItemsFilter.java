package school.faang.searchservice.service.search.filter.impl;

import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import lombok.RequiredArgsConstructor;
import school.faang.searchservice.service.search.filter.Filter;
import school.faang.searchservice.util.CollectionUtils;

import java.util.List;

@RequiredArgsConstructor
public class ExcludeItemsFilter implements Filter {

    private final List<Long> excludedUserIds;

    private static final String USER_ID_FIELD = "userId";

    @Override
    public void apply(BoolQuery.Builder boolQuery) {
        if (CollectionUtils.isNotEmpty(excludedUserIds)) {
            List<FieldValue> fieldValues = excludedUserIds.stream()
                    .map(FieldValue::of)
                    .toList();

            Query excludeQuery = Query.of(query -> query
                    .terms(t -> t
                            .field(USER_ID_FIELD)
                            .terms(t2 -> t2.value(fieldValues))
                    ));
            boolQuery.mustNot(excludeQuery);
        }
    }
}
