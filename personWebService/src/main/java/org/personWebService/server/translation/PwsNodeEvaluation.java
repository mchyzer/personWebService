/**
 * @author mchyzer
 * $Id$
 */
package org.personWebService.server.translation;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.personWebService.server.beans.PwsNode;
import org.personWebService.server.beans.PwsNode.PwsNodeType;
import org.personWebService.server.operation.PwsOperationStep;
import org.personWebService.server.util.PersonWsServerUtils;


/**
 * evaluate an expression against a node
 */
public class PwsNodeEvaluation {

  /** logger */
  private static final Log LOG = PersonWsServerUtils.getLog(PwsNodeEvaluation.class);

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
    
    Map<String, Object> debugLog = LOG.isDebugEnabled() ? new LinkedHashMap<String, Object>() : null;

    try {
      if (LOG.isDebugEnabled()) {
        debugLog.put("currentNode", currentNode);
        debugLog.put("step", pwsOperationStep);
        debugLog.put("autoCreate", autoCreate);
      }
      
      PwsNode result = null;
      
      switch (pwsOperationStep.getPwsOperationStepEnum()) {
        
        case traverseArray:
  
          result = currentNode.retrieveField(pwsOperationStep.getFieldName());
  
          if (LOG.isDebugEnabled()) {
            debugLog.put("foundNode", result != null);
          }
  
          //doesnt have this field, add it, tell the result it is added
          if (result == null) {
            if (autoCreate) {
              pwsNodeEvaluationResult.setCreatedNode(true);
  
              //i dont think we really know the type at this point
              result = new PwsNode(PwsNodeType.object);
              result.setFromFieldName(pwsOperationStep.getFieldName());
              result.setFromNode(currentNode);
              currentNode.assignField(pwsOperationStep.getFieldName(), result);
              result.setArrayType(true);
            } else {
              //dont auto create, not there
              return null;
            }
          }
          if (LOG.isDebugEnabled()) {
            debugLog.put("node", result);
          }
  
          if (!result.isArrayType()) {
            if (autoCreate) {
              pwsNodeEvaluationResult.setChangedArrayType(true);
              result.setArrayType(true);
            } else {
              throw new RuntimeException("Not an array type: " 
                  + pwsOperationStep.getFieldName() + ", " + pwsOperationStep.getArrayIndex());
            }
          }
          
          //see if not enough there, and maybe auto create
          int createItemCount = (pwsOperationStep.getArrayIndex()+1) - result.getArrayLength();
          
          if (createItemCount > 0) {
            if (LOG.isDebugEnabled()) {
              debugLog.put("createItemCount", createItemCount);
            }
            if (autoCreate) {
              PwsNode currentArrayNode = null;
              //create some more nodes
              for (int i = 0; i<createItemCount; i++ ) {
                currentArrayNode = new PwsNode(result.getPwsNodeType());
                currentArrayNode.setFromNode(result);
                currentArrayNode.setFromFieldName(pwsOperationStep.getFieldName());
                result.addArrayItem(currentArrayNode);
              }
              if (LOG.isDebugEnabled()) {
                debugLog.put("return", currentArrayNode);
              }
  
              return currentArrayNode;
            }
            
            if (LOG.isDebugEnabled()) {
              debugLog.put("return", null);
            }
  
            return null;
          }
          PwsNode theResult = result.retrieveArrayItem(pwsOperationStep.getArrayIndex());
          if (LOG.isDebugEnabled()) {
            debugLog.put("theResult", theResult);
          }
          return theResult;
          
        case traverseField:
  
          result = currentNode.retrieveField(pwsOperationStep.getFieldName());
  
          if (LOG.isDebugEnabled()) {
            debugLog.put("foundNode", result != null);
          }
  
          //doesnt have this field, add it, tell the result it is added
          if (result == null && autoCreate) {
  
            pwsNodeEvaluationResult.setCreatedNode(true);
            result = new PwsNode(PwsNodeType.object);
            result.setFromFieldName(pwsOperationStep.getFieldName());
            result.setFromNode(currentNode);
            currentNode.assignField(pwsOperationStep.getFieldName(), result);
          }
  
          if (LOG.isDebugEnabled()) {
            debugLog.put("node", result);
          }
  
          return result;
        default:
          throw new RuntimeException("Not expecting PwsOperationStemEnum: " + pwsOperationStep.getPwsOperationStepEnum());
        
      }
    } finally {
      if (LOG.isDebugEnabled()) {
        LOG.debug(PersonWsServerUtils.mapToString(debugLog));
      }
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
