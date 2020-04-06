package com.jsontypedef.jtd;

import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

public class Validator {
  private int maxDepth;
  private int maxErrors;

  public List<ValidationError> validate(Schema schema, JsonElement instance) throws MaxDepthExceededException {
    ValidationState state = new ValidationState();
    state.errors = new ArrayList<>();
    state.instanceTokens = new ArrayList<>();
    state.schemaTokens = new ArrayList<>();
    state.schemaTokens.add(new ArrayList<>());
    state.root = schema;

    try {
      validate(state, schema, instance, null);
    } catch (MaxErrorsReachedException e) {
      // Nothing to be done here. This is not an actual error condition, just a
      // circuit-breaker.
    }

    return state.errors;
  }

  private void validate(ValidationState state, Schema schema, JsonElement instance, String parentTag)
      throws MaxDepthExceededException, MaxErrorsReachedException {
    if (schema.isNullable() && instance.isJsonNull()) {
      return;
    }

    switch (schema.getForm()) {
      case EMPTY:
        break;
      case REF:
        state.schemaTokens.add(new ArrayList<>());
        state.pushSchemaToken("definitions");
        state.pushSchemaToken(schema.getRef());

        validate(state, state.root.getDefinitions().get(schema.getRef()), instance, null);

        state.schemaTokens.remove(state.schemaTokens.size() - 1);
        break;
      case TYPE:
        state.pushSchemaToken("type");
        switch (schema.getType()) {
          case BOOLEAN:
            if (!instance.isJsonPrimitive() || !((JsonPrimitive) instance).isBoolean()) {
              state.pushError();
            }
            break;
          case FLOAT32:
          case FLOAT64:
            if (!instance.isJsonPrimitive() || !((JsonPrimitive) instance).isNumber()) {
              state.pushError();
            }
            break;
          case INT8:
            checkInt(state, instance, -128, 127);
            break;
          case UINT8:
            checkInt(state, instance, 0, 255);
            break;
          case INT16:
            checkInt(state, instance, -32768, 32767);
            break;
          case UINT16:
            checkInt(state, instance, 0, 65535);
            break;
          case INT32:
            checkInt(state, instance, -2147483648, 2147483647);
            break;
          case UINT32:
            checkInt(state, instance, 0, 4294967295L);
            break;
          case STRING:
            if (!instance.isJsonPrimitive() || !((JsonPrimitive) instance).isString()) {
              state.pushError();
            }
            break;
          case TIMESTAMP:
            if (!instance.isJsonPrimitive() || !((JsonPrimitive) instance).isString()) {
              state.pushError();
            } else {
              // The instance is a JSON string. Let's verify it's a
              // well-formatted RFC3339 timestamp.
              try {
                DateTimeFormatter.ISO_ZONED_DATE_TIME.parse(instance.getAsString());
              } catch (DateTimeParseException e) {
                state.pushError();
              }
            }
            break;
        }
        state.popSchemaToken();
        break;
      case ENUM:
        state.pushSchemaToken("enum");

        if (!instance.isJsonPrimitive() || !((JsonPrimitive) instance).isString()) {
          state.pushError();
        } else {
          if (!schema.getEnum().contains(instance.getAsString())) {
            state.pushError();
          }
        }

        state.popSchemaToken();
        break;
      case ELEMENTS:
        state.pushSchemaToken("elements");

        if (!instance.isJsonArray()) {
          state.pushError();
        } else {
          int index = 0;
          for (JsonElement subInstance : instance.getAsJsonArray()) {
            state.pushInstanceToken(Integer.toString(index));
            validate(state, schema.getElements(), subInstance, null);
            state.popInstanceToken();

            index += 1;
          }
        }

        state.popSchemaToken();
        break;
      case PROPERTIES:
        if (instance.isJsonObject()) {
          if (schema.getProperties() != null) {
            state.pushSchemaToken("properties");
            for (Map.Entry<String, Schema> entry : schema.getProperties().entrySet()) {
              state.pushSchemaToken(entry.getKey());
              if (instance.getAsJsonObject().has(entry.getKey())) {
                state.pushInstanceToken(entry.getKey());
                validate(state, entry.getValue(), instance.getAsJsonObject().get(entry.getKey()), null);
                state.popInstanceToken();
              } else {
                state.pushError();
              }
              state.popSchemaToken();
            }
            state.popSchemaToken();
          }

          if (schema.getOptionalProperties() != null) {
            state.pushSchemaToken("optionalProperties");
            for (Map.Entry<String, Schema> entry : schema.getOptionalProperties().entrySet()) {
              state.pushSchemaToken(entry.getKey());
              if (instance.getAsJsonObject().has(entry.getKey())) {
                state.pushInstanceToken(entry.getKey());
                validate(state, entry.getValue(), instance.getAsJsonObject().get(entry.getKey()), null);
                state.popInstanceToken();
              }
              state.popSchemaToken();
            }
            state.popSchemaToken();
          }

          if (schema.getAdditionalProperties() == null || !schema.getAdditionalProperties()) {
            for (String key : instance.getAsJsonObject().keySet()) {
              boolean inProperties = schema.getProperties() != null && schema.getProperties().containsKey(key);
              boolean inOptionalProperties = schema.getOptionalProperties() != null
                  && schema.getOptionalProperties().containsKey(key);
              boolean discriminatorTagException = key.equals(parentTag);

              if (!inProperties && !inOptionalProperties && !discriminatorTagException) {
                state.pushInstanceToken(key);
                state.pushError();
                state.popInstanceToken();
              }
            }
          }
        } else {
          if (schema.getProperties() == null) {
            state.pushSchemaToken("optionalProperties");
          } else {
            state.pushSchemaToken("properties");
          }

          state.pushError();
          state.popSchemaToken();
        }

        break;
      case VALUES:
        state.pushSchemaToken("values");
        if (instance.isJsonObject()) {
          for (Map.Entry<String, JsonElement> entry : instance.getAsJsonObject().entrySet()) {
            state.pushInstanceToken(entry.getKey());
            validate(state, schema.getValues(), entry.getValue(), null);
            state.popInstanceToken();
          }
        } else {
          state.pushError();
        }
        state.popSchemaToken();
        break;
      case DISCRIMINATOR:
        if (instance.isJsonObject()) {
          JsonObject instanceObj = instance.getAsJsonObject();

          if (instanceObj.has(schema.getDiscriminator())) {
            JsonElement instanceTag = instanceObj.get(schema.getDiscriminator());
            if (instanceTag.isJsonPrimitive() && ((JsonPrimitive) instanceTag).isString()) {
              String instanceTagString = instanceTag.getAsString();
              if (schema.getMapping().containsKey(instanceTagString)) {
                Schema subSchema = schema.getMapping().get(instanceTagString);

                state.pushSchemaToken("mapping");
                state.pushSchemaToken(instanceTagString);
                validate(state, subSchema, instance, schema.getDiscriminator());
                state.popSchemaToken();
                state.popSchemaToken();
              } else {
                state.pushSchemaToken("mapping");
                state.pushInstanceToken(schema.getDiscriminator());
                state.pushError();
                state.popInstanceToken();
                state.popSchemaToken();
              }
            } else {
              state.pushSchemaToken("discriminator");
              state.pushInstanceToken(schema.getDiscriminator());
              state.pushError();
              state.popInstanceToken();
              state.popSchemaToken();
            }
          } else {
            state.pushSchemaToken("discriminator");
            state.pushError();
            state.popSchemaToken();
          }
        } else {
          state.pushSchemaToken("discriminator");
          state.pushError();
          state.popSchemaToken();
        }
        break;
    }
  }

