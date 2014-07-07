package org.personWebService.server.databasePerf;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.personWebService.server.util.PersonWsServerUtils;

/**
 * separate class for log4j to get database log records in their own file
 * @author mchyzer
 *
 */
public class PwsDatabasePerfLog {

  /** logger */
  public static final Log LOG = PersonWsServerUtils.getLog(PwsDatabasePerfLog.class);
 
  
  /**
   * log something to the log file
   * @param message
   */
  public static void dbPerfLog(String message) {
    LOG.debug(message);
  }
  
  /**
   * log something to the log file
   * @param messageMap
   */
  public static void dbPerfLog(Map<String, Object> messageMap) {
    if (LOG.isDebugEnabled()) {
      LOG.debug(PersonWsServerUtils.mapToString(messageMap));
    }
  }

}
