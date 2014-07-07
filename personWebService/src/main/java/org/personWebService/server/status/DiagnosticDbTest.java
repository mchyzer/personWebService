
/**
 * 
 */
package org.personWebService.server.status;

import org.apache.commons.lang.builder.HashCodeBuilder;
import org.personWebService.server.cache.PersonWsCache;
import org.personWebService.server.hibernate.PersonWsDaoFactory;



/**
 * see if the server can connect to the DB (cache results)
 * @author mchyzer
 *
 */
public class DiagnosticDbTest extends DiagnosticTask {

  
  /**
   * cache the results
   */
  private static PersonWsCache<String, Boolean> dbCache = new PersonWsCache<String, Boolean>(DiagnosticDbTest.class.getName() + ".dbDiagnostic", 100, false, 120, 120, false);
  

  /**
   * @see DiagnosticTask#doTask()
   */
  @Override
  protected boolean doTask() {
    
    if (dbCache.containsKey("personWs")) {

      this.appendSuccessTextLine("Retrieved object from cache");

    } else {
      
      //doesnt matter what this returns
      PersonWsDaoFactory.getFactory().getTwoFactorDaemonLog().retrieveByUuid("123");
  
    
      this.appendSuccessTextLine("Retrieved object from database");
      dbCache.put("personWs", Boolean.TRUE);
      
    }
    return true;
    
  }

  /**
   * @see DiagnosticTask#retrieveName()
   */
  @Override
  public String retrieveName() {
    
    return "dbTest_personWs";
  }

  /**
   * @see DiagnosticTask#retrieveNameFriendly()
   */
  @Override
  public String retrieveNameFriendly() {
    return "Database test";
  }

  /**
   * 
   */
  @Override
  public boolean equals(Object obj) {
    return obj instanceof DiagnosticDbTest;
  }
  
  /**
   * 
   */
  @Override
  public int hashCode() {
    return new HashCodeBuilder().toHashCode();
  }

}
