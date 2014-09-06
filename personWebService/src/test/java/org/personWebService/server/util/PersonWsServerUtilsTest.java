/**
 * @author mchyzer
 * $Id$
 */
package org.personWebService.server.util;

import junit.framework.TestCase;
import junit.textui.TestRunner;


/**
 *
 */
public class PersonWsServerUtilsTest extends TestCase {

  /**
   * 
   */
  public PersonWsServerUtilsTest() {
    super();
    
  }

  /**
   * @param name
   */
  public PersonWsServerUtilsTest(String name) {
    super(name);
    
  }

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new PersonWsServerUtilsTest("testSplitTrimQuoted"));
  }

  /**
   * 
   */
  public void testIndexOfsQuoted() {

    //* if the input is ab..cd..ef
    //* and the substring is ..
    //* then return 2,6
    PersonWsServerUtils.assertEqualsList(PersonWsServerUtils.toList(2,6), 
        PersonWsServerUtils.indexOfsQuoted("ab..cd..ef", ".."));
    

    //* if the input is ab..c"e..\" '.."d..ef
    //* and the substring is ..
    //* then return 2,17
    PersonWsServerUtils.assertEqualsList(PersonWsServerUtils.toList(2, 17), 
        PersonWsServerUtils.indexOfsQuoted("ab..c\"e..\\\" '..\"d..ef", ".."));

    //* if the input is ab..c'e..\' "..'d..ef
    //* and the substring is ..
    //* then return 2,17
    PersonWsServerUtils.assertEqualsList(PersonWsServerUtils.toList(2, 17), 
        PersonWsServerUtils.indexOfsQuoted("ab..c'e..\\' \"..'d..ef", ".."));

    PersonWsServerUtils.assertEqualsList(PersonWsServerUtils.toList(2, 17), 
        PersonWsServerUtils.indexOfsQuoted("ab..c'e..\\' \"..'d..ef", ".."));

    PersonWsServerUtils.assertEqualsList(PersonWsServerUtils.toList(31),
        PersonWsServerUtils.indexOfsQuoted("\"someField:complicate.whatever\".\"someField:complicate.another\"[2]", "."));
                                         
    
  }
  
  /**
   * 
   */
  public void testSplitTrimQuoted() {
    //    String[] expressionParts = PersonWsServerUtils.splitTrim(expression, ".");
    //
    //    "someField:complicate.whatever"."someField:complicate.another"[2]
    
    String[] result = PersonWsServerUtils.splitTrimQuoted("a .b. c.d", ".");
    PersonWsServerUtils.assertEqualsArray(new String[]{"a", "b", "c", "d"}, result);
    
    result = PersonWsServerUtils.splitTrimQuoted("\"someField:complicate.whatever\".\"someField:complicate.another\"[2]", ".");
    PersonWsServerUtils.assertEqualsArray(new String[]{"\"someField:complicate.whatever\"", "\"someField:complicate.another\"[2]"}, result);
    
  }
  
  
}
