package org.opennms.netmgt.eventd;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.opennms.netmgt.dao.api.EventDao;
import org.opennms.netmgt.model.OnmsEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.orm.hibernate4.HibernateCallback;
import org.springframework.orm.hibernate4.HibernateTemplate;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

@Service
public class EventDaoHibernate extends AbstractDaoHibernate<OnmsEvent, Integer> implements EventDao {
	
	@Autowired
	private HibernateTemplate hibernateTemplate;

	public EventDaoHibernate() {
		super(OnmsEvent.class);
	}

    /** {@inheritDoc} */
        @Override
    public int deletePreviousEventsForAlarm(Integer id, OnmsEvent e) throws DataAccessException {
        String hql = "delete from OnmsEvent where alarmid = ? and eventid != ?";
        Object[] values = {id, e.getId()};
        return bulkDelete(hql, values);
    }

    @Override
    public List<OnmsEvent> getEventsAfterDate(final List<String> ueiList, final Date date) {
        final String hql = "From OnmsEvent e where e.eventUei in (:eventUei) and e.eventTime > :eventTime order by e.eventTime desc";

        return (List<OnmsEvent>)hibernateTemplate.execute(new HibernateCallback<List<OnmsEvent>>() {
            @Override
            public List<OnmsEvent> doInHibernate(Session session) throws HibernateException {
                return session.createQuery(hql)
                        .setParameterList("eventUei", ueiList)
                        .setParameter("eventTime", date)
                        .list();
            }
        });
    }

}

