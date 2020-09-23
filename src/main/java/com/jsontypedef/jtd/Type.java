package com.jsontypedef.jtd;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;

/**
 * Type represents the values the {@code type} keyword can take in JTD.
 */
public enum Type {
  /**
   * The {@code boolean} type.
   */
  @SerializedName("boolean")
  @JsonProperty("boolean")
  BOOLEAN,

  /**
   * The {@code float32} type.
   */
  @SerializedName("float32")
  @JsonProperty("float32")
  FLOAT32,

  /**
   * The {@code float64} type.
   */
  @SerializedName("float64")
  @JsonProperty("float64")
  FLOAT64,

  /**
   * The {@code int8} type.
   */
  @SerializedName("int8")
  @JsonProperty("int8")
  INT8,

  /**
   * The {@code uint8} type.
   */
  @SerializedName("uint8")
  @JsonProperty("uint8")
  UINT8,

  /**
   * The {@code int16} type.
   */
  @SerializedName("int16")
  @JsonProperty("int16")
  INT16,

  /**
   * The {@code uint16} type.
   */
  @SerializedName("uint16")
  @JsonProperty("uint16")
  UINT16,

  /**
   * The {@code int32} type.
   */
  @SerializedName("int32")
  @JsonProperty("int32")
  INT32,

  /**
   * The {@code uint32} type.
   */
  @SerializedName("uint32")
  @JsonProperty("uint32")
  UINT32,

  /**
   * The {@code string} type.
   */
  @SerializedName("string")
  @JsonProperty("string")
  STRING,

  /**
   * The {@code timestamp} type.
   */
  @SerializedName("timestamp")
  @JsonProperty("timestamp")
  TIMESTAMP,
}
