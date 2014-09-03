/**
 * @author mchyzer
 * $Id$
 */
package org.personWebService.server.translation;

import org.personWebService.server.beans.PwsNode;
import org.personWebService.server.beans.PwsNode.PwsNodeType;
import org.personWebService.server.beans.PwsNodeTest;

import junit.framework.TestCase;
import junit.textui.TestRunner;


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
    TestRunner.run(new PwsNodeTranslationTest("testTranslate"));
  }
  
  /**
   * 
   */
  public void testTranslate() {
    
    PwsNode dataNode = new PwsNode();
    dataNode.setPwsNodeType(PwsNodeType.object);
    dataNode.assignField("someField", new PwsNode("someValue"));
    
    PwsNode newNode = new PwsNode();
    newNode.setPwsNodeType(PwsNodeType.object);

    PwsNodeTranslation.assign(newNode, dataNode, "someField = someField");
    
    assertEquals("someValue", newNode.retrieveField("someField").getString());
  }


  
}
