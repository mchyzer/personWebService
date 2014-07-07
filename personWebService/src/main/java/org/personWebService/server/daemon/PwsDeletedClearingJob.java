/**
 * @author mchyzer
 * $Id: PwsDeletedClearingJob.java,v 1.1 2013/06/20 06:02:50 mchyzer Exp $
 */
package org.personWebService.server.daemon;

import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.personWebService.server.beans.PersonWsDaemonLog;
import org.personWebService.server.beans.PersonWsDaemonLogStatus;
import org.personWebService.server.config.PersonWebServiceServerConfig;
import org.personWebService.server.hibernate.PersonWsDaoFactory;
import org.personWebService.server.hibernate.PersonWsHibernateBeanBase;
import org.personWebService.server.util.PersonWsServerUtils;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;



/**
 * daemon which clears out deleted records
 */
public class PwsDeletedClearingJob implements Job {

  /** logger */
  private static final Log LOG = PersonWsServerUtils.getLog(PwsDeletedClearingJob.class);

  /**
   * @see org.quartz.Job#execute(org.quartz.JobExecutionContext)
   */
  @Override
  public void execute(JobExecutionContext context) throws JobExecutionException {
    deletedClearingLogic(PersonWsDaoFactory.getFactory());
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
  public void deletedClearingLogic(final PersonWsDaoFactory twoFactorDaoFactory) {
    
    boolean hasError = false;

    long start = System.nanoTime();
    Date startDate = new Date();
    int elapsedTime = -1;
    
    Map<String, Object> debugLog = new LinkedHashMap<String, Object>();

    int count = 0;
    int[] errorCount = new int[]{0};
    
    Throwable throwable = null;
    try {
      
      int deleteRecordsAfterMinutes = PersonWebServiceServerConfig.retrieveConfig().propertyValueInt("personWsServer.purgeDeletedRecordsAfterMinutes", 2880);
      long deleteBeforeMilli = System.currentTimeMillis() - (deleteRecordsAfterMinutes * 60L * 1000);
      
      if (deleteRecordsAfterMinutes < 0) {
        throw new RuntimeException("Why is personWsServer.purgeDeletedRecordsAfterMinutes less than 0???");
      }

      debugLog.put("delete before", new Date(deleteBeforeMilli));

      for (int i=0;i<10000;i++) {
        List<PersonWsDaemonLog> twoFactorDaemonLogs = twoFactorDaoFactory.getTwoFactorDaemonLog()
          .retrieveDeletedOlderThanAge(deleteBeforeMilli);
        int records = deleteRecords(twoFactorDaoFactory, twoFactorDaemonLogs, debugLog, "twoFactorDaemonLogs", i, errorCount);
        count += records;
        if (records == 0) {
          break;
        }
      }


    } catch (Throwable t) {
      hasError = true;
      debugLog.put("exception", PersonWsServerUtils.getFullStackTrace(t));
      LOG.error(PersonWsServerUtils.mapToString(debugLog));
      LOG.error("Error in daemon", t);
      throwable = t;
    } finally {
      if (errorCount[0] > 0) {
        debugLog.put("errorCount", errorCount);
      }
      elapsedTime = (int)((System.nanoTime() - start) / 1000000);
      debugLog.put("tookMillis", elapsedTime);
      debugLog.put("total records", count);
      PwsDaemonLog.daemonLog(debugLog);
      testingTotalCount = count;
    }

    try {
      //lets store to DB
      PersonWsDaemonLog twoFactorDaemonLog = new PersonWsDaemonLog();
      twoFactorDaemonLog.setUuid(PersonWsServerUtils.uuid());
      twoFactorDaemonLog.setDaemonName(PersonWsDaemonName.permanentlyDeleteOldRecords.name());
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

  /**
   * delete some records
   * @param debugLabel                     
   * @param twoFactorDaoFactory 
   * @param beans
   * @param debugLog 
   * @param passIndex 
   * @param errorCount 
   * @return the number of records deleted
   */
  public static int deleteRecords(PersonWsDaoFactory twoFactorDaoFactory,
      Collection<? extends PersonWsHibernateBeanBase> beans, Map<String, Object> debugLog, 
      String debugLabel, int passIndex, int[] errorCount) {

    if (PersonWsServerUtils.length(beans) > 0) {
      for (PersonWsHibernateBeanBase bean : beans) {
        
        try {
          bean.delete(twoFactorDaoFactory);
        } catch (Exception e) {
          LOG.error("Error in record: " + debugLabel + " id: " + bean.getUuid(), e);
          if (++errorCount[0] > 50) {
            throw new RuntimeException("Error count: " + errorCount[0]);
          }
        }
      }
      testingLoopsThrough++;
    }
    
    debugLog.put("deleting " + debugLabel + " index " + passIndex + " records", PersonWsServerUtils.length(beans));

    return PersonWsServerUtils.length(beans);

  }
}

