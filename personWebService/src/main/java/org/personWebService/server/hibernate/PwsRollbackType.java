package org.personWebService.server.hibernate;

import org.personWebService.server.util.PersonWsServerUtils;

/**
 * Various types of rolling back
 * @author mchyzer
 *
 */
public enum PwsRollbackType {
  /** always rollback right now */
  ROLLBACK_NOW,
  
  /** only rollback if this is a new transaction */
  ROLLBACK_IF_NEW_TRANSACTION;
  
  /**
   * do a case-insensitive matching
   * 
   * @param string
   * @return the enum or null or exception if not found
   */
  public static PwsRollbackType valueOfIgnoreCase(String string) {
    return PersonWsServerUtils.enumValueOfIgnoreCase(PwsRollbackType.class,string, false );
  }

}
