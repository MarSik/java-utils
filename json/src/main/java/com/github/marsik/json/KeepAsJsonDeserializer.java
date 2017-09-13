package com.github.marsik.json;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

/**
 * KeepAsJsonDeserializer is used to be able to read raw json data into
 * string member variable.
 *
 * Use in DTO classes like this:
 *
 * @JsonDeserialize(using=KeepAsJsonDeserializer.class)
 * @JsonRawValue
 * String jsonContent;
 *
 * The JsonRawValue annotation instructs Jackson to insert the content
 * to the output stream as-is - without encoding. This is necessary as
 * the value is already a valid json.
 */
public class KeepAsJsonDeserializer extends JsonDeserializer<String> {
    @Override
    public String deserialize(JsonParser jp, DeserializationContext ctxt)
            throws IOException, JsonProcessingException {

        TreeNode tree = jp.getCodec().readTree(jp);
        return tree.toString();
    }
}
