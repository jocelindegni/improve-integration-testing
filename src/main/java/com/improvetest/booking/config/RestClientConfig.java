package com.improvetest.booking.config;

import com.improvetest.booking.exception.ServerIsDownException;
import com.improvetest.booking.exception.UserNotFoundException;
import com.improvetest.booking.repository.UserAccountRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.config.EmbeddedValueResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration
public class RestClientConfig {


    @Bean
    public UserAccountRepository userAccountRepository(@Value("${user-account.url}") String url, ConfigurableBeanFactory configurableBeanFactory) {

        RestClient restClient = RestClient.builder()
                .baseUrl(url)
                .defaultStatusHandler(HttpStatusCode::is5xxServerError,
                        (request, response) -> {
                            throw new ServerIsDownException();
                        })
                .defaultStatusHandler(httpStatusCode -> 404 == httpStatusCode.value(),
                        (request, response) -> {
                            throw new UserNotFoundException();
                        })
                .build();

        var httpServiceProxyFactory = HttpServiceProxyFactory.builder()
                .embeddedValueResolver(new EmbeddedValueResolver(configurableBeanFactory))
                .exchangeAdapter(RestClientAdapter.create(restClient));

        return httpServiceProxyFactory
                .build()
                .createClient(UserAccountRepository.class);
    }
}
