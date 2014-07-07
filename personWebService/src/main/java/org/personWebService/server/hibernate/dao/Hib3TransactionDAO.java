
/**
 * 
 */
package org.personWebService.server.hibernate.dao;

import org.personWebService.server.exceptions.PwsDaoException;
import org.personWebService.server.hibernate.HibernateHandler;
import org.personWebService.server.hibernate.HibernateHandlerBean;
import org.personWebService.server.hibernate.HibernateSession;
import org.personWebService.server.hibernate.PwsAuditControl;
import org.personWebService.server.hibernate.PwsCommitType;
import org.personWebService.server.hibernate.PwsRollbackType;
import org.personWebService.server.hibernate.PersonWsTransaction;
import org.personWebService.server.hibernate.PersonWsTransactionHandler;
import org.personWebService.server.hibernate.PersonWsTransactionType;


/**
 * @author mchyzer
 *
 */
public class Hib3TransactionDAO implements TransactionDAO {
  
  /**
   * @see TransactionDAO#transactionActive(PersonWsTransaction)
   */
  public boolean transactionActive(PersonWsTransaction transaction) {
    HibernateSession hibernateSession = (HibernateSession)transaction._internal_getPayload();
    return hibernateSession == null ? false : hibernateSession.isTransactionActive();
  }

  /**
   * any runtime exceptions will propagate to the outer method call
   * @see TransactionDAO#transactionCallback(PersonWsTransactionType, PersonWsTransactionHandler, PersonWsTransaction)
   */
  public Object transactionCallback(
      final PersonWsTransactionType transactionType,
      final PersonWsTransactionHandler transactionHandler,
      final PersonWsTransaction transaction) throws PwsDaoException {
    
    Object result = HibernateSession.callbackHibernateSession(
        transactionType, PwsAuditControl.WILL_NOT_AUDIT, new HibernateHandler() {

      public Object callback(HibernateHandlerBean hibernateHandlerBean)
          throws PwsDaoException {
        HibernateSession hibernateSession = hibernateHandlerBean.getHibernateSession();

        //set the session object
        transaction._internal_setPayload(hibernateSession);
        try {
          return transactionHandler.callback(transaction);
        } finally {
          //clear this
          transaction._internal_setPayload(null);
        }
      }
      
    });
    
    return result;
  }

  /** 
   * @see TransactionDAO#transactionCommit(PersonWsTransaction, PwsCommitType)
   */
  public boolean transactionCommit(PersonWsTransaction transaction,
      PwsCommitType commitType) {
    HibernateSession hibernateSession = (HibernateSession)transaction._internal_getPayload();
    return hibernateSession == null ? false : hibernateSession.commit(commitType);
  }

  /**
   * @see TransactionDAO#transactionRollback(PersonWsTransaction, PwsRollbackType)
   */
  public boolean transactionRollback(PersonWsTransaction transaction,
      PwsRollbackType rollbackType) {
    HibernateSession hibernateSession = (HibernateSession)transaction._internal_getPayload();
    return hibernateSession == null ? false : hibernateSession.rollback(rollbackType);
  }

}
