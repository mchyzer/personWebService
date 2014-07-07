/**
 * @author mchyzer
 * $Id: HibernateDaoFactory.java,v 1.1 2013/06/20 06:02:50 mchyzer Exp $
 */
package org.personWebService.server.hibernate.dao;

import org.hibernate.Session;
import org.hibernate.cfg.Configuration;
import org.personWebService.server.dao.PersonWsDaemonLogDao;
import org.personWebService.server.hibernate.PersonWsDao;
import org.personWebService.server.hibernate.PersonWsDaoFactory;



/**
 *
 */
public class HibernateDaoFactory extends PersonWsDaoFactory {

  /**
   * @see org.personWebService.server.hibernate.PersonWsDaoFactory#getConfiguration()
   */
  @Override
  public Configuration getConfiguration() {
    return PersonWsDao.getConfiguration();
  }

  /**
   * @see org.personWebService.server.hibernate.PersonWsDaoFactory#getSession()
   */
  @Override
  public Session getSession() {
    return PersonWsDao.session();
  }

  /**
   * @see org.personWebService.server.hibernate.PersonWsDaoFactory#getTransaction()
   */
  @Override
  public TransactionDAO getTransaction() {
    return new Hib3TransactionDAO();
  }


  /**
   * @see org.personWebService.server.hibernate.PersonWsDaoFactory#getTwoFactorDaemonLog()
   */
  @Override
  public PersonWsDaemonLogDao getTwoFactorDaemonLog() {
    return new HibernatePersonWsDaemonLogDao();
  }


}
