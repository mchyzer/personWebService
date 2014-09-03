/**
 * @author mchyzer
 * $Id$
 */
package org.personWebService.server.translation;

import org.personWebService.server.beans.PwsNode;
import org.personWebService.server.beans.PwsNode.PwsNodeType;
import org.personWebService.server.operation.PwsOperation;


/**
 *
 */
public class PwsNodeTranslation {

  /**
   * assign a field from one object to another, can rename
   * @param fromNode
   * @param toNode
   * @param pwsOperation
   * @return the assignment object
   */
  static PwsNodeAssignmentResult assign(PwsNode toNode, PwsNode fromNode, PwsOperation pwsOperation) {
    
    PwsNodeAssignmentResult pwsNodeAssignmentResult = new PwsNodeAssignmentResult();

    switch (pwsOperation.getPwsOperationEnum()) {
      case assign:

        PwsNodeEvaluationResult pwsNodeEvaluationResult = PwsNodeEvaluation.evaluate(fromNode, pwsOperation.getSourcePwsOperationSteps(), false);
        
        if (pwsNodeEvaluationResult.getPwsNode() == null) {
          pwsNodeAssignmentResult.setFoundSourceLocation(false);
        } else {

          PwsNode sourceNode = pwsNodeEvaluationResult.getPwsNode();
          
          pwsNodeEvaluationResult = PwsNodeEvaluation.evaluate(toNode, pwsOperation.getDestinationPwsOperationSteps(), true);
          
          PwsNode destinationNode = pwsNodeEvaluationResult.getPwsNode();
          if (pwsNodeEvaluationResult.isCreatedNode()) {
            pwsNodeAssignmentResult.setCreatedDestinationLocation(true);
          }
          
          assignNode(destinationNode, sourceNode);
          
        }

         
         
        break;
      case invalidOperation:
      case nullOperation:
        break;
      default:
        throw new RuntimeException("Not expecting operation: " + pwsOperation.getPwsOperationEnum());
    }
    
    
    return pwsNodeAssignmentResult;
  }

  /**
   * assign (clone?) whats in the from node to the to node
   * @param toNode
   * @param fromNode
   * @return true if changed type
   */
  static boolean assignNode(PwsNode toNode, PwsNode fromNode) {
    boolean changedType = false;
    if (fromNode.getPwsNodeType() != toNode.getPwsNodeType()) {
      if (toNode.getPwsNodeType() != null) {
        //this is only a changed type if it wasnt equal and wasnt null
        changedType = true;
      }
      toNode.setPwsNodeType(fromNode.getPwsNodeType());
    }

    if (fromNode.isArrayType()) {
      throw new RuntimeException("Cant handle arrays yet");
    }
    
    //copy all the data over
    switch(fromNode.getPwsNodeType()) {
      case bool:
        toNode.setBool(fromNode.getBool());
        break;
      case floating:
        toNode.setFloating(fromNode.getFloating());
        break;
      case integer:
        toNode.setInteger(fromNode.getInteger());
        break;
      case string:
        toNode.setString(fromNode.getString());
        break;
      case object:
        throw new RuntimeException("Cant handle objects yet");
      default: 
        throw new RuntimeException("Not expecting node type: " + fromNode.getPwsNodeType());
        
    }

    
    return changedType;
  }
  
  /**
   * assign a field from one object to another, can rename
   * @param fromNode
   * @param toNode
   * @param assignment
   * @return the assignment object
   */
  public static PwsNodeAssignmentResult assign(PwsNode toNode, PwsNode fromNode, String assignment) {
    
    //get the assignment, note, these are cached
    PwsOperation pwsOperation = PwsOperation.retrieve(assignment);

    PwsNodeAssignmentResult pwsNodeAssignmentResult = assign(toNode, fromNode, pwsOperation);    
    
    return pwsNodeAssignmentResult;
  }
  
}
