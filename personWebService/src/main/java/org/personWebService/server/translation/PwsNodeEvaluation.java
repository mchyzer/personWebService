/**
 * @author mchyzer
 * $Id$
 */
package org.personWebService.server.translation;

import java.util.List;

import org.personWebService.server.beans.PwsNode;
import org.personWebService.server.operation.PwsOperationStep;
import org.personWebService.server.util.PersonWsServerUtils;


/**
 * evaluate an expression against a node
 */
public class PwsNodeEvaluation {

  /**
   * evaluate an operation step on a node
   * @param currentNode
   * @param pwsOperationStep
   * @param autoCreate
   * @param pwsNodeEvaluationResult
   * @return the new node
   */
  static PwsNode evaluate(PwsNode currentNode, PwsOperationStep pwsOperationStep, 
      boolean autoCreate, PwsNodeEvaluationResult pwsNodeEvaluationResult) {
    
    switch (pwsOperationStep.getPwsOperationStepEnum()) {
      
      case traverseField:
        
        PwsNode result = currentNode.retrieveField(pwsOperationStep.getFieldName());
        
        //doesnt have this field, add it, tell the result it is added
        if (result == null && autoCreate) {
          pwsNodeEvaluationResult.setCreatedNode(true);
          result = new PwsNode();
          currentNode.assignField(pwsOperationStep.getFieldName(), result);
        }
        
        return result;
      default:
        throw new RuntimeException("Not expecting PwsOperationStemEnum: " + pwsOperationStep.getPwsOperationStepEnum());
      
    }
    
  }
  
  /**
   * evaluate steps against a node
   * @param pwsNode
   * @param pwsOperationSteps
   * @param autoCreate if should autocreate
   * @return the result
   */
  static PwsNodeEvaluationResult evaluate(PwsNode pwsNode, 
      List<PwsOperationStep> pwsOperationSteps, boolean autoCreate) {
    
    PwsNodeEvaluationResult pwsNodeEvaluationResult = new PwsNodeEvaluationResult();
    
    PwsNode currentNode = pwsNode;
    
    for (PwsOperationStep pwsOperationStep : PersonWsServerUtils.nonNull(pwsOperationSteps)) {
      
      currentNode = evaluate(currentNode, pwsOperationStep, autoCreate, pwsNodeEvaluationResult);
      
      //if node is not found and not autocreate
      if (currentNode == null) {
        break;
      }
      
    }
    
    pwsNodeEvaluationResult.setPwsNode(currentNode);

    return pwsNodeEvaluationResult;
  }
  
}
