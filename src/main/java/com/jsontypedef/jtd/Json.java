package com.jsontypedef.jtd;

import java.util.List;
import java.util.Map;

/**
 * An implementation-independent representation of the JSON data model.
 *
 * Different Java JSON implementations, such as Gson or Jackson, have different
 * models of JSON data. The {@code Json} interface unifies these different
 * implementations of the JSON data model.
 *
 * This interface presumes that a JSON document can be exactly one of
 * {@code null}, a boolean, a number (equivalent to a {@code double}; this
 * includes both integers and floating-point numbers), a string, an array of
 * sub-{@code Json} elements, or an object mapping strings to sub-{@code Json}
 * values.
 *
 * Every instance of Json must return true for exactly one of {@code isNull},
 * {@code isBoolean}, {@code isNumber}, {@code isString}, {@code isArray}, or
 * {@code isObject}.
 *
 * The {@code GsonAdapter} and {@code JacksonAdapter} classes are
 * implementations of {@code Json} for the widely-used Gson and Jackson
 * libraries.
 */
public interface Json {
  /**
   * Gets whether the JSON value corresponds to JSON {@code null}.
   *
   * @return whether the value is null
   */
  public boolean isNull();

  /**
   * Gets whether the JSON value corresponds to JSON {@code true} or {@code false}.
   *
   * @return whether the value is boolean
   */
  public boolean isBoolean();

  /**
   * Gets whether the JSON value corresponds to a JSON number.
   *
   * Note that some implementations of JSON distinguish between numbers whose
   * JSON textual representation contains a "." or not, for instance
   * distinguishing between "10.0" and "10". {@code isNumber} makes no such
   * distinction; all "integers" and "non-integral numbers" are numbers for the
   * purposes of this method.
   *
   * @return whether the value is a number
   */
  public boolean isNumber();

  /**
   * Gets whether the JSON value corresponds to a JSON string.
   *
   * @return whether the value is a string
   */
  public boolean isString();

  /**
   * Gets whether the JSON value corresponds to a JSON array.
   *
   * @return whether the value is an array
   */
  public boolean isArray();

  /**
   * Gets whether the JSON value corresponds to a JSON object.
   *
   * @return whether the value is an object
   */
  public boolean isObject();

  /**
   * Gets the JSON value as a boolean.
   *
   * The behavior of this method is undefined if {@code isBoolean} does not
   * return true.
   *
   * @return the Java boolean value of this JSON value
   */
  public boolean asBoolean();

  /**
   * Gets the JSON value as a double.
   *
   * The behavior of this method is undefined if {@code isNumber} does not
   * return true.
   *
   * @return the Java double value of this JSON value
   */
  public double asNumber();

  /**
   * Gets the JSON value as a string.
   *
   * The behavior of this method is undefined if {@code isString} does not
   * return true.
   *
   * @return the Java string value of this JSON value
   */
  public String asString();

  /**
   * Gets the JSON value as an array.
   *
   * The behavior of this method is undefined if {@code isArray} does not return
   * true.
   *
   * @return the Java List value of this JSON value
   */
  public List<Json> asArray();

  /**
   * Gets the JSON value as an object.
   *
   * The behavior of this method is undefined if {@code isObject} does not
   * return true.
   *
   * @return the Java Map value of this JSON value
   */
  public Map<String, Json> asObject();
}
