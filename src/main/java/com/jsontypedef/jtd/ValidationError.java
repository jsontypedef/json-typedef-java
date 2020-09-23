package com.jsontypedef.jtd;

import java.util.List;

/**
 * Represents a single validation problem, typically returned from
 * {@code verify} in {@code Validator}.
 *
 * Note well that this class is not an {@code Exception}. It is a plain old Java
 * object.
 */
public class ValidationError {
  private List<String> instancePath;
  private List<String> schemaPath;

  /**
   * Constructs a new validation error with an uninitialized
   * {@code instancePath} and {@code schemaPath}.
   */
  public ValidationError() {
  }

  /**
   * Constructs a new validation error with the given instance and schema paths.
   *
   * @param instancePath the instance path
   * @param schemaPath the schema path
   */
  public ValidationError(List<String> instancePath, List<String> schemaPath) {
    this.instancePath = instancePath;
    this.schemaPath = schemaPath;
  }

  /**
   * Gets the error's instance path, which is a "pointer" to the part of the
   * instance that was rejected.
   *
   * @return the instance path
   */
  public List<String> getInstancePath() {
    return instancePath;
  }

  /**
   * Sets the error's instance path.
   *
   * @param instancePath the instance path
   */
  public void setInstancePath(List<String> instancePath) {
    this.instancePath = instancePath;
  }

  /**
   * Gets the error's schema path, which is a "pointer" to the part of the
   * schema that was rejected.
   *
   * @return the schema path
   */
  public List<String> getSchemaPath() {
    return schemaPath;
  }

  /**
   * Sets the error's schema path.
   *
   * @param schemaPath the schema path
   */
  public void setSchemaPath(List<String> schemaPath) {
    this.schemaPath = schemaPath;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((instancePath == null) ? 0 : instancePath.hashCode());
    result = prime * result + ((schemaPath == null) ? 0 : schemaPath.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    ValidationError other = (ValidationError) obj;
    if (instancePath == null) {
      if (other.instancePath != null)
        return false;
    } else if (!instancePath.equals(other.instancePath))
      return false;
    if (schemaPath == null) {
      if (other.schemaPath != null)
        return false;
    } else if (!schemaPath.equals(other.schemaPath))
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "ValidationError [instancePath=" + instancePath + ", schemaPath=" + schemaPath + "]";
  }
}
