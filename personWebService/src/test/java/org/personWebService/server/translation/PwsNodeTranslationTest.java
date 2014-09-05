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
    TestRunner.run(new PwsNodeTranslationTest("testTranslateArrayAssignment"));
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
    // someField.another[3] = someField2.another2[3]
    // someField.another[3] = someField2.another2[3]
    // "someField:complicate.whatever"."someField:complicate.another"[3] = "someField2:complicate2.whatever2"."someField2:complicate.another2"[3]
    // "someField:comp&quot;lic&amp;ate.whatever"."someField:complicate.another"[3] = "someField2:complicate2.whatever2"."someField2:complicate.another2"[3]
    // someField.another[@lang='en'] = someField2.another2[@lang='fr']
    // someField.another[@lang.something='en'] = someField2.another2[@lang.whatever='fr']
    // someField.another[@lang.something='en'].yo = someField2.another2[@lang.whatever='fr'].hey
    // try EL on the object
    // someField = ${fromObject.field('someField')} ${fromObject.array[2].field('someField')}
    // someField = ${SomeClass.resolve("someField")} ${SomeClass.resolve("someField")}
    // someField = ${SomeClass.resolve("someField").valueBoolean ? 'hey' : 'there'}

  }


  
}
