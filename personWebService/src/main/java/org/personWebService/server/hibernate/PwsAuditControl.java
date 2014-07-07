
/*
 * @author mchyzer
 * $Id: PwsAuditControl.java,v 1.1 2013/06/20 06:02:50 mchyzer Exp $
 */
package org.personWebService.server.hibernate;


/**
 *
 */
public enum PwsAuditControl {

  /** will audit this call (or will defer to outside context if auditing */
  WILL_AUDIT, 
  
  /** will not audit */
  WILL_NOT_AUDIT;
  
}
