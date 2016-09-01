package org.ovirt.engine.core.dao;

import java.util.Date;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.AuditLog;
import org.ovirt.engine.core.compat.Guid;

/**
 * {@code AuditLogDao} defines a type for performing CRUD operations on instances of {@link AuditLog}.
 */
public interface AuditLogDao extends Dao, SearchDao<AuditLog> {
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
     */
    AuditLog getByOriginAndCustomEventId(String origin, int customEventId);

    /**
     * Get all entries got a volume with given type
     * @param volumeId
     *            The volume id
     * @param type
     *            The entry type
     */
    List<AuditLog> getByVolumeIdAndType(Guid volumeId, int type);

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
    List<AuditLog> getAll(Guid userID, boolean isFiltered);

    /**
     * Retrieves all audit log entries for the given VM ID.
     *
     * @return the list of entries
     */
    List<AuditLog> getAllByVMId(Guid vmId);

    /**
     * Retrieves all audit log entries for the given VM name with optional permission filtering.
     *
     * @param vmId
     *            The ID of the vm to retrieve audit logs for
     * @param userID
     *            The ID of the user requesting the information
     * @param isFiltered
     *            whether the results should be filtered according to the user's permissions
     * @return the list of entries
     */
    List<AuditLog> getAllByVMId(Guid vmId, Guid userID, boolean isFiltered);

    /**
     * Retrieves all audit log entries for the given VM Template name.
     *
     *
     * @param vmTemplateId
     *          The ID of the vm template to retrieve audit logs for
     * @return the list of entries
     */
    List<AuditLog> getAllByVMTemplateId(Guid vmTemplateId);

    /**
     * Retrieves all audit log entries for the given VM Template name with optional permission filtering.
     *
     *
     * @param vmTemplateId
     *          The ID of the vm template to retrieve audit logs for
     * @param userID
     *            The ID of the user requesting the information
     * @param isFiltered
     *            whether the results should be filtered according to the user's permissions
     * @return the list of entries
     */
    List<AuditLog> getAllByVMTemplateId(Guid vmTemplateId, Guid userID, boolean isFiltered);

    /**
     * Retrieves all audit log entries for the given VOLUME ID.
     * @param brickId
     *            The ID of the brick
     * @param logtype
     *            auditlog alert/event type
     */
    void removeAllofTypeForBrick(Guid brickId, int logtype);

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
     *            if {@code true} then include config alerts
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
     * Removes entries of the specified type for the given volume id.
     * @param volumeId
     *            The volume id
     * @param type
     *            The entry type
     */
    void removeAllOfTypeForVolume(Guid volumeId, int type);

    /**
     * Get time to wait in seconds before another PM operation is allowed on the given Host
     * @param vdsName Host name
     * @param event [USER_VDS_STOP | USER_VDS_START | USER_VDS_RESTART]
     * @return number of seconds (0 or negative value if we can perform operation immediately)
     */
    public int getTimeToWaitForNextPmOp(String vdsName, String event);

    /**
     * Clears all previously dismissed audit log entries with severity other than {@link AuditLogSeverity#ALERT}.
     * Audit logs delete flag will be set back to true.
     */
    void clearAllEvents();

    /**
     * Displays all previously dismissed audit log entries with severity other than {@link AuditLogSeverity#ALERT}.
     * Audit logs delete flag will be set back to false.
     */
    void displayAllEvents();

    /**
     * Clears all audit log entries with severity {@link AuditLogSeverity#ALERT}. Audit logs
     * delete flag will be set back to true.
     */
    void clearAllAlerts();

    /**
     * Displays all audit log entries with severity {@link AuditLogSeverity#ALERT}. Audit logs
     * delete flag will be set back to false.
     */
    void displayAllAlerts();

    /**
     * Clears all backup related alerts (no backup alert or old backup alert)
     */
    void deleteBackupRelatedAlerts();
}
