package com.jsontypedef.jtd;

import com.google.gson.annotations.SerializedName;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;

/**
 * Represents a JSON Type Definition schema.
 */
public class Schema {
  private Map<String, Schema> definitions;
  private boolean nullable;
  private Map<String, Object> metadata;
  private String ref;
  private Type type;
  @SerializedName("enum")
  private Set<String> enm;
  private Schema elements;
  private Map<String, Schema> properties;
  private Map<String, Schema> optionalProperties;
  private boolean additionalProperties;
  private Schema values;
  private String discriminator;
  private Map<String, Schema> mapping;

  // Index of valid form "signatures" -- i.e., combinations of the presence of the
  // keywords (in order):
  //
  // ref type enum elements properties optionalProperties additionalProperties
  // values discriminator mapping
  //
  // The keywords "definitions", "nullable", and "metadata" are not included here,
  // because they would restrict nothing.
  private static final boolean[][] VALID_FORMS = {
      // Empty form
      { false, false, false, false, false, false, false, false, false, false },
      // Ref form
      { true, false, false, false, false, false, false, false, false, false },
      // Type form
      { false, true, false, false, false, false, false, false, false, false },
      // Enum form
      { false, false, true, false, false, false, false, false, false, false },
      // Elements form
      { false, false, false, true, false, false, false, false, false, false },
      // Properties form -- properties or optional properties or both, and never
      // additional properties on its own
      { false, false, false, false, true, false, false, false, false, false },
      { false, false, false, false, false, true, false, false, false, false },
      { false, false, false, false, true, true, false, false, false, false },
      { false, false, false, false, true, false, true, false, false, false },
      { false, false, false, false, false, true, true, false, false, false },
      { false, false, false, false, true, true, true, false, false, false },
      // Values form
      { false, false, false, false, false, false, false, true, false, false },
      // Discriminator form
      { false, false, false, false, false, false, false, false, true, true } };

  /**
   * Ensures the schema is a valid root schema.
   *
   * The JSON Type Definition has some rules for the correctness of a schema
   * that go beyond what can be represented in Java's type system. This method
   * will verify these rules, such as ensuring that all references have a
   * corresponding definition, and only valid combinations of JSON Typedef
   * keywords are used.
   *
   * @throws InvalidSchemaException if the schema is not valid
   */
  public void verify() throws InvalidSchemaException {
    verify(this);
  }

  private void verify(Schema root) throws InvalidSchemaException {
    boolean[] formSignature = { this.ref != null, this.type != null, this.enm != null, this.elements != null,
        this.properties != null, this.optionalProperties != null, this.additionalProperties, this.values != null,
        this.discriminator != null, this.mapping != null };

    boolean formOk = false;
    for (boolean[] validForm : VALID_FORMS) {
      formOk = formOk || Arrays.equals(formSignature, validForm);
    }

    if (!formOk) {
      throw new InvalidSchemaException("invalid form");
    }

    if (this.definitions != null) {
      if (this != root) {
        throw new InvalidSchemaException("non-root definition");
      }

      for (Schema schema : this.definitions.values()) {
        schema.verify(root);
      }
    }

    if (this.ref != null) {
      if (root.definitions == null || !root.definitions.containsKey(this.ref)) {
        throw new InvalidSchemaException("ref to non-existent definition");
      }
    }

    if (this.enm != null) {
      if (this.enm.size() == 0) {
        throw new InvalidSchemaException("empty enum");
      }
    }

    if (this.elements != null) {
      this.elements.verify(root);
    }

    if (this.properties != null) {
      for (Schema schema : this.properties.values()) {
        schema.verify(root);
      }
    }

    if (this.optionalProperties != null) {
      for (Schema schema : this.optionalProperties.values()) {
        schema.verify(root);
      }
    }

    if (this.properties != null && this.optionalProperties != null) {
      for (String key : this.properties.keySet()) {
        if (this.optionalProperties.containsKey(key)) {
          throw new InvalidSchemaException("properties shares keys with optionalProperties");
        }
      }
    }

    if (this.values != null) {
      this.values.verify(root);
    }

    if (this.mapping != null) {
      for (Schema schema : this.mapping.values()) {
        schema.verify(root);

        if (schema.nullable) {
          throw new InvalidSchemaException("mapping value has nullable set to true");
        }

        if (schema.getForm() != Form.PROPERTIES) {
          throw new InvalidSchemaException("mapping value not of properties form");
        }

        if (schema.properties != null && schema.properties.containsKey(this.discriminator)) {
          throw new InvalidSchemaException("discriminator shares keys with mapping properties");
        }

        if (schema.optionalProperties != null && schema.optionalProperties.containsKey(this.discriminator)) {
          throw new InvalidSchemaException("discriminator shares keys with mapping optionalProperties");
        }
      }
    }
  }

