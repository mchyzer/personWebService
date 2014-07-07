/**
 * @author mchyzer
 * $Id: AllTwoFactorTests.java,v 1.1 2013/06/20 06:02:51 mchyzer Exp $
 */
package org.personWebService.server;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;


/**
 *
 */
public class AllPersonWebServiceTests {

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(AllPersonWebServiceTests.suite());
  }
  
  /**
   * 
   * @return test
   */
  public static Test suite() {
    TestSuite suite = new TestSuite("Test for org.personWebService.server");
    //$JUnit-BEGIN$


    //$JUnit-END$
    return suite;
  }

}
