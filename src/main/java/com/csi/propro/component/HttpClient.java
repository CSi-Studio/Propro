package com.csi.propro.component;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Component("httpClient")
public class HttpClient {

    public String client(String url, String body){
        RestTemplate restTemplate = new RestTemplate();
//        MultiValueMap<String, Object> postParameters = new LinkedMultiValueMap<>();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");
        HttpEntity<String> r = new HttpEntity<>(body, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(url, r, String.class);
        return response.getBody();
    }
}
