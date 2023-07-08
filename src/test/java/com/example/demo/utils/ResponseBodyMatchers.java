package com.example.demo.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.api.AssertionsForClassTypes;
import org.springframework.test.web.servlet.ResultMatcher;

public class ResponseBodyMatchers {
    private final ObjectMapper objectMapper = new ObjectMapper();

    static public ResponseBodyMatchers response() {
        return new ResponseBodyMatchers();
    }

    public <T> ResultMatcher containsObjectAsJson(Object expectedObject, TypeReference<T> targetClass) {
        return mvcResult -> {
            String json = mvcResult.getResponse().getContentAsString();
            T actualObject = objectMapper.readValue(json, targetClass);

            AssertionsForClassTypes.assertThat(actualObject).isEqualTo(expectedObject);
        };
    }
}
