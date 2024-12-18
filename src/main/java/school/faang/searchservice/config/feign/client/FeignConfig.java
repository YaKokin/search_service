package school.faang.searchservice.config.feign.client;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import school.faang.searchservice.config.context.UserContext;

@Configuration
public class FeignConfig {

    @Bean
    public FeignUserInterceptor feignUserInterceptor(UserContext userContext) {
        return new FeignUserInterceptor(userContext);
    }
}
