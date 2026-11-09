package com.manager.salon.exception;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.OffsetDateTime;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ProblemDetailsResponse(
        String type,
        String title,
        int status,
        String detail,
        String instance,
        OffsetDateTime timestamp,
        List<InvalidParam> invalidParams
) {
    public record InvalidParam(String name, String reason) {}

    public static ProblemDetailsResponse of(String type, String title, int status, String detail, String instance) {
        return new ProblemDetailsResponse(type, title, status, detail, instance, OffsetDateTime.now(), null);
    }

    public static ProblemDetailsResponse ofWithParams(String type, String title, int status, String detail, String instance, List<InvalidParam> params) {
        return new ProblemDetailsResponse(type, title, status, detail, instance, OffsetDateTime.now(), params);
    }
}
