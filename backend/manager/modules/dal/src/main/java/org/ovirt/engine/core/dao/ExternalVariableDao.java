package org.ovirt.engine.core.dao;

import org.ovirt.engine.core.common.ExternalVariable;

public interface ExternalVariableDao extends Dao {
    /**
     * Inserts variable in db
     *
     * @param var
     *            variable to save
     */
    public void save(ExternalVariable var);

    /**
     * Updates variable in db
     *
     * @param var
     *            variable to save
     */
    public void update(ExternalVariable var);

    /**
     * Inserts or updates variable in db
     *
     * @param var
     *            variable to save
     */
    public void saveOrUpdate(ExternalVariable var);

    /**
     * Removes variable from db
     *
     * @param name
     *            name of the variable to remove
     */
    public void remove(String name);

    /**
     * Get variable from db
     *
     * @param name
     *            name of the variable to get
     */
    public ExternalVariable get(String name);
}
