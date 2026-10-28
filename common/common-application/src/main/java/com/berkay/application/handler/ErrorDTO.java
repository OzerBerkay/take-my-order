package com.berkay.application.handler;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorDTO {
    private final String code;
    private final String errorCode;
    private final String message;

    public ErrorDTO(String code, String message) {
        this.code = code;
        this.errorCode = code;
        this.message = message;
    }

    public static class ErrorDTOBuilder {
        public ErrorDTOBuilder code(String code) {
            this.code = code;
            if (this.errorCode == null) {
                this.errorCode = code;
            }
            return this;
        }
    }
}

