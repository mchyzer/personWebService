/**
 * @author mchyzer
 * $Id: TwoFactorCloneable.java,v 1.1 2013/06/20 06:02:50 mchyzer Exp $
 */
package org.personWebService.server.util;


/**
 *
 */
public interface PwsCloneable extends Cloneable {

  /**
   * @see Object#clone()
   * public clone method
   * @return a clone of this object
   */
  public Object clone();
  
}