  /**
   * Gets the form the {@code Schema} takes on.
   *
   * The return value of this method is not meaningful if the {@code Schema} is
   * not a valid schema, such as if calling {@code verify} on its containing
   * root schema raises {@code InvalidSchemaException}.
   *
   * @return the form of the schema
   */
  public Form getForm() {
    if (this.ref != null) {
      return Form.REF;
    } else if (this.type != null) {
      return Form.TYPE;
    } else if (this.enm != null) {
      return Form.ENUM;
    } else if (this.elements != null) {
      return Form.ELEMENTS;
    } else if (this.properties != null) {
      return Form.PROPERTIES;
    } else if (this.optionalProperties != null) {
      return Form.PROPERTIES;
    } else if (this.values != null) {
      return Form.VALUES;
    } else if (this.discriminator != null) {
      return Form.DISCRIMINATOR;
    } else {
      return Form.EMPTY;
    }
  }

  /**
   * Gets the value of the schema's {@code definitions} keyword.
   *
   * @return the {@code definitions} keyword
   */
  public Map<String, Schema> getDefinitions() {
    return definitions;
  }

  /**
   * Sets the value of the schema's {@code definitions} keyword.
   *
   * @param definitions the {@code definitions} keyword
   */
  public void setDefinitions(Map<String, Schema> definitions) {
    this.definitions = definitions;
  }

  /**
   * Gets the value of the schema's {@code nullable} keyword.
   *
   * @return the {@code nullable} keyword
   */
  public boolean isNullable() {
    return nullable;
  }

  /**
   * Sets the value of the schema's {@code nullable} keyword.
   *
   * @param nullable the {@code nullable} keyword
   */
  public void setNullable(boolean nullable) {
    this.nullable = nullable;
  }

  /**
   * Gets the value of the schema's {@code metadata} keyword.
   *
   * @return the {@code metadata} keyword
   */
  public Map<String, Object> getMetadata() {
    return metadata;
  }

  /**
   * Sets the value of the schema's {@code metadata} keyword.
   *
   * @param metadata the {@code metadata} keyword
   */
  public void setMetadata(Map<String, Object> metadata) {
    this.metadata = metadata;
  }

  /**
   * Gets the value of the schema's {@code ref} keyword.
   *
   * @return the {@code ref} keyword
   */
  public String getRef() {
    return ref;
  }

  /**
   * Sets the value of the schema's {@code ref} keyword.
   *
   * @param ref the {@code ref} keyword
   */
  public void setRef(String ref) {
    this.ref = ref;
  }

  /**
   * Gets the value of the schema's {@code type} keyword.
   *
   * @return the {@code type} keyword
   */
  public Type getType() {
    return type;
  }

  /**
   * Sets the value of the schema's {@code type} keyword.
   *
   * @param type the {@code type} keyword
   */
  public void setType(Type type) {
    this.type = type;
  }

  /**
   * Gets the value of the schema's {@code enum} keyword.
   *
   * @return the {@code enum} keyword
   */
  public Set<String> getEnum() {
    return enm;
  }

  /**
   * Sets the value of the schema's {@code enum} keyword.
   *
   * @param enm the {@code enum} keyword
   */
  public void setEnum(Set<String> enm) {
    this.enm = enm;
  }

  /**
   * Gets the value of the schema's {@code elements} keyword.
   *
   * @return the {@code elements} keyword
   */
  public Schema getElements() {
    return elements;
  }

  /**
   * Sets the value of the schema's {@code elements} keyword.
   *
   * @param elements the {@code elements} keyword
   */
  public void setElements(Schema elements) {
    this.elements = elements;
  }

  /**
   * Gets the value of the schema's {@code properties} keyword.
   *
   * @return the {@code properties} keyword
   */
  public Map<String, Schema> getProperties() {
    return properties;
  }

  /**
   * Sets the value of the schema's {@code properties} keyword.
   *
   * @param properties the {@code properties} keyword
   */
  public void setProperties(Map<String, Schema> properties) {
    this.properties = properties;
  }

  /**
   * Gets the value of the schema's {@code optionalProperties} keyword.
   *
   * @return the {@code optionalProperties} keyword
   */
  public Map<String, Schema> getOptionalProperties() {
    return optionalProperties;
  }

  /**
   * Sets the value of the schema's {@code optionalProperties} keyword.
   *
   * @param optionalProperties the {@code optionalProperties} keyword
   */
  public void setOptionalProperties(Map<String, Schema> optionalProperties) {
    this.optionalProperties = optionalProperties;
  }

