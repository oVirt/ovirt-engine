package org.ovirt.engine.core.dao.dwh;

import org.ovirt.engine.core.common.businessentities.DwhHistoryTimekeeping;
import org.ovirt.engine.core.common.businessentities.DwhHistoryTimekeepingVariable;
import org.ovirt.engine.core.dao.DAO;

public interface DwhHistoryTimekeepingDao extends DAO {
    /**
     * Retrieves content of the specified variable
     *
     * @param variable
     *            specified variable
     * @return content of the specified variable
     */
    DwhHistoryTimekeeping get(DwhHistoryTimekeepingVariable variable);

    /**
     * Saves content of the specified variable
     *
     * @param variable
     *            specified variable
     */
    void save(DwhHistoryTimekeeping variable);
}
