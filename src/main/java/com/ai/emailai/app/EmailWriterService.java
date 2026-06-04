package com.ai.emailai.app;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import java.util.*;

@Service
public class EmailWriterService {

    private final WebClient webClient;
    @Value("${gemini.api.url}")
    private String geminiApiUrl;
    @Value("${gemini.api.key}")
    private String geminiApiKey;
    public EmailWriterService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    public String generateEmailReply (EmailRequest emailRequest){
           String prompt=buildPrompt(emailRequest);
           Map<String,Object> requestBody=Map.of(
                   "contents",List.of(
                           Map.of("parts",List.of(
                                   Map.of("text",prompt)
                           ))
                        )
           );
           String res="";
           try {
               res = webClient.post()
                       .uri(geminiApiUrl + "?key=" + geminiApiKey)
                       .contentType(MediaType.APPLICATION_JSON)
                       .bodyValue(requestBody)
                       .retrieve()
                       .onStatus(
                               HttpStatusCode::isError,
                               clientResponse -> clientResponse
                                       .bodyToMono(String.class)
                                       .flatMap(errorBody -> {
                                           System.out.println("GEMINI ERROR: " + errorBody);
                                           return Mono.error(new RuntimeException(errorBody));
                                       })
                       )
                       .bodyToMono(String.class)
                       .block();
//               res = webClient.post()
//                       .uri(geminiApiUrl + geminiApiKey)
//                       .contentType(MediaType.APPLICATION_JSON)
//                       .bodyValue(requestBody)
//                       .retrieve()
//
//                       .bodyToMono(String.class)
//                       .block();
               return extractResponse(res);
           }catch(Exception e){
               return "ERROR:" + e.getMessage();
           }


    }

    private String extractResponse(String response) {
         try{
             ObjectMapper mapper =new ObjectMapper();
             JsonNode rootNode  = mapper.readTree(response);
             return rootNode.path("candidates")
                     .get(0)
                     .path("content")
                     .path("parts")
                     .get(0)
                     .path("text").asString();
         }catch(Exception e){
             return "error while processing req"+ e.getMessage();
         }
    }

    private String buildPrompt(EmailRequest emailRequest) {
        StringBuilder prompt=new StringBuilder();
        prompt.append("generate a professional email reply for the following email content and please dont generate a subject line.");
        if(emailRequest.getTone()!=null&&!emailRequest.getTone().isEmpty()){
            prompt.append("Use a ").append(emailRequest.getTone()).append(" tone.");
        }
        prompt.append("\nOriginal email: \n").append(emailRequest.getEmailContent());
        return prompt.toString();
    }
}
