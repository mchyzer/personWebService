/**
 * @author mchyzer
 * $Id$
 */
package org.personWebService.server.operation;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.personWebService.server.cache.PersonWsCache;


/**
 * step of getting to the data
 */
public class PwsOperationStep {

  /**
   * cache the operation parsing for 10 hours
   */
  private static PersonWsCache<String, List<PwsOperationStep>> operationStepParseCache = new PersonWsCache<String, List<PwsOperationStep>>(
      PwsOperationStep.class.getName() + ".operationStepParseCache", 100000, false, 60 * 60 * 10, 60 * 60 * 10, false);

  /**
   * parse an expression part that gives an operation
   * @param expression
   * @return the list of step
   */
  public static List<PwsOperationStep> parseExpression(String expression) {

    List<PwsOperationStep> pwsOperationSteps = operationStepParseCache.get(expression);
    
    if (pwsOperationSteps == null) {

      pwsOperationSteps = new ArrayList<PwsOperationStep>();

      //simple case, no dot, no nonsense
      if (!StringUtils.isBlank(expression)) {

        if (!expression.contains(".")) {
          
          PwsOperationStep pwsOperationStep = new PwsOperationStep();
          pwsOperationStep.setFieldName(expression);
          pwsOperationStep.setPwsOperationStepEnum(PwsOperationStepEnum.traverseField);
          pwsOperationSteps.add(pwsOperationStep);

        }
        
      }


      operationStepParseCache.put(expression, pwsOperationSteps);
      
    }
    
    return pwsOperationSteps;

    
  }
  
  /**
   * pws operation step enum is the type of step to take
   */
  private PwsOperationStepEnum pwsOperationStepEnum;
  
  
  /**
   * pws operation step enum is the type of step to take
   * @return the pwsOperationStepEnum
   */
  public PwsOperationStepEnum getPwsOperationStepEnum() {
    return this.pwsOperationStepEnum;
  }
  
  /**
   * pws operation step enum is the type of step to take
   * @param pwsOperationStepEnum1 the pwsOperationStepEnum to set
   */
  public void setPwsOperationStepEnum(PwsOperationStepEnum pwsOperationStepEnum1) {
    this.pwsOperationStepEnum = pwsOperationStepEnum1;
  }


  /**
   * fieldName of the stem
   */
  private String fieldName;

  
  /**
   * @return the fieldName
   */
  public String getFieldName() {
    return this.fieldName;
  }

  
  /**
   * @param fieldName1 the fieldName to set
   */
  public void setFieldName(String fieldName1) {
    this.fieldName = fieldName1;
  }
  
  
  
}
