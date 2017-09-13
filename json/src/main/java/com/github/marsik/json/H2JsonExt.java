package com.github.marsik.json;

import java.io.IOException;

import org.zalando.jackson.datatype.money.MoneyModule;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.util.ISO8601DateFormat;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import lombok.Getter;

/**
 * A static class that can be used to provide JSON methods to H2 using its
 * java extension mechanism.
 *
 * http://www.h2database.com/html/features.html#user_defined_functions
 *
 * Ex.:
 *
 * CREATE ALIAS JSON_VALID FOR "com.github.marsik.json.H2JsonExt.isValidJson";
 */
@Getter
public class H2JsonExt {
    private final static ObjectMapper objectMapper = new ObjectMapper();

    public static boolean isValidJson(String input) {
        try {
            objectMapper.readTree(input);
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}
