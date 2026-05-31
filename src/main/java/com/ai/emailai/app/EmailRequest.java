package com.ai.emailai.app;

import lombok.Data;

@Data
public class EmailRequest {
    private String tone;
    private String emailContent;
}
