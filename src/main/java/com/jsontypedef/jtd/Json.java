package com.jsontypedef.jtd;

import java.util.List;
import java.util.Map;

public interface Json {
  public boolean isNull();

  public boolean isBoolean();

  public boolean isNumber();

  public boolean isString();

  public boolean isArray();

  public boolean isObject();

  public boolean asBoolean();

  public double asNumber();

  public String asString();

  public List<Json> asArray();

  public Map<String, Json> asObject();
}
