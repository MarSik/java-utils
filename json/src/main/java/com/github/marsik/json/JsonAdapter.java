package com.github.marsik.json;

import java.io.IOException;
import java.io.Reader;

import org.zalando.jackson.datatype.money.MoneyModule;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.util.ISO8601DateFormat;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import lombok.EqualsAndHashCode;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JsonAdapter {
    private final ObjectMapper objectMapper;

    public final static String TYPE_FIELD = "@type";

    public JsonAdapter() {
        objectMapper = new ObjectMapper()
                .registerModule(new ParameterNamesModule())
                .registerModule(new Jdk8Module())
                .registerModule(new JavaTimeModule())
                .registerModule(new MoneyModule());

        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        objectMapper.setDateFormat(ISO8601DateFormat.getDateTimeInstance());
    }

    @JsonTypeInfo(use=JsonTypeInfo.Id.NAME, include=JsonTypeInfo.As.PROPERTY, property=TYPE_FIELD)
    private class TypeInfoMixIn {}

    @Value
    public static class TypeInfo<T> {
        final TypeReference<T> reference;

        public TypeInfo() {
            this.reference = new TypeReference<T>() {};
        }

        public TypeInfo(TypeReference<T> reference) {
            this.reference = reference;
        }
    }

    /**
     * registerSubtypes provides a way to tell json parser how to
     * interpret the @type value (it points the parser to the children)
     * @param cls list of classes that can be returned when parsing polymorphic input
     */
    public JsonAdapter registerSubtypes(Class<?>... cls) {
        objectMapper.registerSubtypes(cls);
        return this;
    }

    @Value
    @EqualsAndHashCode(callSuper = true)
    public static class JsonCreateException extends IOException {
        private JsonProcessingException exception;
    }

    public class SequentialReader {
        private final JsonParser parser;

        protected SequentialReader(String input) throws IOException {
            parser = objectMapper.getFactory().createParser(input);
        }

        protected SequentialReader(Reader input) throws IOException {
            parser = objectMapper.getFactory().createParser(input);
        }

        public <T> T read(Class<T> type) throws IOException {
            findNextObject(parser);

            try {
                return parser.readValueAs(type);
            } catch (IOException ex) {
                readRemainderOfFailedAttempt(parser);
                return null;
            }
        }

        public <T> T read(TypeInfo<T> type) throws IOException {
            findNextObject(parser);

            if (parser.isClosed()) {
                return null;
            }

            try {
                final T value = parser.readValueAs(type.getReference());
                return value;
            } catch (IOException ex) {
                readRemainderOfFailedAttempt(parser);
                throw ex;
            }
        }

        private void findNextObject(JsonParser parser) throws IOException {
            while (!parser.isClosed() && parser.currentToken() != JsonToken.START_OBJECT) {
                parser.nextToken();
            }
        }

        /**
         * A nice marker of a next record is end object token immediately followed
         * by start object token "} {" as this is normally illegal in JSON
         */
        private void readRemainderOfFailedAttempt(JsonParser parser) throws IOException {
            while (!parser.isClosed()) {
                JsonToken token = parser.currentToken();
                JsonToken nextToken = parser.nextToken();
                if (token == JsonToken.END_OBJECT
                        && nextToken == JsonToken.START_OBJECT) {
                    break;
                }

                log.debug("Skipping json token: {}", token);
            }
        }

        public boolean isClosed() {
            return parser.isClosed();
        }
    }

    /**
     * fromJson constructs a reader object that can read multiple
     * json objects coming from the provided input after each other
     *
     * @param input input contains the multiobject json
     * @return returns reader instance
     * @throws IOException
     */
    public SequentialReader fromJson(String input) throws IOException {
        return new SequentialReader(input);
    }

    /**
     * fromJson constructs a reader object that can read multiple
     * json objects coming from the provided input after each other
     *
     * @param input input contains the multiobject json
     * @return returns reader instance
     * @throws IOException
     */
    public SequentialReader fromJson(Reader input) throws IOException {
        return new SequentialReader(input);
    }

    public <T> T fromJson(String input, TypeInfo<T> outputType) throws IOException {
        return objectMapper.readValue(input, outputType.getReference());
    }

    public <T> T fromJson(Reader input, TypeInfo<T> outputType) throws IOException {
        return objectMapper.readValue(input, outputType.getReference());
    }

    public <T> T fromJson(String input, Class<T> outputType) throws IOException {
        return objectMapper.readValue(input, outputType);
    }

    public <T> T fromJson(Reader input, Class<T> outputType) throws IOException {
        return objectMapper.readValue(input, outputType);
    }

    public String toJson(Object object) throws JsonCreateException {
        try {
            return objectMapper.writerFor(Object.class).writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new JsonCreateException(e);
        }
    }

    public boolean isValidJson(String input) {
        try {
            objectMapper.readTree(input);
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}
