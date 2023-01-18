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

/**
 * You need to launch manually {@link be.ben.MySutApplication} first before running this test !!!!!!!!!!!!!!
 */
@CitrusSpringSupport
class GetResourceWithSUTTest {
  @Autowired private HttpClient httpClient;

  @Autowired private HttpServer httpMockServer;

  @Test
  @CitrusTest
  void testThatTheMockWillAnswer(@CitrusResource GherkinTestActionRunner runner) {
    // Client query the SUT to get a specific resource
    runner.when(
        http()
          .client(httpClient)
          .send() //
          .post("/resource") //
          .message());

    // Assert that the mock received the outgoing request from the SUT to request a login
    // Test will work if you comment the following assertion
    runner.then(
            http()
              .server(httpMockServer)
              .receive()
              .get("/login"));

    // TODO once the previous assertion work: assert that the server receive the second request for the resource with the jwt

    // Assert that the client will receive finally the resource
    runner.then( //
            http() //
                    .client(httpClient) //
                    .receive() //
                    .response(HttpStatus.OK) //
                    .message()
                    .type(MessageType.JSON) //
                    .validate(
                            jsonPath() //
                                    .expression("$.resource", equalTo("someResource"))));
  }

  @Configuration
  @Import(CitrusSpringConfig.class)
  static class TestContext {
    @Bean
    public HttpClient httpClient() {
      return new HttpClientBuilder()
              // the url of the SUT
          .requestUrl("http://localhost:8080/")
          .requestMethod(HttpMethod.GET)
          .build();
    }

    /**
     * Mocking the external service, which will reply a token to the /login request from the SUT
     */
    @Bean
    public HttpServer httpMockServer() {
      return new HttpServerBuilder()
          .contextPath("/login")
          .port(8081)
          .autoStart(true)
          .timeout(5000L)
          .endpointAdapter(tokenResponseAdapter(null))
          .build();
    }

    /**
     * I didn't even start with a {@link com.consol.citrus.endpoint.adapter.RequestDispatchingEndpointAdapter} to handle
     * the second query that actually query the resource with the received token, because it fails before.
     */
    @Bean
    public EndpointAdapter tokenResponseAdapter(TestContextFactory contextFactory) {
        StaticResponseEndpointAdapter endpointAdapter = new StaticResponseEndpointAdapter();
        endpointAdapter.setMessagePayload("someBase64Token");
        endpointAdapter.setTestContextFactory(contextFactory);
        return endpointAdapter;
    }
  }
}
