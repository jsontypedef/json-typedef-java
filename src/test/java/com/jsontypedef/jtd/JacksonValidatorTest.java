package com.jsontypedef.jtd;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

public class JacksonValidatorTest {
  @Test
  public void testMaxDepth() throws JsonMappingException, JsonProcessingException {
    ObjectMapper objectMapper = new ObjectMapper();
    Schema schema = objectMapper.readValue("{\"definitions\": {\"x\": {\"ref\": \"x\"}}, \"ref\": \"x\"}",
        Schema.class);

    Validator validator = new Validator();
    validator.setMaxDepth(3);
    assertThrows(MaxDepthExceededException.class, () -> validator.validate(schema, null));
  }

  @Test
  public void testMaxErrors() throws MaxDepthExceededException, JsonMappingException, JsonProcessingException {
    ObjectMapper objectMapper = new ObjectMapper();
    Schema schema = objectMapper.readValue("{\"elements\": {\"type\": \"string\"}}", Schema.class);

    JsonNode instance = objectMapper.readTree("[1, 1, 1, 1, 1]");

    Validator validator = new Validator();
    validator.setMaxErrors(3);
    assertEquals(3, validator.validate(schema, new JacksonAdapter(instance)).size());
  }

  // We ignore two test cases from the standard spec. Both of these are due to
  // the fact that the Java standard library's version of RFC3339 does not
  // support leap seconds.
  private static final List<String> IGNORED_SPEC_TESTS = Arrays.asList("timestamp type schema - 1990-12-31T23:59:60Z",
      "timestamp type schema - 1990-12-31T15:59:60-08:00");

  @TestFactory
  public List<DynamicTest> testValidate()
      throws JsonParseException, JsonMappingException, UnsupportedEncodingException, IOException {
    ClassLoader classLoader = this.getClass().getClassLoader();
    InputStream inputStream = classLoader.getResourceAsStream("json-typedef-spec/tests/validation.json");
    ObjectMapper objectMapper = new ObjectMapper();

    Map<String, TestCase> testCases = objectMapper.readValue(new InputStreamReader(inputStream, "UTF-8"),
        new TypeReference<Map<String, TestCase>>() {
        });

    List<DynamicTest> tests = new ArrayList<>();
    for (Map.Entry<String, TestCase> testCase : testCases.entrySet()) {
      tests.add(DynamicTest.dynamicTest(testCase.getKey(), () -> {
        assumeFalse(IGNORED_SPEC_TESTS.contains(testCase.getKey()));
        testCase.getValue().schema.verify();

        List<ValidationError> expected = testCase.getValue().errors;
        List<ValidationError> actual = new Validator().validate(testCase.getValue().schema,
            new JacksonAdapter(testCase.getValue().instance));

        expected.sort((e1, e2) -> {
          String a = String.join("/", e1.getSchemaPath()) + ":" + String.join("/", e1.getInstancePath());
          String b = String.join("/", e2.getSchemaPath()) + ":" + String.join("/", e2.getInstancePath());
          return a.compareTo(b);
        });

        actual.sort((e1, e2) -> {
          String a = String.join("/", e1.getSchemaPath()) + ":" + String.join("/", e1.getInstancePath());
          String b = String.join("/", e2.getSchemaPath()) + ":" + String.join("/", e2.getInstancePath());
          return a.compareTo(b);
        });

        assertEquals(expected, actual);
      }));
    }

    return tests;
  }

  private static class TestCase {
    public Schema schema;
    public JsonNode instance;
    public List<ValidationError> errors;
  }
}
