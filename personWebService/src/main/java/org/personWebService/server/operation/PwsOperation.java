/**
 * @author mchyzer
 * $Id$
 */
package org.personWebService.server.operation;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.personWebService.server.cache.PersonWsCache;
import org.personWebService.server.util.PersonWsServerUtils;


/**
 * a string operation from a config file, parsed into a java object to improve performance
 */
public class PwsOperation {

  /**
   * this should be retrieved since it might be cached
   */
  private PwsOperation() {
    
  }
  
  /**
   * operation string
   */
  private String operationString;
  
  /**
   * operation string
   * @return the operationString
   */
  public String getOperationString() {
    return this.operationString;
  }
  
  /**
   * operation string
   * @param operationString1 the operationString to set
   */
  public void setOperationString(String operationString1) {
    this.operationString = operationString1;
  }

  /**
   * cache the operation parsing for 10 hours
   */
  private static PersonWsCache<String, PwsOperation> operationParseCache = new PersonWsCache<String, PwsOperation>(
      PwsOperation.class.getName() + ".operationParseCache", 100000, false, 60 * 60 * 10, 60 * 60 * 10, false);

  /** logger */
  private static final Log LOG = PersonWsServerUtils.getLog(PwsOperation.class);
  
  /**
   * retrieve an operation from cache or parse it
   * @param operationString
   * @return the operation
   */
  public static PwsOperation retrieve(String operationString) {
    
    PwsOperation pwsOperation = operationParseCache.get(operationString);
    
    if (pwsOperation == null) {

      pwsOperation = new PwsOperation();

      try {
        //use a different string so we dont cache the wrong thing
        String workingOperationString = StringUtils.trimToEmpty(operationString);
        
        //see if it is an assignment
        if (workingOperationString.contains("=")) {
          
          String[] operationParts = PersonWsServerUtils.splitTrim(workingOperationString, "=");
          
          if (operationParts.length > 2) {
            throw new RuntimeException("Assignments cannot have more than one equals sign! " + operationParts.length);
          }
          
          pwsOperation.pwsOperationEnum = PwsOperationEnum.assign;

          String destinationPart = operationParts[0];
          String sourcePart = operationParts[1];
          
          pwsOperation.destinationPwsOperationSteps = PwsOperationStep.parseExpression(null, destinationPart);
          pwsOperation.sourcePwsOperationSteps = PwsOperationStep.parseExpression(null, sourcePart);
          
        } else {
          throw new RuntimeException("Not expecting operation");
        }
      } catch (Exception e) {
        
        //dont make one assignment ruin the whole thing
        LOG.error("Error parsing operation: '" + operationString + "'", e);
        pwsOperation.setPwsOperationEnum(PwsOperationEnum.invalidOperation);
        
      }
      operationParseCache.put(operationString, pwsOperation);
      
    }
    
    return pwsOperation;
  }

  
  
  /**
   * the operation being performed
   */
  private PwsOperationEnum pwsOperationEnum;

  
  /**
   * the operation being performed
   * @return the pwsOperationEnum
   */
  public PwsOperationEnum getPwsOperationEnum() {
    return this.pwsOperationEnum;
  }

  
  /**
   * the operation being performed
   * @param pwsOperationEnum1 the pwsOperationEnum to set
   */
  public void setPwsOperationEnum(PwsOperationEnum pwsOperationEnum1) {
    this.pwsOperationEnum = pwsOperationEnum1;
  }

  /**
   * steps to get to the source to assign from, or if there is only one set of steps
   */
  private List<PwsOperationStep> sourcePwsOperationSteps;
  
  /**
   * steps to get to the source to assign from, or if there is only one set of steps
   * @return the sourcePwsOperationSteps
   */
  public List<PwsOperationStep> getSourcePwsOperationSteps() {
    return this.sourcePwsOperationSteps;
  }
  
  /**
   * steps to get to the source to assign from, or if there is only one set of steps
   * @param sourcePwsOperationSteps1 the sourcePwsOperationSteps to set
   */
  public void setSourcePwsOperationSteps(List<PwsOperationStep> sourcePwsOperationSteps1) {
    this.sourcePwsOperationSteps = sourcePwsOperationSteps1;
  }
  
  /**
   * steps to get to the destination to assign to
   */
  private List<PwsOperationStep> destinationPwsOperationSteps;
  
  /**
   * @return the destinationPwsOperationSteps
   */
  public List<PwsOperationStep> getDestinationPwsOperationSteps() {
    return this.destinationPwsOperationSteps;
  }
  
  /**
   * @param destinationPwsOperationSteps1 the destinationPwsOperationSteps to set
   */
  public void setDestinationPwsOperationSteps(List<PwsOperationStep> destinationPwsOperationSteps1) {
    this.destinationPwsOperationSteps = destinationPwsOperationSteps1;
  }

  
}

