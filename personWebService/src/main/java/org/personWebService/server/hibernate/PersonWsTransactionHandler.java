
package org.personWebService.server.hibernate;

import org.personWebService.server.exceptions.PwsDaoException;

/**
 * Use this class to make your anonymous inner class for 
 * transactions with (if transactions are supported by your DAO strategy
 * configured in properties files
 * 
 * 
 * @author mchyzer
 *
 */
public interface PersonWsTransactionHandler {
  
  /**
   * This method will be called with the transaction object to do 
   * what you wish.  Note, RuntimeExceptions can be
   * thrown by this method... others should be handled somehow.
   * @param transaction is the transaction
   * @return the return value to be passed to return value of callback method
   * @throws PwsDaoException if there is a problem, or runtime ones
   */
  public Object callback(PersonWsTransaction transaction) throws PwsDaoException;

}
