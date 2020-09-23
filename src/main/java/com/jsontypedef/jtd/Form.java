package com.jsontypedef.jtd;

/**
 * Form represents the eight forms a JTD schema may take on.
 */
public enum Form {
  /**
   * The empty form.
   *
   * Only the keywords shared by all schema forms ({@code metadata} and
   * {@code nullable}) may be used.
   */
  EMPTY,

  /**
   * The ref form.
   *
   * The {@code ref} keyword is used.
   */
  REF,

  /**
   * The type form.
   *
   * The {@code type} keyword is used.
   */
  TYPE,

  /**
   * The enum form.
   *
   * The {@code enum} keyword is used.
   */
  ENUM,

  /**
   * The elements form.
   *
   * The {@code elements} keyword is used.
   */
  ELEMENTS,

  /**
   * The properties form.
   *
   * One or both of {@code properties} and {@code optionalProperties} is used,
   * and {@code additionalProperites} may be used.
   */
  PROPERTIES,

  /**
   * The values form.
   *
   * The {@code values} keyword is used.
   */
  VALUES,

  /**
   * The discriminator form.
   *
   * Both the {@code discriminator} and {@code mapping} keywords are used.
   */
  DISCRIMINATOR,
}
