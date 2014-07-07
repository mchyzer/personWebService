/**
 * @author mchyzer
 * $Id: HibernatePersonWsDaemonLogDao.java,v 1.1 2013/06/20 06:02:50 mchyzer Exp $
 */
package org.personWebService.server.hibernate.dao;

import java.util.List;

import org.personWebService.server.beans.PersonWsDaemonLog;
import org.personWebService.server.dao.PersonWsDaemonLogDao;
import org.personWebService.server.hibernate.ByHqlStatic;
import org.personWebService.server.hibernate.HibernateSession;
import org.personWebService.server.hibernate.PwsQueryOptions;
import org.personWebService.server.util.PersonWsServerUtils;



/**
 * implementation of daemon log dao
 */
public class HibernatePersonWsDaemonLogDao implements PersonWsDaemonLogDao {


  /**
   * @see org.personWebService.server.dao.PersonWsDaemonLogDao#delete(org.personWebService.server.beans.PersonWsDaemonLog)
   */
  @Override
  public void delete(PersonWsDaemonLog twoFactorDaemonLog) {
    HibernateSession.byObjectStatic().delete(twoFactorDaemonLog);
  }

  /**
   * @see org.personWebService.server.dao.PersonWsDaemonLogDao#store(org.personWebService.server.beans.PersonWsDaemonLog)
   */
  @Override
  public void store(PersonWsDaemonLog twoFactorDaemonLog) {
    if (twoFactorDaemonLog == null) {
      throw new RuntimeException("Why is daemon null?");
    }
    HibernateSession.byObjectStatic().saveOrUpdate(twoFactorDaemonLog);
  }

  /**
   * @see org.personWebService.server.dao.PersonWsDaemonLogDao#retrieveByUuid(java.lang.String)
   */
  @Override
  public PersonWsDaemonLog retrieveByUuid(String uuid) {
    if (PersonWsServerUtils.isBlank(uuid)) {
      throw new RuntimeException("Why is uuid blank? ");
    }

    List<PersonWsDaemonLog> theList = HibernateSession.byHqlStatic().createQuery(
        "select tfdl from PersonWsDaemonLog as tfdl where tfdl.uuid = :theUuid")
        .setString("theUuid", uuid)
        .list(PersonWsDaemonLog.class);

    PersonWsDaemonLog twoFactorDaemonLog = PersonWsServerUtils.listPopOne(theList);
    
    return twoFactorDaemonLog;
  }

  /**
   * @see org.personWebService.server.dao.PersonWsDaemonLogDao#retrieveDeletedOlderThanAge(long)
   */
  @Override
  public List<PersonWsDaemonLog> retrieveDeletedOlderThanAge(long selectBeforeThisMilli) {
    List<PersonWsDaemonLog> theList = HibernateSession.byHqlStatic().createQuery(
      "select tfdl from PersonWsDaemonLog as tfdl where tfdl.deletedOn is not null and tfdl.deletedOn < :selectBeforeThisMilli")
      .setLong("selectBeforeThisMilli", selectBeforeThisMilli)
      .options(new PwsQueryOptions().paging(1000, 1,false))
      .list(PersonWsDaemonLog.class);
    return theList;
  }

  /**
   * @see org.personWebService.server.dao.PersonWsDaemonLogDao#retrieveOlderThanAge(long)
   */
  @Override
  public List<PersonWsDaemonLog> retrieveOlderThanAge(long selectBeforeThisMilli) {
    ByHqlStatic byHqlStatic = HibernateSession.byHqlStatic();
    
    List<PersonWsDaemonLog> theList = byHqlStatic.createQuery(
      "select tfdl from PersonWsDaemonLog as tfdl where tfdl.deletedOn is null and tfa.theTimestamp < :selectBeforeThisMilli")
      .setLong("selectBeforeThisMilli", selectBeforeThisMilli)
      .options(new PwsQueryOptions().paging(1000, 1,false))
      .list(PersonWsDaemonLog.class);
    return theList;
  }

  /**
   * @see PersonWsDaemonLogDao#retrieveMostRecentSuccessTimestamp(String)
   */
  @Override
  public Long retrieveMostRecentSuccessTimestamp(String daemonName) {
    Long result = HibernateSession.byHqlStatic().createQuery(
        "select max(tfdl.theTimestamp) from PersonWsDaemonLog as tfdl where " +
        " tfdl.status = 'success' and tfdl.daemonName = :theDaemonName ")
        .setString("theDaemonName", daemonName)
        .uniqueResult(Long.class);

    return result;
    
  }

}
