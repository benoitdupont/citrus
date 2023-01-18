package be.ben;

import com.consol.citrus.GherkinTestActionRunner;
import com.consol.citrus.annotations.CitrusResource;
import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.config.CitrusSpringConfig;
import com.consol.citrus.context.TestContextFactory;
import com.consol.citrus.endpoint.EndpointAdapter;
import com.consol.citrus.endpoint.adapter.StaticResponseEndpointAdapter;
import com.consol.citrus.http.client.HttpClient;
import com.consol.citrus.http.client.HttpClientBuilder;
import com.consol.citrus.http.server.HttpServer;
import com.consol.citrus.http.server.HttpServerBuilder;
import com.consol.citrus.junit.jupiter.spring.CitrusSpringSupport;
import com.consol.citrus.message.MessageType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

import static com.consol.citrus.http.actions.HttpActionBuilder.http;
import static com.consol.citrus.validation.json.JsonPathMessageValidationContext.Builder.jsonPath;
import static org.hamcrest.Matchers.equalTo;

@CitrusSpringSupport
class GetResourcesWithoutSUTTest {
  @Autowired private HttpClient httpClient;

  @Autowired private HttpServer httpAuthMockServer;

  @Test
  @CitrusTest
  void succeed_when_no__assertions_on_the_mock(@CitrusResource GherkinTestActionRunner runner) {
    // Client query the SUT to get a specific resource
    runner.when(
            http()
                    .client(httpClient)
                    .send() //
                    .post("/login") //
                    .message() //
                    .body("{\"username\": \"login\", \"password\": \"passwd\"}")); //

    // Client receive a username/password (yes it make no senses I know)
    runner.then( //
            http() //
                    .client(httpClient) //
                    .receive() //
                    .response(HttpStatus.OK) //
                    .message()
                    .type(MessageType.JSON) //
                    .validate(
                            jsonPath() //
                                    .expression("$.token", equalTo("someBase64Token"))));
  }

  @Test
  @CitrusTest
  void failing_when_assertions_on_the_mock(@CitrusResource GherkinTestActionRunner runner) {
    // Client query the SUT to get a specific resource
    runner.when(
        http()
          .client(httpClient)
          .send() //
          .post("/login") //
          .message() //
          .body("{\"username\": \"login\", \"password\": \"passwd\"}")); //

    // This is where it will fails:
    runner.then(
            http()
              .server(httpAuthMockServer)
              .receive()
              .post()
              .message()
              .type(MessageType.JSON) //
              .validate(
                  jsonPath() //
                      .expression("$.username", equalTo("login")) //
                      .expression("$.password", equalTo("passwd")))); //

    // Client receive a username/password (yes it make no senses I know)
    runner.then( //
            http() //
                    .client(httpClient) //
                    .receive() //
                    .response(HttpStatus.OK) //
                    .message()
                    .type(MessageType.JSON) //
                    .validate(
                            jsonPath() //
                                    .expression("$.token", equalTo("someBase64Token"))));
  }

  @Configuration
  @Import(CitrusSpringConfig.class)
  static class TestContext {
    @Bean
    public HttpClient httpClient() {
      return new HttpClientBuilder()
          .requestUrl("http://localhost:8082/external-ws-mock")
          .requestMethod(HttpMethod.POST)
          .contentType("application/json")
          .charset("UTF-8")
          .timeout(60000L)
          .build();
    }

    @Bean
    public HttpServer httpAuthMockServer() {
      return new HttpServerBuilder()
          .contextPath("/external-ws-mock")
          .port(8082)
          .autoStart(true)
          .timeout(5000L)
          .endpointAdapter(tokenResponseAdapter(null))
          .build();
    }

    @Bean
    public EndpointAdapter tokenResponseAdapter(TestContextFactory contextFactory) {
        StaticResponseEndpointAdapter endpointAdapter = new StaticResponseEndpointAdapter();
        endpointAdapter.setMessagePayload("{ \"token\": \"someBase64Token\" }");
        endpointAdapter.setTestContextFactory(contextFactory);
        return endpointAdapter;
    }
  }
}
