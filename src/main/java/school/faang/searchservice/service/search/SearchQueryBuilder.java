package school.faang.searchservice.service.search;

import co.elastic.clients.elasticsearch._types.SortOptions;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import org.springframework.data.domain.Pageable;
import school.faang.searchservice.service.search.filter.Filter;

import java.util.ArrayList;
import java.util.List;

public class SearchQueryBuilder {

    private final SearchRequest.Builder searchRequestBuilder;
    private final BoolQuery.Builder boolQueryBuilder;
    private final List<Filter> filters = new ArrayList<>();

    public SearchQueryBuilder() {
        searchRequestBuilder = new SearchRequest.Builder();
        boolQueryBuilder = new BoolQuery.Builder();
    }

    public SearchRequest build() {
        filters.forEach(filter -> filter.apply(boolQueryBuilder));
        return searchRequestBuilder.query(q -> q
                        .bool(boolQueryBuilder.build())
                )
                .build();
    }

    public SearchQueryBuilder indexName(String indexName) {
        searchRequestBuilder.index(indexName);
        return this;
    }

    public SearchQueryBuilder size(int size) {
        searchRequestBuilder.size(size);
        return this;
    }

    public SearchQueryBuilder from(int from) {
        searchRequestBuilder.from(from);
        return this;
    }

    public SearchQueryBuilder addFilter(Filter filter) {
        filters.add(filter);
        return this;
    }

    public SearchQueryBuilder sortOptions(Pageable pageable) {
        if (pageable.getSort().isSorted()) {
            List<SortOptions> sortOptions = pageable.getSort().stream()
                    .map(order -> SortOptions.of(s -> s
                            .field(f -> f
                                    .field(order.getProperty())
                                    .order(order.isAscending() ? SortOrder.Asc : SortOrder.Desc))
                    ))
                    .toList();
            searchRequestBuilder.sort(sortOptions);
        }
        return this;
    }
}
