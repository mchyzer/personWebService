package org.personWebService.server.hibernate.dao;

import org.personWebService.server.exceptions.PwsDaoException;
import org.personWebService.server.hibernate.PwsCommitType;
import org.personWebService.server.hibernate.PwsRollbackType;
import org.personWebService.server.hibernate.PersonWsTransaction;
import org.personWebService.server.hibernate.PersonWsTransactionHandler;
import org.personWebService.server.hibernate.PersonWsTransactionType;

/** 
 * methods for dealing with transactions
 * @author mchyzer
 *
 */
public interface TransactionDAO {
  /**
   * call this to send a callback for the methods.  This shouldnt be called directly,
   * it should filter through the PersonWsTransaction.callback... method
   * 
   * @param transactionType
   *          is enum of how the transaction should work.
   * @param transactionHandler
   *          will get the callback
   *          
   * @param transaction is the state of the transaction, can hold payload
   * @return the object returned from the callback
   * @throws PwsDaoException if something wrong inside, its
   * whatever your methods throw
   */
  public abstract Object transactionCallback(
      PersonWsTransactionType transactionType,
      PersonWsTransactionHandler transactionHandler,
      PersonWsTransaction transaction) throws PwsDaoException;

  /**
   * call this to commit a transaction
   * 
   * @param commitType
   *          type of commit (now or only under certain circumstances?)
   * @param transaction is the state of the transaction, can hold payload
   * @return if committed
   */
  public boolean transactionCommit(
      PersonWsTransaction transaction, PwsCommitType commitType);

  /**
   * call this to rollback a transaction
   * 
   * @param rollbackType
   *          type of commit (now or only under certain circumstances?)
   * @param transaction is the state of the transaction, can hold payload
   * @return if rolled back
   */
  public boolean transactionRollback(
      PersonWsTransaction transaction, PwsRollbackType rollbackType);

  /**
   * call this to see if a transaction is active (exists and not committed or rolledback)
   * 
   * @param transaction is the state of the transaction, can hold payload
   * @return the object returned from the callback
   */
  public abstract boolean transactionActive(
      PersonWsTransaction transaction);

}
