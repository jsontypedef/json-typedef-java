package com.jsontypedef.jtd;

import com.google.gson.annotations.SerializedName;

/**
 * Type represents the eleven values the {@code type} keyword can take in JDDF.
 */
public enum Type {
  @SerializedName("boolean")
  BOOLEAN,

  @SerializedName("float32")
  FLOAT32,

  @SerializedName("float64")
  FLOAT64,

  @SerializedName("int8")
  INT8,

  @SerializedName("uint8")
  UINT8,

  @SerializedName("int16")
  INT16,

  @SerializedName("uint16")
  UINT16,

  @SerializedName("int32")
  INT32,

  @SerializedName("uint32")
  UINT32,

  @SerializedName("string")
  STRING,

  @SerializedName("timestamp")
  TIMESTAMP,
}
