package com.jsontypedef.jtd;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;

public class JacksonAdapter implements Json {
  private JsonNode jsonNode;

  public JacksonAdapter(JsonNode jsonNode) {
    this.jsonNode = jsonNode;
  }

  @Override
  public boolean isNull() {
    return jsonNode.isNull();
  }

  @Override
  public boolean isBoolean() {
    return jsonNode.isBoolean();
  }

  @Override
  public boolean isNumber() {
    return jsonNode.isNumber();
  }

  @Override
  public boolean isString() {
    return jsonNode.isTextual();
  }

  @Override
  public boolean isArray() {
    return jsonNode.isArray();
  }

  @Override
  public boolean isObject() {
    return jsonNode.isObject();
  }

  @Override
  public boolean asBoolean() {
    return jsonNode.asBoolean();
  }

  @Override
  public double asNumber() {
    return jsonNode.asDouble();
  }

  @Override
  public String asString() {
    return jsonNode.asText();
  }

  @Override
  public List<Json> asArray() {
    List<Json> arr = new ArrayList<>();
    for (JsonNode node : jsonNode) {
      arr.add(new JacksonAdapter(node));
    }

    return arr;
  }

  @Override
  public Map<String, Json> asObject() {
    Map<String, Json> obj = new HashMap<>();
    jsonNode.fields().forEachRemaining(entry -> obj.put(entry.getKey(), new JacksonAdapter(entry.getValue())));

    return obj;
  }
}
