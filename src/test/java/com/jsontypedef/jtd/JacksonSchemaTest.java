package com.jsontypedef.jtd;

import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Arrays;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

public class JacksonSchemaTest {
  // We ignore these spec test cases because Jackson's behavior in many cases is
  // to either type-cast or ignore properties which have the wrong type.
  //
  // Broadly speaking, these are test cases that it would be un-Java-like, or
  // un-Jackson-like, to attempt to handle.
  private static final List<String> IGNORED_SPEC_TESTS = Arrays.asList("nullable not boolean",
      "enum not array of strings", "enum contains duplicates", "additionalProperties not boolean",
      "discriminator not string");

  @TestFactory
  public List<DynamicTest> testVerify() throws JsonParseException, JsonMappingException, IOException {
    ClassLoader classLoader = this.getClass().getClassLoader();
    InputStream inputStream = classLoader.getResourceAsStream("json-typedef-spec/tests/invalid_schemas.json");
    ObjectMapper objectMapper = new ObjectMapper();

    Map<String, JsonNode> testCases = objectMapper.readValue(new InputStreamReader(inputStream, "UTF-8"),
        new TypeReference<Map<String, JsonNode>>() {
        });

    List<DynamicTest> tests = new ArrayList<>();
    for (Map.Entry<String, JsonNode> testCase : testCases.entrySet()) {
      tests.add(DynamicTest.dynamicTest(testCase.getKey(), () -> {
        assumeFalse(IGNORED_SPEC_TESTS.contains(testCase.getKey()));

        try {
          Schema schema = objectMapper.treeToValue(testCase.getValue(), Schema.class);
          schema.verify();

          System.out.println(testCase.getKey());
          System.out.println(schema);
        } catch (NullPointerException | MismatchedInputException | InvalidSchemaException e) {
          return;
        }

        fail();
      }));
    }

    return tests;
  }
}
