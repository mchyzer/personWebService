/**
 * @author mchyzer
 * $Id$
 */
package org.personWebService.server.translation;

import org.personWebService.server.beans.PwsNode;


/**
 * result of evaluation
 */
public class PwsNodeEvaluationResult {

  /**
   * node that this evaluates to
   */
  private PwsNode pwsNode;

  
  /**
   * node that this evaluates to
   * @return the pwsNode
   */
  public PwsNode getPwsNode() {
    return this.pwsNode;
  }

  
  /**
   * node that this evaluates to
   * @param pwsNode1 the pwsNode to set
   */
  public void setPwsNode(PwsNode pwsNode1) {
    this.pwsNode = pwsNode1;
  }

  /**
   * if the node was created
   */
  private boolean createdNode = false;


  
  /**
   * if the node was created
   * @return the createdNode
   */
  public boolean isCreatedNode() {
    return this.createdNode;
  }


  
  /**
   * if the node was created
   * @param createdNode1 the createdNode to set
   */
  public void setCreatedNode(boolean createdNode1) {
    this.createdNode = createdNode1;
  }

  
  
}
