package org.personWebService.server.hibernate;

import org.personWebService.server.util.PersonWsServerUtils;


/**
 * Various types of committing
 * @author mchyzer
 *
 */
public enum PwsCommitType {
  /** always commit right now */
  COMMIT_NOW,
  
  /** only commit if this is a new transaction */
  COMMIT_IF_NEW_TRANSACTION;
  
  /**
   * do a case-insensitive matching
   * 
   * @param string
   * @return the enum or null or exception if not found
   */
  public static PwsCommitType valueOfIgnoreCase(String string) {
    return PersonWsServerUtils.enumValueOfIgnoreCase(PwsCommitType.class,string, false );
  }

}
