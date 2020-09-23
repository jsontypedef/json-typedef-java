package com.jsontypedef.jtd;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonElement;

/**
 * An implementation of {@code Json} for Gson.
 */
public class GsonAdapter implements Json {
  private JsonElement jsonElement;

  /**
   * Constructs a {@code GsonAdapter} that wraps a Gson {@code JsonElement}.
   *
   * @param jsonElement the Gson value to wrap
   */
  public GsonAdapter(JsonElement jsonElement) {
    this.jsonElement = jsonElement;
  }

  @Override
  public boolean isNull() {
    return jsonElement.isJsonNull();
  }

  @Override
  public boolean isBoolean() {
    return jsonElement.isJsonPrimitive() && jsonElement.getAsJsonPrimitive().isBoolean();
  }

  @Override
  public boolean isNumber() {
    return jsonElement.isJsonPrimitive() && jsonElement.getAsJsonPrimitive().isNumber();
  }

  @Override
  public boolean isString() {
    return jsonElement.isJsonPrimitive() && jsonElement.getAsJsonPrimitive().isString();
  }

  @Override
  public boolean isArray() {
    return jsonElement.isJsonArray();
  }

  @Override
  public boolean isObject() {
    return jsonElement.isJsonObject();
  }

  @Override
  public boolean asBoolean() {
    return jsonElement.getAsJsonPrimitive().getAsBoolean();
  }

  @Override
  public double asNumber() {
    return jsonElement.getAsJsonPrimitive().getAsDouble();
  }

  @Override
  public String asString() {
    return jsonElement.getAsJsonPrimitive().getAsString();
  }

  @Override
  public List<Json> asArray() {
    List<Json> arr = new ArrayList<>();
    for (JsonElement element : jsonElement.getAsJsonArray()) {
      arr.add(new GsonAdapter(element));
    }

    return arr;
  }

  @Override
  public Map<String, Json> asObject() {
    Map<String, Json> obj = new HashMap<>();
    for (Map.Entry<String, JsonElement> entry : jsonElement.getAsJsonObject().entrySet()) {
      obj.put(entry.getKey(), new GsonAdapter(entry.getValue()));
    }

    return obj;
  }
}