  private void checkInt(ValidationState state, JsonElement instance, long min, long max)
      throws MaxErrorsReachedException {
    if (!instance.isJsonPrimitive() || !((JsonPrimitive) instance).isNumber()) {
      state.pushError();
    } else {
      double val = instance.getAsDouble();
      if (val < min || val > max || val != Math.round(val)) {
        state.pushError();
      }
    }
  }

  private static class ValidationState {
    public List<ValidationError> errors;
    public List<String> instanceTokens;
    public List<List<String>> schemaTokens;
    public Schema root;

    public void pushSchemaToken(String token) {
      schemaTokens.get(schemaTokens.size() - 1).add(token);
    }

    public void popSchemaToken() {
      List<String> last = schemaTokens.get(schemaTokens.size() - 1);
      last.remove(last.size() - 1);
    }

    public void pushInstanceToken(String token) {
      instanceTokens.add(token);
    }

    public void popInstanceToken() {
      instanceTokens.remove(instanceTokens.size() - 1);
    }

    public void pushError() throws MaxErrorsReachedException {
      this.errors.add(new ValidationError(new ArrayList<>(instanceTokens),
          new ArrayList<>(schemaTokens.get(schemaTokens.size() - 1))));
    }
  }

  private static class MaxErrorsReachedException extends Exception {
    private static final long serialVersionUID = -1396966477948063085L;
  }
}
