/**
 * @author mchyzer
 * $Id$
 */
package org.personWebService.server.operation;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.personWebService.server.cache.PersonWsCache;
import org.personWebService.server.util.PersonWsServerUtils;


/**
 * step of getting to the data
 */
public class PwsOperationStep {

  /** logger */
  private static final Log LOG = PersonWsServerUtils.getLog(PwsOperationStep.class);

  /**
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    
    StringBuilder result = new StringBuilder();
    
    result.append("{fieldName: ").append(this.fieldName)
      .append(", step: ").append(this.pwsOperationStepEnum)
      .append(", fromFieldName: ").append(this.fromFieldName);
    
    if (this.arrayIndex != -1) {
      result.append(", arrayIndex: ").append(this.arrayIndex);
    }
    result.append("}");
    return result.toString();
    
  }
  
  /**
   * create a pws operation step
   * @param fromFieldName 
   * @param operationExpression
   * @return the operation step
   */
  public static PwsOperationStep create(String fromFieldName, String operationExpression) {

    PwsOperationStep pwsOperationStep = new PwsOperationStep();
    pwsOperationStep.setFromFieldName(fromFieldName);
    String fieldName = null;

    int leftBracketIndex = PersonWsServerUtils.lastIndexOfQuoted(operationExpression,"[");
    if (leftBracketIndex > -1) {
      
      int rightBracketIndex = PersonWsServerUtils.lastIndexOfQuoted(operationExpression,"]");

      if (rightBracketIndex > -1) {
        
        fieldName = operationExpression.substring(0, leftBracketIndex);
        String indexString = operationExpression.substring(leftBracketIndex+1, rightBracketIndex);
        int index = PersonWsServerUtils.intValue(indexString);

        pwsOperationStep.setArrayIndex(index);
        pwsOperationStep.setPwsOperationStepEnum(PwsOperationStepEnum.traverseArray);

      } else  {
        throw new RuntimeException("Why doesnt matcher match??? '" + operationExpression + "'");
      }
      
    } else {
    
      pwsOperationStep.setPwsOperationStepEnum(PwsOperationStepEnum.traverseField);
      fieldName = operationExpression;

    }

    fieldName = fieldName.trim();
    
    fieldName = fieldName.trim();
    
    if ((fieldName.startsWith("\"") && fieldName.endsWith("\""))
        || (fieldName.startsWith("'") && fieldName.endsWith("'")) ){
      fieldName = fieldName.substring(1, fieldName.length()-1);
    }
    pwsOperationStep.setFieldName(fieldName);

    if (LOG.isDebugEnabled()) {
      LOG.debug("Create Step: fromFieldName: " + fromFieldName + ", expression: " + operationExpression + ", step: " + pwsOperationStep.toString() );
    }

    return pwsOperationStep;

  }
  
  /**
   * cache the operation parsing for 10 hours
   */
  private static PersonWsCache<String, List<PwsOperationStep>> operationStepParseCache = new PersonWsCache<String, List<PwsOperationStep>>(
      PwsOperationStep.class.getName() + ".operationStepParseCache", 100000, false, 60 * 60 * 10, 60 * 60 * 10, false);

  /**
   * parse an expression part that gives an operation
   * @param fromFieldName where this field is coming from, for debugging purposes
   * @param expression
   * @return the list of step
   */
  public static List<PwsOperationStep> parseExpression(String fromFieldName, String expression) {

    List<PwsOperationStep> pwsOperationSteps = operationStepParseCache.get(expression);
    
    if (pwsOperationSteps == null) {

      pwsOperationSteps = new ArrayList<PwsOperationStep>();

      //simple case, no dot, no nonsense
      if (!StringUtils.isBlank(expression)) {

        if (!PersonWsServerUtils.containsQuoted(expression, ".")) {
          
          PwsOperationStep pwsOperationStep = PwsOperationStep.create(fromFieldName, expression);
          pwsOperationSteps.add(pwsOperationStep);

        } else {
          
          //lets traverse down
          String[] expressionParts = PersonWsServerUtils.splitTrimQuoted(expression, ".");
          PwsOperationStep previousStep = null;
          for (String expressionPart : expressionParts) {
            
            PwsOperationStep pwsOperationStep = PwsOperationStep.create(
                previousStep == null ? null : previousStep.getFieldName(), expressionPart);
            pwsOperationSteps.add(pwsOperationStep);
            previousStep = pwsOperationStep;
          }
          
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
   * array index
   */
  private int arrayIndex = -1;
  
  /**
   * array index
   * @return the arrayIndex
   */
  public int getArrayIndex() {
    return this.arrayIndex;
  }
  
  /**
   * array index
   * @param arrayIndex1 the arrayIndex to set
   */
  public void setArrayIndex(int arrayIndex1) {
    this.arrayIndex = arrayIndex1;
  }

  /**
   * fieldName of the stem
   */
  private String fieldName;
  
  /**
   * field name we are coming from (for debugging reasons)
   */
  private String fromFieldName;

  
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

  /**
   * field name we are coming from (for debugging reasons)
   * @return the fromFieldName
   */
  public String getFromFieldName() {
    return this.fromFieldName;
  }

  /**
   * field name we are coming from (for debugging reasons)
   * @param fromFieldName1 the fromFieldName to set
   */
  public void setFromFieldName(String fromFieldName1) {
    this.fromFieldName = fromFieldName1;
  }
  
  
  
}
