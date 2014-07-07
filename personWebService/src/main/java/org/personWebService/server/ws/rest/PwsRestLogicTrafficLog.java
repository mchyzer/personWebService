/**
 * @author mchyzer
 * $Id: TfRestLogicTrafficLog.java,v 1.1 2013/06/20 06:02:50 mchyzer Exp $
 */
package org.personWebService.server.ws.rest;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.personWebService.server.util.PersonWsServerUtils;



/**
 * logger to log the traffic of 
 */
public class PwsRestLogicTrafficLog {

  /** logger */
  private static final Log LOG = PersonWsServerUtils.getLog(PwsRestLogicTrafficLog.class);
 
  
  /**
   * log something to the log file
   * @param message
   */
  public static void wsRestTrafficLog(String message) {
    LOG.debug(message);
  }
  
  /**
   * log something to the log file
   * @param messageMap
   */
  public static void wsRestTrafficLog(Map<String, Object> messageMap) {
    if (LOG.isDebugEnabled()) {
      LOG.debug(PersonWsServerUtils.mapToString(messageMap));
    }
  }

  
}
