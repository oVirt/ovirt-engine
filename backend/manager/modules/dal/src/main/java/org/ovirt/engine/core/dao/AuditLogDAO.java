package org.ovirt.engine.core.dao;

import java.util.Date;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.AuditLog;
import org.ovirt.engine.core.compat.Guid;

/**
 * <code>AuditLogDAO</code> defines a type for performing CRUD operations on instances of {@link AuditLog}.
 *
 *
 */
public interface AuditLogDAO extends DAO, SearchDAO<AuditLog> {
    /**
     * Retrieves the entry with the given id.
     *
     * @param id
     *            the entry id
     * @return the entry
     */
    AuditLog get(long id);

    /**
     * Get External Event injected by a plug-in
     * @param origin
     *            the event origin
     * @param customEventId
     *            the unique ide per origin associated with the event instance
     * @return
     */
    AuditLog getByOriginAndCustomEventId(String origin, int customEventId);

    /**
     * Finds all entries created after the specified cutoff date
     *
     * @param cutoff
     *            the cutoff date
     * @return the list of entries
     */
    List<AuditLog> getAllAfterDate(Date cutoff);

    /**
     * Retrieves all audit log entries.
     *
     * @return the list of entries
     */
    List<AuditLog> getAll();

    /**
     * Retrieves all audit log entries for the given VM name.
     *
     * @param vmName
     *          The name of the vm to retrieve audit logs for
     * @return the list of entries
     */
    List<AuditLog> getAllByVMName(String vmName);

    /**
     * Retrieves all audit log entries for the given VM name with optional permission filtering.
     *
     * @param vmName
     *            The name of the vm to retrieve audit logs for
     * @param userID
     *            The ID of the user requesting the information
     * @param isFiltered
     *            whether the results should be filtered according to the user's permissions
     * @return the list of entries
     */
    List<AuditLog> getAllByVMName(String vmName, Guid userID, boolean isFiltered);

    /**
     * Retrieves all audit log entries for the given VM Template name.
     *
     * @param vmTemplateName
     *          The name of the vm template to retrieve audit logs for
     * @return the list of entries
     */
    List<AuditLog> getAllByVMTemplateName(String vmTemplateName);

    /**
     * Retrieves all audit log entries for the given VM Template name with optional permission filtering.
     *
     * @param vmTemplateName
     *            The name of the vm template to retrieve audit logs for
     * @param userID
     *            The ID of the user requesting the information
     * @param isFiltered
     *            whether the results should be filtered according to the user's permissions
     * @return the list of entries
     */
    List<AuditLog> getAllByVMTemplateName(String vmTemplateName, Guid userID, boolean isFiltered);

    /**
     * Saves the provided audit log
     *
     * @param entry
     *            the entry
     */
    void save(AuditLog entry);

    /**
     * Updates the provided audit log entry.
     *
     * @param entry
     *            the entry
     */
    void update(AuditLog entry);

    /**
     * Removes the entry with the given id.
     *
     * @param id
     *            the entry id
     */
    void remove(long id);

    /**
     * Removes all entries before the specified cutoff date
     *
     * @param cutoff
     *            the cutoff date
     */
    void removeAllBeforeDate(Date cutoff);

    /**
     * Removes all entries for the given VDS id.
     *
     * @param id
     *            the vds id
     * @param configAlerts
     *            if <code>true</code> then include config alerts
     */
    void removeAllForVds(Guid id, boolean configAlerts);

    /**
     * Removes entries of the specified type for the given VDS id.
     *
     * @param id
     *            the VDS id
     * @param type
     *            the entry type
     */
    void removeAllOfTypeForVds(Guid id, int type);

    /**
     * Get time to wait in seconds before another PM operation is allowed on the given Host
     * @param vdsName Host name
     * @param event [USER_VDS_STOP | USER_VDS_START | USER_VDS_RESTART]
     * @return number of seconds (0 or negative value if we can perform operation immediately)
     */
    public int getTimeToWaitForNextPmOp(String vdsName, String event);
}
