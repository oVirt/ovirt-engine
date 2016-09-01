package org.ovirt.engine.core.dao;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.VdcOption;

/**
 * {@code VdcOptionDao} defines a type for performing CRUD operations on instances of {@link VdcOption}.
 */
public interface VdcOptionDao extends Dao {
    /**
     * Retrieves the option with the specified id.
     *
     * @param id
     *            the option id
     * @return the option
     */
    VdcOption get(int id);

    /**
     * Retrieves the option with the specified name.
     *
     * @param name
     *            the option name
     * @param version
     *            the version
     * @return the option
     */
    VdcOption getByNameAndVersion(String name, String version);

    /**
     * Retrieves all options.
     *
     * @return the list of options
     */
    List<VdcOption> getAll();

    /**
     * Saves the supplied option.
     *
     * @param option
     *            the option
     */
    void save(VdcOption option);

    /**
     * Updates the specified option.
     *
     * @param option
     *            the option
     */
    void update(VdcOption option);

    /**
     * Removes the option with the specified id.
     *
     * @param id
     *            the option
     */
    void remove(int id);
}
