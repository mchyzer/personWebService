/**
 * @author mchyzer
 * $Id: PersonWsDaemonName.java,v 1.1 2013/06/20 06:02:51 mchyzer Exp $
 */
package org.personWebService.server.daemon;


/**
 * name of daemon
 */
public enum PersonWsDaemonName {

  /** permanently delete old records */
  permanentlyDeleteOldRecords,
  
  /** delete old daemon logs */
  deleteOldDaemonLogs;
  
}
