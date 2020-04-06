package com.jsontypedef.jtd;

import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Arrays;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

public class SchemaTest {
  // We ignore these spec test cases because Gson's behavior in many cases is to
  // either type-cast or ignore properties which have the wrong type.
  //
  // Broadly speaking, these are test cases that it would be un-Java-like, or
  // un-Gson-like, to attempt to handle.
  private static final List<String> IGNORED_SPEC_TESTS = Arrays.asList("type not string", "type not valid string value",
      "enum not array of strings", "enum contains duplicates", "discriminator not string", "illegal keyword");

  @TestFactory
  public List<DynamicTest> testVerify() throws JsonIOException, JsonSyntaxException, UnsupportedEncodingException {
    ClassLoader classLoader = this.getClass().getClassLoader();
    InputStream inputStream = classLoader.getResourceAsStream("json-typedef-spec/tests/invalid_schemas.json");
    Gson gson = new Gson();

    Map<String, JsonElement> testCases = gson.fromJson(new InputStreamReader(inputStream, "UTF-8"),
        new TypeToken<Map<String, JsonElement>>() {
        }.getType());

    List<DynamicTest> tests = new ArrayList<>();
    for (Map.Entry<String, JsonElement> testCase : testCases.entrySet()) {
      tests.add(DynamicTest.dynamicTest(testCase.getKey(), () -> {
        assumeFalse(IGNORED_SPEC_TESTS.contains(testCase.getKey()));

        try {
          Schema schema = gson.fromJson(testCase.getValue(), Schema.class);
          schema.verify();
        } catch (NullPointerException | JsonSyntaxException | InvalidSchemaException e) {
          return;
        }

        fail();
      }));
    }

    return tests;
  }
}
