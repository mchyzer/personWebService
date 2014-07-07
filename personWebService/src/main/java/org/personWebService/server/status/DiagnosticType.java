
package org.personWebService.server.status;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.personWebService.server.util.PersonWsServerUtils;

/**
 * type of diagnostics to run (trivial, deep, etc)
 * 
 * @author mchyzer
 */
public enum DiagnosticType {

  /**
   * just do a trivial memory only test
   */
  TRIVIAL {

    /**
     * @see DiagnosticType#appendDiagnostics(List)
     */
    @Override
    public void appendDiagnostics(Set<DiagnosticTask> diagnosticsTasks) {
      diagnosticsTasks.add(new DiagnosticMemoryTest());
    }
  },
  
  /**
   * just do the trivial plus the database check
   */
  DB {

    /**
     * @see DiagnosticType#appendDiagnostics(List)
     */
    @Override
    public void appendDiagnostics(Set<DiagnosticTask> diagnosticsTasks) {
      TRIVIAL.appendDiagnostics(diagnosticsTasks);
      diagnosticsTasks.add(new DiagnosticDbTest());
    }
  },
  
  /**
   * do the sources test plus the jobs
   */
  ALL {

    /**
     * @see DiagnosticType#appendDiagnostics(List)
     */
    @SuppressWarnings("unchecked")
    @Override
    public void appendDiagnostics(Set<DiagnosticTask> diagnosticsTasks) {
      DB.appendDiagnostics(diagnosticsTasks);
      
      diagnosticsTasks.add(new DiagnosticDaemonJobTest("deleteOldAudits"));
      diagnosticsTasks.add(new DiagnosticDaemonJobTest("permanentlyDeleteOldRecords"));
      
      
    }
  };
  
  /**
   * append the diagnostics for this tasks
   * @param diagnosticsTasks
   */
  public abstract void appendDiagnostics(Set<DiagnosticTask> diagnosticsTasks);
  
  /**
   * do a case-insensitive matching
   * 
   * @param string
   * @param exceptionOnNotFound true to throw exception if method not found
   * @return the enum or null or exception if not found
   * @throws GrouperRestInvalidRequest if there is a problem
   */
  public static DiagnosticType valueOfIgnoreCase(String string,
      boolean exceptionOnNotFound)  {
    return PersonWsServerUtils.enumValueOfIgnoreCase(DiagnosticType.class, 
        string, exceptionOnNotFound);
  }

}
