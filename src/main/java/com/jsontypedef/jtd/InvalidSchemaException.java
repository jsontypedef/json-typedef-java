package com.jsontypedef.jtd;

/**
 * The exception raised from {@code verify} on {@code Schema} if a schema is
 * invalid.
 */
public class InvalidSchemaException extends Exception {
  private static final long serialVersionUID = 5646571806222003913L;

  /**
   * Constructs an {@code InvalidSchemaException} with the given error message.
   *
   * @param msg a message describing the sort of problem with the schema
   */
  public InvalidSchemaException(String msg) {
    super(msg);
  }
}
