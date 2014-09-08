/**
 * @author mchyzer
 * $Id$
 */
package org.personWebService.server.operation;


/**
 * what type of step are we talking about
 */
public enum PwsOperationStepEnum {

  /**
   * traverse array
   */
  traverseArray,
  
  /**
   * traverse array by selector
   */
  traverseArrayBySelector,
  
  /**
   * simple traverse field
   */
  traverseField;
  
}
