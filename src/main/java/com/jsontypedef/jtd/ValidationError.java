package com.jsontypedef.jtd;

import java.util.List;

public class ValidationError {
  private List<String> instancePath;
  private List<String> schemaPath;

  public ValidationError() {
  }

  public ValidationError(List<String> instancePath, List<String> schemaPath) {
    this.instancePath = instancePath;
    this.schemaPath = schemaPath;
  }

  public List<String> getInstancePath() {
    return instancePath;
  }

  public void setInstancePath(List<String> instancePath) {
    this.instancePath = instancePath;
  }

  public List<String> getSchemaPath() {
    return schemaPath;
  }

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
