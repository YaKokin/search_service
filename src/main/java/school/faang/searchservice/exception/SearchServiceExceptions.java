package school.faang.searchservice.exception;

public class SearchServiceExceptions extends RuntimeException {

    private static final String SEARCHING_USERS_ERROR = "Error while searching users";

    public SearchServiceExceptions(Throwable cause) {
        super(SEARCHING_USERS_ERROR, cause);
    }
}
