package be.ben;


import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
public class MyRestController {

    private RestTemplate restTemplate;

    public MyRestController(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder.build();
    }

    @GetMapping
    public ResponseEntity<String> getSomeResource(){
        ResponseEntity<String> forEntityToken = restTemplate.getForEntity("http://localhost:8081/login", String.class);

        ResponseEntity<String> forEntityResource = restTemplate.getForEntity("http://localhost:8081/resource?token=" + forEntityToken.getBody(), String.class);

        return ResponseEntity.ok(forEntityResource.getBody());
    }
}
