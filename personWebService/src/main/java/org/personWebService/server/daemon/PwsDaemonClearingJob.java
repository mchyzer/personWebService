/**
 * @author mchyzer
 * $Id: PwsDaemonClearingJob.java,v 1.1 2013/06/20 06:02:50 mchyzer Exp $
 */
package org.personWebService.server.daemon;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.personWebService.server.beans.PersonWsDaemonLog;
import org.personWebService.server.beans.PersonWsDaemonLogStatus;
import org.personWebService.server.hibernate.PersonWsDaoFactory;
import org.personWebService.server.util.PersonWsServerUtils;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;



/**
 * daemon which clears out daemon audit records
 */
public class PwsDaemonClearingJob implements Job {

  /** logger */
  private static final Log LOG = PersonWsServerUtils.getLog(PwsDaemonClearingJob.class);

  /**
   * @see org.quartz.Job#execute(org.quartz.JobExecutionContext)
   */
  @Override
  public void execute(JobExecutionContext context) throws JobExecutionException {
    daemonClearingLogic(PersonWsDaoFactory.getFactory());
  }

  /**
   * loops through the audit clearing logic
   */
  static long testingLoopsThrough = 0;
  
  /**
   * total count of last run
   */
  static long testingTotalCount = 0;
  
  /**
   * clear out audit logs
   * @param twoFactorDaoFactory 
   */
  public void daemonClearingLogic(final PersonWsDaoFactory twoFactorDaoFactory) {
    
    boolean hasError = false;

    long start = System.nanoTime();
    Date startDate = new Date();
    int elapsedTime = -1;
    
    Map<String, Object> debugLog = new LinkedHashMap<String, Object>();

    int count = 0;
    
    int errorCount = 0;
    Throwable throwable = null;
      
    
    try {
      //lets store to DB
      PersonWsDaemonLog twoFactorDaemonLog = new PersonWsDaemonLog();
      twoFactorDaemonLog.setUuid(PersonWsServerUtils.uuid());
      twoFactorDaemonLog.setDaemonName(PersonWsDaemonName.deleteOldDaemonLogs.name());
      twoFactorDaemonLog.setDetails(PersonWsServerUtils.mapToString(debugLog));
      //2012-06-05 17:09:19
      twoFactorDaemonLog.setStartedTimeDate(startDate);
      twoFactorDaemonLog.setEndedTimeDate(new Date());
      twoFactorDaemonLog.setMillis(new Long(elapsedTime));
      twoFactorDaemonLog.setRecordsProcessed(new Long(count));
      twoFactorDaemonLog.setServerName(PersonWsServerUtils.hostname());
      twoFactorDaemonLog.setStatus(hasError ? PersonWsDaemonLogStatus.error.toString() : PersonWsDaemonLogStatus.success.toString());
      twoFactorDaemonLog.store(twoFactorDaoFactory);
    } catch (Throwable t) {
      LOG.error("Error storing log", t);
      if (t instanceof RuntimeException) {
        throw (RuntimeException)t;
      }
      throw new RuntimeException(t);
    }
    if (hasError) {
      throw new RuntimeException("Error in dameon!", throwable);
    }
  }

  
}

