package com.jsontypedef.jtd;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;

/**
 * Type represents the values the {@code type} keyword can take in JTD.
 */
public enum Type {
  @SerializedName("boolean")
  @JsonProperty("boolean")
  BOOLEAN,

  @SerializedName("float32")
  @JsonProperty("float32")
  FLOAT32,

  @SerializedName("float64")
  @JsonProperty("float64")
  FLOAT64,

  @SerializedName("int8")
  @JsonProperty("int8")
  INT8,

  @SerializedName("uint8")
  @JsonProperty("uint8")
  UINT8,

  @SerializedName("int16")
  @JsonProperty("int16")
  INT16,

  @SerializedName("uint16")
  @JsonProperty("uint16")
  UINT16,

  @SerializedName("int32")
  @JsonProperty("int32")
  INT32,

  @SerializedName("uint32")
  @JsonProperty("uint32")
  UINT32,

  @SerializedName("string")
  @JsonProperty("string")
  STRING,

  @SerializedName("timestamp")
  @JsonProperty("timestamp")
  TIMESTAMP,
}
