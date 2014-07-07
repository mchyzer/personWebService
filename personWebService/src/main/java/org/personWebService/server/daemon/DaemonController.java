/**
 * @author mchyzer
 * $Id: DaemonController.java,v 1.1 2013/06/20 06:02:50 mchyzer Exp $
 */
package org.personWebService.server.daemon;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.personWebService.server.config.PersonWebServiceServerConfig;
import org.personWebService.server.util.PersonWsServerUtils;
import org.quartz.CronScheduleBuilder;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.quartz.impl.StdSchedulerFactory;


/**
 * Controller for daemons
 */
public class DaemonController {

  /** logger */
  private static final Log LOG = PersonWsServerUtils.getLog(DaemonController.class);

  /**
  * scheduler
  * @return scheduler
  */
  private static Scheduler scheduler() {
    try {
      Scheduler scheduler = schedulerFactory().getScheduler();
      return scheduler;
    } catch (SchedulerException se) {
      throw new RuntimeException(se);
    }
  }

  /**
  * scheduler factory singleton
  */
  private static SchedulerFactory schedulerFactory = null;

  /**
  * lazy load (and start the scheduler) the scheduler factory
  * @return the scheduler factory
  */
  private static SchedulerFactory schedulerFactory() {
    if (schedulerFactory == null) {
      schedulerFactory = new StdSchedulerFactory();
      try {
        schedulerFactory.getScheduler().start();
      } catch (SchedulerException se) {
        throw new RuntimeException(se);
      }
    }
    return schedulerFactory;
  }

  /** make sure we only schedule jobs once */
  private static boolean scheduledJobs = false;
  
  /**
   * schedule jobs once
   */
  public static void scheduleJobsOnce() {
    if (scheduledJobs) {
      return;
    }
    synchronized (DaemonController.class) {
      if (scheduledJobs) {
        return;
      }
      try {
        //maybe we dont run on this server...
        //# if this is true, then dont run daemons here
        //personWsServer.dontRunDaemonsHere = false
        
        //# if we arent restricting daemons in personWsServer.dontRunDaemonsHere, then if server
        //# names are listed here, then only run on this server
        //personWsServer.runOnlyOnServerNames = fasttest-small-d-01
        
        boolean runDaemons = true;
        
        if (PersonWebServiceServerConfig.retrieveConfig().propertyValueBoolean("personWsServer.dontRunDaemonsHere", false)) {
          
          LOG.warn("Daemons dont run here since this is set in personWebService.server properties file overlay: " +
          		"personWsServer.dontRunDaemonsHere, hostname: " + PersonWsServerUtils.hostname());

          runDaemons = false;
        }

        if (runDaemons) {
          
          String serverNamesString = PersonWebServiceServerConfig.retrieveConfig().propertyValueString("personWsServer.runOnlyOnServerNames");
          
          if (!StringUtils.isBlank(serverNamesString)) {
            
            List<String> serverNamesList = PersonWsServerUtils.splitTrimToList(serverNamesString, ",");
            runDaemons = false;
            for (String serverName : serverNamesList) {
              
              if (StringUtils.equalsIgnoreCase(serverName, PersonWsServerUtils.hostname())) {
                
                runDaemons = true;
                LOG.warn("Daemons running since " + serverName + "  is in the list of allowed servernames from personWebService.server config file: " +
                		"personWsServer.runOnlyOnServerNames: " + serverNamesString );
                break;
                
              }
              
            }
            
            if (!runDaemons) {

              LOG.warn("Daemons dont run here since " + PersonWsServerUtils.hostname() + " is not in the list of allowed servernames " +
              		"from personWebService.server config file: " +
                  "personWsServer.runOnlyOnServerNames: " + serverNamesString );
              
            }
            
          }
          
          
        }
        
        if (runDaemons) {
          scheduleDaemon(PwsDaemonClearingJob.class);
          scheduleDaemon(PwsDeletedClearingJob.class);
        }
        
        scheduledJobs = true;
      } catch (Throwable t) {
        LOG.error("Error scheduling jobs once", t);
      }
    }
  }
  
  /**
   * generic schedule deamon method
   * @param jobClass 
   */
  private static void scheduleDaemon(Class<? extends Job> jobClass) {
    
    try {
      Scheduler scheduler = scheduler();
      String jobName = jobClass.getSimpleName() + "Daemon";
  
      //note, in old versions of quartz, blank group cannot be used
      String quartzJobName = "fullRefresh_" + jobName;
  
      JobDetail job = JobBuilder.newJob(jobClass)
                   .withIdentity(quartzJobName, Scheduler.DEFAULT_GROUP)
                   .build();
  
      String cronString = PersonWebServiceServerConfig.retrieveConfig().propertyValueStringRequired("personWsServer." + jobClass.getSimpleName() + ".quartzCron");
      
      TriggerKey triggerKey = TriggerKey.triggerKey(jobName + "_trigger");
      Trigger trigger = TriggerBuilder.newTrigger() 
         .withIdentity(triggerKey)
         .withSchedule(CronScheduleBuilder.cronSchedule(cronString))
         .build();
  
      scheduler.scheduleJob(job, trigger);

    } catch (Throwable t) {
      LOG.error("Error scheduling " + jobClass.getSimpleName() + " daemon", t);
    }

  }

}
