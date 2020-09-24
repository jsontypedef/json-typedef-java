# jtd: JSON Validation for Java

[![Maven Central](https://img.shields.io/maven-central/v/com.jsontypedef.jtd/jtd)](https://javadoc.io/doc/com.jsontypedef.jtd/jtd)

> This package implements JSON Typedef *validation* for Java. If you're trying
> to do JSON Typedef *code generation*, see ["Generating Java from JSON Typedef
> Schemas"][jtd-java-codegen] in the JSON Typedef docs.

`jtd` is a Java implementation of [JSON Type Definition][jtd], a schema language
for JSON. `jtd` primarily gives you two things:

1. Validating input data against JSON Typedef schemas.
2. A Java representation of JSON Typedef schemas.

With this package, you can add JSON Typedef-powered validation to your
application, or you can build your own tooling on top of JSON Type Definition.

## Installation

You can install this package with `mvn`:

```xml
<dependency>
  <groupId>com.jsontypedef.jtd</groupId>
  <artifactId>jtd</artifactId>
  <version>0.2.2</version>
</dependency>
```

Or with `gradle`:

```groovy
dependencies {
  implementation 'com.jsontypedef.jtd:jtd:0.2.2'
}
```

## Documentation

Detailed API documentation is available online at:

https://javadoc.io/doc/com.jsontypedef.jtd/jtd

For more high-level documentation about JSON Typedef in general, or JSON Typedef
in combination with Java in particular, see:

* [The JSON Typedef Website][jtd]
* ["Validating JSON in Java with JSON Typedef"][jtd-js-validation]
* ["Generating Java from JSON Typedef Schemas"][jtd-ts-codegen]

## Basic Usage

> For a more detailed tutorial and guidance on how to integrate `jtd` in your
> application, see ["Validating JSON in Java with JSON
> Typedef"][jtd-java-validation] in the JSON Typedef docs.

Here's an example of how you can use this package to validate JSON data against
a JSON Typedef schema:

```java
String schemaJson = String.join("\n",
  "{",
  "  \"properties\": {",
  "    \"name\": { \"type\": \"string\" },",
  "    \"age\": { \"type\": \"uint32\" },",
  "    \"phones\": {",
  "      \"elements\": { \"type\": \"string\" }",
  "    }",
  "  }",
  "}");

// First, we'll show how to do validation with Gson. See further below for
// the corresponding Jackson example.
Gson gson = new Gson();
Schema schema = gson.fromJson(schemaJson, Schema.class);

// Validators can find validation errors in an input against a schema.
//
// They are backend-neutral; you can use a Validator with both Gson and Jackson.
// To make that work, you'll see in these examples that we construct GsonAdapter
// and JacksonAdapter instances, which abstract away Gson and Jackson into a
// shared interface (which you can implement yourself as well).
Validator validator = new Validator();

// Validator.validate() returns an array of validation errors. If there were
// no problems with the input, it returns an empty array.
//
// This input is perfect, so we'll get back an empty list of validation
// errors.
String okJson = "{ \"name\": \"John Doe\", \"age\": 43, \"phones\": [\"+44 1234567\", \"+44 2345678\"] }";
JsonElement okInput = gson.fromJson(okJson, JsonElement.class);

// Outputs: []
System.out.println(validator.validate(schema, new GsonAdapter(okInput)));

// This next input has three problems with it:
//
// 1. It's missing "name", which is a required property.
// 2. "age" is a string, but it should be an integer.
// 3. "phones[1]" is a number, but it should be a string.
//
// Each of those errors corresponds to one of the errors returned by
// Validator.validate().
String badJson = "{ \"age\": \"43\", \"phones\": [\"+44 1234567\", 442345678] }";
JsonElement badInput = gson.fromJson(badJson, JsonElement.class);

// Outputs:
//
// [
//   ValidationError [instancePath=[], schemaPath=[properties, name]],
//   ValidationError [instancePath=[age], schemaPath=[properties, age, type]],
//   ValidationError [instancePath=[phones, 1], schemaPath=[properties, phones, elements, type]]
// ]
System.out.println(validator.validate(schema, new GsonAdapter(badInput)));

// Here's the same code as above, but with Jackson instead of Gson:
ObjectMapper objectMapper = new ObjectMapper();
schema = objectMapper.readValue(schemaJson, Schema.class);

// These two lines output the exact same set of data as in the previous
// examples with Gson.
System.out.println(validator.validate(schema,
  new JacksonAdapter(objectMapper.readTree(okJson))));

System.out.println(validator.validate(schema,
  new JacksonAdapter(objectMapper.readTree(badJson))));
```

## Advanced Usage: Limiting Errors Returned

By default, `Validator.validate()` returns every error it finds. If you just
care about whether there are any errors at all, or if you can't show more than
some number of errors, then you can get better performance out of
`Validator.validate()` using the `maxErrors` option.

For example, taking the same example from before, but limiting it to 1 error, we
get:

```java
// Outputs:
//
// [ValidationError [instancePath=[], schemaPath=[properties, name]]]
validator.setMaxErrors(1);
System.out.println(validator.validate(schema, new GsonAdapter(badInput)));
```

## Advanced Usage: Handling Untrusted Schemas

If you want to run `jtd` against a schema that you don't trust, then you should:

1. Ensure the schema is well-formed, using `Schema.verify()`, which validates
   things like making sure all `ref`s have corresponding definitions.

2. Call `Validator.validate()` with `maxDepth` being set (either using the
   constructor, or with `setMaxDepth()`). JSON Typedef lets you write recursive
   schemas -- if you're evaluating against untrusted schemas, you might go into
   an infinite loop when evaluating against a malicious input, such as this one:

   ```json
   {
     "ref": "loop",
     "definitions": {
       "loop": {
         "ref": "loop"
       }
     }
   }
   ```

   The `maxDepth` option tells `Validator.validate()` how many `ref`s to follow
   recursively before giving up and throwing `MaxDepthExceededException`.

Here's an example of how you can use `jtd` to evaluate data against an untrusted
schema:

```java
public class ValidateUntrusted {
  private boolean validateUntrusted(Schema schema, Json data) throws InvalidSchemaException, MaxDepthExceededException {
    schema.verify();

    Validator validator = new Validator();
    validator.setMaxDepth(32);

    return validator.validate(schema, data).isEmpty();
  }

  private void example() throws InvalidSchemaException, MaxDepthExceededException {
    Gson gson = new Gson();

    // Returns: true
    validateUntrusted(
      gson.fromJson("{ \"type\": \"string\" }", Schema.class),
      new GsonAdapter(gson.fromJson("\"foo\"", JsonElement.class)));

    // Returns: false
    validateUntrusted(
      gson.fromJson("{ \"type\": \"string\" }", Schema.class),
      new GsonAdapter(gson.fromJson("null", JsonElement.class)));

    // Raises:
    //
    // com.jsontypedef.jtd.InvalidSchemaException: ref to non-existent definition
    validateUntrusted(
      gson.fromJson("{ \"type\": \"string\" }", Schema.class),
      new GsonAdapter(gson.fromJson("\"foo\"", JsonElement.class)));

    // Raises:
    //
    // com.jsontypedef.jtd.MaxDepthExceededException
    validateUntrusted(
      gson.fromJson("{ \"definitions\": {\"loop\": {\"ref\": \"loop\"}}, \"ref\": \"loop\" }", Schema.class),
      new GsonAdapter(gson.fromJson("null", JsonElement.class)));
  }
}
```

[jtd]: https://jsontypedef.com
[jtd-java-codegen]: https://jsontypedef.com/docs/java/code-generation
[jtd-java-validation]: https://jsontypedef.com/docs/java/validation
