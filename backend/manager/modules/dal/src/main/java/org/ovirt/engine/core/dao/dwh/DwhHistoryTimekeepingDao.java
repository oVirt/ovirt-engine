package org.ovirt.engine.core.dao.dwh;

import org.ovirt.engine.core.common.businessentities.DwhHistoryTimekeeping;
import org.ovirt.engine.core.common.businessentities.DwhHistoryTimekeepingVariable;
import org.ovirt.engine.core.dao.Dao;

public interface DwhHistoryTimekeepingDao extends Dao {
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
