package ro.pub.acs.mobiway.dao;

import org.hibernate.criterion.Restrictions;
import ro.pub.acs.mobiway.model.StreetMeanSpeed;

import org.hibernate.*;

import org.springframework.transaction.annotation.Transactional;
import java.util.List;

public class StreetMeanSpeedDAOImpl implements StreetMeanSpeedDAO {

    private SessionFactory sessionFactory;

    public StreetMeanSpeedDAOImpl(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    @Transactional
    public List<StreetMeanSpeed> getAllStreetMeanSpeeds() {

        List<StreetMeanSpeed> streetMeanSpeeds = (List<StreetMeanSpeed>) sessionFactory.getCurrentSession()
                .createCriteria(StreetMeanSpeed.class)
                .setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY).list();

        return streetMeanSpeeds;
    }

    @Override
    @Transactional
    public StreetMeanSpeed getStreetMeanSpeed(String street) {

        Criteria criteria = sessionFactory.getCurrentSession().createCriteria(StreetMeanSpeed.class).
                add(Restrictions.eq("streetName", street));

        if (criteria.uniqueResult() != null) {
            return (StreetMeanSpeed) criteria.uniqueResult();
        } else {
            return null;
        }
    }

    @Override
    @Transactional
    public void saveOrUpdateStreetMeanSpeed(StreetMeanSpeed streetMeanSpeed) {
        Session session = sessionFactory.getCurrentSession();
        //Transaction tx = session.beginTransaction();
        session.saveOrUpdate(streetMeanSpeed);
        //tx.commit();
    }

    @Override
    @Transactional
    public void deleteAllRecords() {
        Session session = sessionFactory.getCurrentSession();
        //session.createSQLQuery("truncate table street_mean_speeds").executeUpdate();
        //Transaction tx = session.beginTransaction();
        List<StreetMeanSpeed> streetMeanSpeeds = getAllStreetMeanSpeeds();
        for (StreetMeanSpeed streetMeanSpeed : streetMeanSpeeds) {
            session.delete(streetMeanSpeed);
        }
        //session.flush();
        //tx.commit();
    }
}
