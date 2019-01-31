package org.ovirt.engine.core.dao;

import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.Label;
import org.ovirt.engine.core.compat.Guid;


/**
 * {@code LabelDao} defines a type for performing CRUD operations on instances of {@link Label}.
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
     * Retrieves all labels that are attached to hosts and VMs in given cluster.
     *
     * @param clusterId ID of the cluster
     * @return the list of labels
     */
    List<Label> getAllByClusterId(Guid clusterId);

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

    /**
     * Adds the VM to each of the labels in the provided list
     *
     * @param vmId VM id to add
     * @param labelIds the list of labels
     */
    void addVmToLabels(Guid vmId, List<Guid> labelIds);

    /**
     * Adds the host to each of the labels in the provided list
     *
     * @param hostId host id to add
     * @param labelIds the list of labels
     */
    void addHostToLabels(Guid hostId, List<Guid> labelIds);

    /**
     * Updates the VM to be associated with the provided list of labels
     *
     * @param vmId the VM to update
     * @param labelIds the list of labels
     */
    void updateLabelsForVm(Guid vmId, List<Guid> labelIds);

    /**
     * Updates the host to be associated with the provided list of labels
     *
     * @param hostId the host to update
     * @param labelIds the list of labels
     */
    void updateLabelsForHost(Guid hostId, List<Guid> labelIds);

    /**
     * Returns a map of entity ids to entity names for any
     * entity attached to a label
     *
     * @return the id to name map
     */
    Map<Guid, String> getEntitiesNameMap();
}
