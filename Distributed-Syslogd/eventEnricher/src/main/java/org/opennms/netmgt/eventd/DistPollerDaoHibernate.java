package org.opennms.netmgt.eventd;

import org.opennms.netmgt.dao.api.DistPollerDao;
import org.opennms.netmgt.model.OnmsDistPoller;
import org.springframework.stereotype.Service;

@Service
public class DistPollerDaoHibernate extends AbstractDaoHibernate<OnmsDistPoller, String> implements DistPollerDao {

    /**
     * <p>Constructor for DistPollerDaoHibernate.</p>
     */
    public DistPollerDaoHibernate() {
        super(OnmsDistPoller.class);
    }

    @Override
    public OnmsDistPoller whoami() {
        // Return the OnmsDistPoller with the default UUID
        return get(DEFAULT_DIST_POLLER_ID);
    }
}

