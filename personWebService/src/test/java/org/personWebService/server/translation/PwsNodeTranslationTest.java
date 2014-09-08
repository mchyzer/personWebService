/**
 * @author mchyzer
 * $Id$
 */
package org.personWebService.server.translation;

import junit.framework.TestCase;
import junit.textui.TestRunner;

import org.personWebService.server.beans.PwsNode;
import org.personWebService.server.beans.PwsNode.PwsNodeType;


/**
 *
 */
public class PwsNodeTranslationTest extends TestCase {

  /**
   * 
   */
  public PwsNodeTranslationTest() {
    super();
    
  }

  /**
   * @param name
   */
  public PwsNodeTranslationTest(String name) {
    super(name);

  }

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new PwsNodeTranslationTest("testTranslateArrayScalarAssignmentQuotedEqualsFields"));
    //TestRunner.run(PwsNodeTranslationTest.class);
  }

  /**
   * someField.another = someField.another
   */
  public void testTranslateDrillDownAssignment() {
    
    //{"someInteger":45,"someFloat":34.567,"someFloatInt":34,"someBoolTrue":true,"someBoolFalse":false,"someString":"some string","sub":{"subInteger":37,"subString":"sub string"},"arraySub":[{"subInteger":37,"subString":"sub string"},{"subInteger":37,"subString":"sub string"}],"arrayInteger":[28,17,9],"arrayString":["abc","123","true"]}
    PwsNode dataNode = PwsNode.fromJson("{\"someField\":{\"another\":56}}");

    PwsNode newNode = new PwsNode();
    newNode.setPwsNodeType(PwsNodeType.object);

    PwsNodeTranslation.assign(newNode, dataNode, "someField.another = someField.another");

    assertEquals(new Long(56), newNode.retrieveField("someField").retrieveField("another").getInteger());
    assertEquals(new Long(56), dataNode.retrieveField("someField").retrieveField("another").getInteger());
    
    assertEquals("{\"someField\":{\"another\":56}}", dataNode.toJson());
    assertEquals("{\"someField\":{\"another\":56}}", newNode.toJson());
    
  }
  
  /**
   * someField.another[2].something = someField2.another2[2].something2
   */
  public void testTranslateArrayAssignment() {

    //{"someInteger":45,"someFloat":34.567,"someFloatInt":34,"someBoolTrue":true,"someBoolFalse":false,"someString":"some string","sub":{"subInteger":37,"subString":"sub string"},"arraySub":[{"subInteger":37,"subString":"sub string"},{"subInteger":37,"subString":"sub string"}],"arrayInteger":[28,17,9],"arrayString":["abc","123","true"]}
    PwsNode dataNode = PwsNode.fromJson("{\"someField2\":{\"another2\":[{\"something2\":\"a\"},{\"something2\":\"b\"},{\"something2\":\"c\"},{\"something2\":\"d\"}]}}");

    PwsNode newNode = new PwsNode();
    newNode.setPwsNodeType(PwsNodeType.object);

    PwsNodeTranslation.assign(newNode, dataNode, "someField.another[2].something = someField2.another2[2].something2");

    assertEquals("c", dataNode.retrieveField("someField2").retrieveField("another2").retrieveArrayItem(2).retrieveField("something2").getString());
    assertEquals("c", newNode.retrieveField("someField").retrieveField("another").retrieveArrayItem(2).retrieveField("something").getString());

    assertEquals("{\"someField2\":{\"another2\":[{\"something2\":\"a\"},{\"something2\":\"b\"},{\"something2\":\"c\"},{\"something2\":\"d\"}]}}", dataNode.toJson());
    assertEquals("{\"someField\":{\"another\":[{},{},{\"something\":\"c\"}]}}", newNode.toJson());

  }

  /**
   * scalar
   * someField.another[1] = someField2.another2[2]
   */
  public void testTranslateArrayScalarAssignment() {

    //{"someInteger":45,"someFloat":34.567,"someFloatInt":34,"someBoolTrue":true,"someBoolFalse":false,"someString":"some string","sub":{"subInteger":37,"subString":"sub string"},"arraySub":[{"subInteger":37,"subString":"sub string"},{"subInteger":37,"subString":"sub string"}],"arrayInteger":[28,17,9],"arrayString":["abc","123","true"]}
    PwsNode dataNode = PwsNode.fromJson("{\"someField2\":{\"another2\":[\"a\", \"b\", \"c\", \"d\",\"e\"]}}");

    PwsNode newNode = new PwsNode();
    newNode.setPwsNodeType(PwsNodeType.object);

    PwsNodeTranslation.assign(newNode, dataNode, "someField.another[2] = someField2.another2[3]");

    assertEquals("d", dataNode.retrieveField("someField2").retrieveField("another2").retrieveArrayItem(3).getString());
    assertEquals("d", newNode.retrieveField("someField").retrieveField("another").retrieveArrayItem(2).getString());

    assertEquals("{\"someField2\":{\"another2\":[\"a\",\"b\",\"c\",\"d\",\"e\"]}}", dataNode.toJson());
    assertEquals("{\"someField\":{\"another\":[null,null,\"d\"]}}", newNode.toJson());

  }

  /**
   * scalar
   * someField.another[2] = someField2.another2  (scalar)
   */
  public void testTranslateArrayScalarFromScalarAssignment() {

    //{"someInteger":45,"someFloat":34.567,"someFloatInt":34,"someBoolTrue":true,"someBoolFalse":false,"someString":"some string","sub":{"subInteger":37,"subString":"sub string"},"arraySub":[{"subInteger":37,"subString":"sub string"},{"subInteger":37,"subString":"sub string"}],"arrayInteger":[28,17,9],"arrayString":["abc","123","true"]}
    PwsNode dataNode = PwsNode.fromJson("{\"someField2\":{\"another2\":3.45}}");

    PwsNode newNode = new PwsNode();
    newNode.setPwsNodeType(PwsNodeType.object);

    PwsNodeTranslation.assign(newNode, dataNode, "someField.another[2] = someField2.another2");

    assertEquals(new Double(3.45), dataNode.retrieveField("someField2").retrieveField("another2").getFloating());
    assertEquals(new Double(3.45), newNode.retrieveField("someField").retrieveField("another").retrieveArrayItem(2).getFloating());

    assertEquals("{\"someField2\":{\"another2\":3.45}}", dataNode.toJson());
    assertEquals("{\"someField\":{\"another\":[null,null,3.45]}}", newNode.toJson());

  }

  /**
   * someField.another = someField2.another2   (object)
   */
  public void testTranslateObject() {

    //{"someInteger":45,"someFloat":34.567,"someFloatInt":34,"someBoolTrue":true,"someBoolFalse":false,"someString":"some string","sub":{"subInteger":37,"subString":"sub string"},"arraySub":[{"subInteger":37,"subString":"sub string"},{"subInteger":37,"subString":"sub string"}],"arrayInteger":[28,17,9],"arrayString":["abc","123","true"]}
    PwsNode dataNode = PwsNode.fromJson("{\"someField2\":{\"another3\":3.45,\"another2\":{\"arraySub\":[{\"subInteger\":37,\"subString\":\"sub string\"},{\"subInteger\":37,\"subString\":\"sub string\"}]}}}");
    
    PwsNode newNode = new PwsNode();
    newNode.setPwsNodeType(PwsNodeType.object);

    PwsNodeTranslation.assign(newNode, dataNode, "someField.another = someField2.another2");

    
    assertEquals(new Long(37), dataNode.retrieveField("someField2").retrieveField("another2").retrieveField("arraySub").retrieveArrayItem(0).retrieveField("subInteger").getInteger());
    assertEquals(new Long(37), newNode.retrieveField("someField").retrieveField("another").retrieveField("arraySub").retrieveArrayItem(0).retrieveField("subInteger").getInteger());

    assertEquals(dataNode.toJson(), "{\"someField2\":{\"another3\":3.45,\"another2\":{\"arraySub\":[{\"subInteger\":37,\"subString\":\"sub string\"},{\"subInteger\":37,\"subString\":\"sub string\"}]}}}", dataNode.toJson());
    assertEquals(newNode.toJson(), "{\"someField\":{\"another\":{\"arraySub\":[{\"subInteger\":37,\"subString\":\"sub string\"},{\"subInteger\":37,\"subString\":\"sub string\"}]}}}", newNode.toJson());
  }


  /**
   * someField.another = someField2.another2   (array of scalars)
   */
  public void testTranslateArrayOfScalars() {

    //{"someInteger":45,"someFloat":34.567,"someFloatInt":34,"someBoolTrue":true,"someBoolFalse":false,"someString":"some string","sub":{"subInteger":37,"subString":"sub string"},"arraySub":[{"subInteger":37,"subString":"sub string"},{"subInteger":37,"subString":"sub string"}],"arrayInteger":[28,17,9],"arrayString":["abc","123","true"]}
    PwsNode dataNode = PwsNode.fromJson("{\"someField2\":{\"another3\":3.45,\"another2\":{\"arraySub\":[23,45]}}}");
    
    PwsNode newNode = new PwsNode();
    newNode.setPwsNodeType(PwsNodeType.object);

    PwsNodeTranslation.assign(newNode, dataNode, "someField.another = someField2.another2.arraySub");

    
    assertEquals(new Long(23), dataNode.retrieveField("someField2").retrieveField("another2").retrieveField("arraySub").retrieveArrayItem(0).getInteger());
    assertEquals(new Long(23), newNode.retrieveField("someField").retrieveField("another").retrieveArrayItem(0).getInteger());

    assertEquals(dataNode.toJson(), "{\"someField2\":{\"another3\":3.45,\"another2\":{\"arraySub\":[23,45]}}}", dataNode.toJson());
    assertEquals(newNode.toJson(), "{\"someField\":{\"another\":[23,45]}}", newNode.toJson());
  }

  /**
   * someField.another = someField2.another2   (array of objects)
   */
  public void testTranslateObjectArray() {

    //{"someInteger":45,"someFloat":34.567,"someFloatInt":34,"someBoolTrue":true,"someBoolFalse":false,"someString":"some string","sub":{"subInteger":37,"subString":"sub string"},"arraySub":[{"subInteger":37,"subString":"sub string"},{"subInteger":37,"subString":"sub string"}],"arrayInteger":[28,17,9],"arrayString":["abc","123","true"]}
    PwsNode dataNode = PwsNode.fromJson("{\"someField2\":{\"another3\":3.45,\"another2\":{\"arraySub\":[{\"subInteger\":37,\"subString\":\"sub string\"},{\"subInteger\":37,\"subString\":\"sub string\"}]}}}");
    
    PwsNode newNode = new PwsNode();
    newNode.setPwsNodeType(PwsNodeType.object);

    PwsNodeTranslation.assign(newNode, dataNode, "someField.another = someField2.another2.arraySub");

    
    assertEquals(new Long(37), dataNode.retrieveField("someField2").retrieveField("another2").retrieveField("arraySub").retrieveArrayItem(0).retrieveField("subInteger").getInteger());
    assertEquals(new Long(37), newNode.retrieveField("someField").retrieveField("another").retrieveArrayItem(0).retrieveField("subInteger").getInteger());

    assertEquals(dataNode.toJson(), "{\"someField2\":{\"another3\":3.45,\"another2\":{\"arraySub\":[{\"subInteger\":37,\"subString\":\"sub string\"},{\"subInteger\":37,\"subString\":\"sub string\"}]}}}", dataNode.toJson());
    assertEquals(newNode.toJson(), "{\"someField\":{\"another\":[{\"subInteger\":37,\"subString\":\"sub string\"},{\"subInteger\":37,\"subString\":\"sub string\"}]}}", newNode.toJson());
  }

  /**
   * 
   * "someField:complicate.whatever"."someField:complicate.another"[2] = "someField2:complicate2.whatever2"."someField2:complicate.another2"[3]
   */
  public void testTranslateArrayScalarAssignmentQuotedFields() {

    //{"someInteger":45,"someFloat":34.567,"someFloatInt":34,"someBoolTrue":true,"someBoolFalse":false,"someString":"some string","sub":{"subInteger":37,"subString":"sub string"},"arraySub":[{"subInteger":37,"subString":"sub string"},{"subInteger":37,"subString":"sub string"}],"arrayInteger":[28,17,9],"arrayString":["abc","123","true"]}
    PwsNode dataNode = PwsNode.fromJson("{\"someField2:complicate2.whatever2\":{\"someField2:complicate.another2\":[\"a\",\"b\",\"c\",\"d\",\"e\"]}}");

    PwsNode newNode = new PwsNode();
    newNode.setPwsNodeType(PwsNodeType.object);

    PwsNodeTranslation.assign(newNode, dataNode, "\"someField:complicate.whatever\".\"someField:complicate.another\"[2] "
        + "= \"someField2:complicate2.whatever2\".\"someField2:complicate.another2\"[3]");

    System.out.println(newNode.toJson());
    
    assertEquals("d", dataNode.retrieveField("someField2:complicate2.whatever2").retrieveField("someField2:complicate.another2").retrieveArrayItem(3).getString());
    assertEquals("d", newNode.retrieveField("someField:complicate.whatever").retrieveField("someField:complicate.another").retrieveArrayItem(2).getString());

    assertEquals("{\"someField2:complicate2.whatever2\":{\"someField2:complicate.another2\":[\"a\",\"b\",\"c\",\"d\",\"e\"]}}", dataNode.toJson());
    assertEquals("{\"someField:complicate.whatever\":{\"someField:complicate.another\":[null,null,\"d\"]}}", newNode.toJson());

  }

  /**
   * "some\"Field:compl[icate.whate=ver"."some\"Field:complic[ate.an=other"[2] = "some\"Field2:complic[ate2.wha=tever2"."some\"Field2:co[mplic=ate.another2"[3]
   */
  public void testTranslateArrayScalarAssignmentQuotedEqualsFields() {

    //{"someInteger":45,"someFloat":34.567,"someFloatInt":34,"someBoolTrue":true,"someBoolFalse":false,"someString":"some string","sub":{"subInteger":37,"subString":"sub string"},"arraySub":[{"subInteger":37,"subString":"sub string"},{"subInteger":37,"subString":"sub string"}],"arrayInteger":[28,17,9],"arrayString":["abc","123","true"]}
    PwsNode dataNode = PwsNode.fromJson("{\"some\\\"Field2:complic[ate2.wha=tever2\":{\"some\\\"Field2:co[mplic=ate.another2\":[\"a\",\"b\",\"c\",\"d\",\"e\"]}}");

    PwsNode newNode = new PwsNode();
    newNode.setPwsNodeType(PwsNodeType.object);

    PwsNodeTranslation.assign(newNode, dataNode, "\"some\\\"Field:compl[icate.whate=ver\".\"some\\\"Field:complic[ate.an=other\"[2] "
        + "= \"some\\\"Field2:complic[ate2.wha=tever2\".\"some\\\"Field2:co[mplic=ate.another2\"[3]");

    System.out.println(newNode.toJson());
    
    assertEquals("d", dataNode.retrieveField("some\\\"Field2:complic[ate2.wha=tever2").retrieveField("some\\\"Field:complic[ate.an=other").retrieveArrayItem(3).getString());
    assertEquals("d", newNode.retrieveField("some\\\"Field:compl[icate.whate=ver").retrieveField("some\\\"Field:complic[ate.an=other").retrieveArrayItem(2).getString());

    assertEquals("{\"some\\\"Field2:complic[ate2.wha=tever2\":{\"some\\\"Field2:co[mplic=ate.another2\":[\"a\",\"b\",\"c\",\"d\",\"e\"]}}", dataNode.toJson());
    assertEquals("{\"some\\\"Field:compl[icate.whate=ver\":{\"some\\\"Field:complic[ate.an=other\":[null,null,\"d\"]}}", newNode.toJson());

  }

  
  /**
   * someField = someField
   */
  public void testTranslateSimpleAssignment() {

    PwsNode dataNode = new PwsNode();
    dataNode.setPwsNodeType(PwsNodeType.object);
    dataNode.assignField("someField", new PwsNode("someValue"));

    PwsNode newNode = new PwsNode();
    newNode.setPwsNodeType(PwsNodeType.object);

    PwsNodeTranslation.assign(newNode, dataNode, "someField = someField");

    assertEquals("someValue", newNode.retrieveField("someField").getString());
    assertEquals("someValue", dataNode.retrieveField("someField").getString());

    assertEquals("{\"someField\":\"someValue\"}", newNode.toJson());
    assertEquals("{\"someField\":\"someValue\"}", dataNode.toJson());

    //Tests:
    // "someField:complicate.whatever"."someField:complicate.another"[3] = "someField2:complicate2.whatever2"."someField2:complicate.another2"[3]
    // "someField:comp&quot;lic&amp;ate.whatever"."someField:complicate.another"[3] = "someField2:complicate2.whatever2"."someField2:complicate.another2"[3]
    // someField.another[@lang='en'] = someField2.another2[@lang='fr']
    // someField.another[@lang.something='en'] = someField2.another2[@lang.whatever='fr']
    // someField.another[@lang.something='en'].yo = someField2.another2[@lang.whatever='fr'].hey
    // someField.another[@lang.something='en'].there = someField2.another2[@lang.whatever='fr'].where
    // someField.another[@lang.something='en'].field = ${null}
    // someField.another[@lang.something='en'].field2 = ${"label"}
    // try EL on the object
    // someField = ${fromObject.field('someField')} ${fromObject.array[2].field('someField')}
    // someField = ${SomeClass.resolve("someField")} ${SomeClass.resolve("someField")}
    // someField = ${SomeClass.resolve("someField").valueBoolean ? 'hey' : 'there'}

  }


  
}
