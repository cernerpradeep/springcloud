package org.opennms.netmgt.eventd;

import org.opennms.netmgt.dao.api.MonitoringSystemDao;
import org.opennms.netmgt.model.OnmsMonitoringSystem;
import org.springframework.stereotype.Service;

@Service
public class MonitoringSystemDaoHibernate extends AbstractDaoHibernate<OnmsMonitoringSystem, String> implements MonitoringSystemDao {

    public MonitoringSystemDaoHibernate() {
        super(OnmsMonitoringSystem.class);
    }
}
