/**
 * @author mchyzer
 * $Id: PwsDaemonLog.java,v 1.1 2013/06/20 06:02:50 mchyzer Exp $
 */
package org.personWebService.server.daemon;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.personWebService.server.util.PersonWsServerUtils;



/**
 * logger to log the daemon messages into one file
 */
public class PwsDaemonLog {

  /** logger */
  private static final Log LOG = PersonWsServerUtils.getLog(PwsDaemonLog.class);
 
  
  /**
   * log something to the log file
   * @param message
   */
  public static void daemonLog(String message) {
    LOG.debug(message);
  }
  
  /**
   * log something to the log file
   * @param messageMap
   */
  public static void daemonLog(Map<String, Object> messageMap) {
    if (LOG.isDebugEnabled()) {
      LOG.debug(PersonWsServerUtils.mapToString(messageMap));
    }
  }

  
}
