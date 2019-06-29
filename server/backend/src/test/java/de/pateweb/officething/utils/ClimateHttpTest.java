package de.pateweb.officething.utils;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.Charset;
import java.util.Arrays;

import org.apache.tomcat.util.codec.binary.Base64;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class ClimateHttpTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    public void getLastClimateTest() throws Exception {
    	
        // HttpHeaders
        HttpHeaders headers = new HttpHeaders();    	
    	
        // 
        // Authentication
        // 
        String auth = "doghunter" + ":" + "doghunter09";
        byte[] encodedAuth = Base64.encodeBase64(auth.getBytes(Charset.forName("US-ASCII")));
        String authHeader = "Basic " + new String(encodedAuth);
        headers.set("Authorization", authHeader);
        // 
        headers.setAccept(Arrays.asList(new MediaType[] { MediaType.APPLICATION_JSON }));
        // Request to return JSON format
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("my_other_key", "my_other_value");
 
        // HttpEntity<String>: To get result as String.
        HttpEntity<String> entity = new HttpEntity<String>(headers);
 
        // RestTemplate
        RestTemplate restTemplate = new RestTemplate();
 
        // Send request with GET method, and Headers.
        ResponseEntity<String> response = restTemplate.exchange("http://localhost:" + port + "/climate/last",
                HttpMethod.GET, entity, String.class);
 
        String result = response.getBody();    	
    	
        assertThat(result).contains("temperature");
    }	
	
}
