package org.ovirt.engine.core.dao;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.Label;
import org.ovirt.engine.core.compat.Guid;


/**
 * <code>LabelDao</code> defines a type for performing CRUD operations on instances of {@link Label}.
 */
public interface LabelDao extends Dao {

    /**
     * Retrieves the label with the specified id.
     *
     * @param id
     *            the label id
     * @return the label
     */
    Label get(Guid id);

    /**
     * Retrieves the label with the specified name.
     *
     * @param name
     *            the label name
     * @return the label
     */
    Label getByName(String name);

    /**
     * Retrieves all labels.
     *
     * @return the list of labels
     */
    List<Label> getAll();

    /**
     * Retrieves all labels that are attached to the provided list of objects
     *
     * @param ids
     *            list of object ids
     * @return the list of labels
     */
    List<Label> getAllByEntityIds(Iterable<Guid> ids);

    /**
     * Retrieves multiple labels by id
     *
     * @param ids
     *            the label ids
     * @return the list of labels
     */
    List<Label> getAllByIds(Iterable<Guid> ids);

    /**
     * Saves the supplied label.
     *
     * @param label
     *            the label
     */
    void save(Label label);

    /**
     * Updates the supplied label.
     *
     * @param label
     *            the label
     */
    void update(Label label);

    /**
     * Removes the label with the specified id.
     */
    void remove(Guid id);
}
