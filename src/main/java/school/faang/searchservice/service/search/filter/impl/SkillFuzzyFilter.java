package school.faang.searchservice.service.search.filter.impl;

import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import lombok.RequiredArgsConstructor;
import school.faang.searchservice.service.search.filter.Filter;
import school.faang.searchservice.util.CollectionUtils;

import java.util.List;

@RequiredArgsConstructor
public class SkillFuzzyFilter implements Filter {

    private final List<String> skillNames;

    private static final String SKILL_NAMES_FIELD = "skillNames";
    private static final String FUZZINESS_VALUE = "AUTO";

    @Override
    public void apply(BoolQuery.Builder boolQuery) {
        if (CollectionUtils.isNotEmpty(skillNames)) {
            boolQuery.must(createSkillFuzzyQueries(skillNames));
        }
    }

    private List<Query> createSkillFuzzyQueries(List<String> skillNames) {
        return skillNames.stream()
                .map(this::createSkillFuzzyQuery)
                .toList();
    }

    private Query createSkillFuzzyQuery(String skillName) {
        return Query.of(q -> q
                .fuzzy(f -> f
                        .field(SKILL_NAMES_FIELD)
                        .value(skillName)
                        .fuzziness(FUZZINESS_VALUE)
                ));
    }
}