  /**
   * Gets the value of the schema's {@code additionalProperties} keyword.
   *
   * @return the {@code additionalProperties} keyword
   */
  public Boolean getAdditionalProperties() {
    return additionalProperties;
  }

  /**
   * Sets the value of the schema's {@code additionalProperties} keyword.
   *
   * @param additionalProperties the {@code additionalProperties} keyword
   */
  public void setAdditionalProperties(Boolean additionalProperties) {
    this.additionalProperties = additionalProperties;
  }

  /**
   * Gets the value of the schema's {@code values} keyword.
   *
   * @return the {@code values} keyword
   */
  public Schema getValues() {
    return values;
  }

  /**
   * Sets the value of the schema's {@code values} keyword.
   *
   * @param values the {@code values} keyword
   */
  public void setValues(Schema values) {
    this.values = values;
  }

  /**
   * Gets the value of the schema's {@code discriminator} keyword.
   *
   * @return the {@code discriminator} keyword
   */
  public String getDiscriminator() {
    return discriminator;
  }

  /**
   * Sets the value of the schema's {@code discriminator} keyword.
   *
   * @param discriminator the {@code discriminator} keyword
   */
  public void setDiscriminator(String discriminator) {
    this.discriminator = discriminator;
  }

  /**
   * Gets the value of the schema's {@code mapping} keyword.
   *
   * @return the {@code mapping} keyword
   */
  public Map<String, Schema> getMapping() {
    return mapping;
  }

  /**
   * Sets the value of the schema's {@code mapping} keyword.
   *
   * @param mapping the {@code mapping} keyword
   */
  public void setMapping(Map<String, Schema> mapping) {
    this.mapping = mapping;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + (additionalProperties ? 1231 : 1237);
    result = prime * result + ((definitions == null) ? 0 : definitions.hashCode());
    result = prime * result + ((discriminator == null) ? 0 : discriminator.hashCode());
    result = prime * result + ((elements == null) ? 0 : elements.hashCode());
    result = prime * result + ((enm == null) ? 0 : enm.hashCode());
    result = prime * result + ((mapping == null) ? 0 : mapping.hashCode());
    result = prime * result + ((metadata == null) ? 0 : metadata.hashCode());
    result = prime * result + (nullable ? 1231 : 1237);
    result = prime * result + ((optionalProperties == null) ? 0 : optionalProperties.hashCode());
    result = prime * result + ((properties == null) ? 0 : properties.hashCode());
    result = prime * result + ((ref == null) ? 0 : ref.hashCode());
    result = prime * result + ((type == null) ? 0 : type.hashCode());
    result = prime * result + ((values == null) ? 0 : values.hashCode());
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
    Schema other = (Schema) obj;
    if (additionalProperties != other.additionalProperties)
      return false;
    if (definitions == null) {
      if (other.definitions != null)
        return false;
    } else if (!definitions.equals(other.definitions))
      return false;
    if (discriminator == null) {
      if (other.discriminator != null)
        return false;
    } else if (!discriminator.equals(other.discriminator))
      return false;
    if (elements == null) {
      if (other.elements != null)
        return false;
    } else if (!elements.equals(other.elements))
      return false;
    if (enm == null) {
      if (other.enm != null)
        return false;
    } else if (!enm.equals(other.enm))
      return false;
    if (mapping == null) {
      if (other.mapping != null)
        return false;
    } else if (!mapping.equals(other.mapping))
      return false;
    if (metadata == null) {
      if (other.metadata != null)
        return false;
    } else if (!metadata.equals(other.metadata))
      return false;
    if (nullable != other.nullable)
      return false;
    if (optionalProperties == null) {
      if (other.optionalProperties != null)
        return false;
    } else if (!optionalProperties.equals(other.optionalProperties))
      return false;
    if (properties == null) {
      if (other.properties != null)
        return false;
    } else if (!properties.equals(other.properties))
      return false;
    if (ref == null) {
      if (other.ref != null)
        return false;
    } else if (!ref.equals(other.ref))
      return false;
    if (type != other.type)
      return false;
    if (values == null) {
      if (other.values != null)
        return false;
    } else if (!values.equals(other.values))
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "Schema [additionalProperties=" + additionalProperties + ", definitions=" + definitions + ", discriminator="
        + discriminator + ", elements=" + elements + ", enm=" + enm + ", mapping=" + mapping + ", metadata=" + metadata
        + ", nullable=" + nullable + ", optionalProperties=" + optionalProperties + ", properties=" + properties
        + ", ref=" + ref + ", type=" + type + ", values=" + values + "]";
  }
}
