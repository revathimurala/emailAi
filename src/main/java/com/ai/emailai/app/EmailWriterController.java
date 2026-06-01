package com.ai.emailai.app;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/email")
@AllArgsConstructor
@CrossOrigin(origins="http://localhost:5173/")
public class EmailWriterController {

    private final EmailWriterService emailWriterService;
    @PostMapping("/generate")
    public ResponseEntity<String> generateEmail(@RequestBody EmailRequest emailRequest){
        String response =emailWriterService.generateEmailReply(emailRequest);
        return ResponseEntity.ok(response);
    }
}
