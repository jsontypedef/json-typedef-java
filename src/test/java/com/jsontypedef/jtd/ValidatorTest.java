package com.jsontypedef.jtd;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

public class ValidatorTest {
  @Test
  public void testMaxDepth() {
    Gson gson = new Gson();
    Schema schema = gson.fromJson("{\"definitions\": {\"x\": {\"ref\": \"x\"}}, \"ref\": \"x\"}", Schema.class);

    Validator validator = new Validator();
    validator.setMaxDepth(3);
    assertThrows(MaxDepthExceededException.class, () -> validator.validate(schema, null));
  }

  @Test
  public void testMaxErrors() throws MaxDepthExceededException {
    Gson gson = new Gson();
    Schema schema = gson.fromJson("{\"elements\": {\"type\": \"string\"}}", Schema.class);
    JsonElement instance = gson.fromJson("[1, 1, 1, 1, 1]", JsonElement.class);

    Validator validator = new Validator();
    validator.setMaxErrors(3);
    assertEquals(3, validator.validate(schema, instance).size());
  }

  // We ignore two test cases from the standard spec. Both of these are due to
  // the fact that the Java standard library's version of RFC3339 does not
  // support leap seconds.
  private static final List<String> IGNORED_SPEC_TESTS = Arrays.asList("timestamp type schema - 1990-12-31T23:59:60Z",
      "timestamp type schema - 1990-12-31T15:59:60-08:00");

  @TestFactory
  public List<DynamicTest> testValidate() throws JsonIOException, JsonSyntaxException, UnsupportedEncodingException {
    ClassLoader classLoader = this.getClass().getClassLoader();
    InputStream inputStream = classLoader.getResourceAsStream("json-typedef-spec/tests/validation.json");
    Gson gson = new Gson();

    Map<String, TestCase> testCases = gson.fromJson(new InputStreamReader(inputStream, "UTF-8"),
        new TypeToken<Map<String, TestCase>>() {
        }.getType());

    List<DynamicTest> tests = new ArrayList<>();
    for (Map.Entry<String, TestCase> testCase : testCases.entrySet()) {
      tests.add(DynamicTest.dynamicTest(testCase.getKey(), () -> {
        assumeFalse(IGNORED_SPEC_TESTS.contains(testCase.getKey()));
        testCase.getValue().schema.verify();

        assertEquals(testCase.getValue().errors,
            new Validator().validate(testCase.getValue().schema, testCase.getValue().instance));
      }));
    }

    return tests;
  }

  private static class TestCase {
    private Schema schema;
    private JsonElement instance;
    private List<ValidationError> errors;
  }
}
