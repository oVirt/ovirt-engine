package org.ovirt.engine.core.vdsbroker;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.AuditLog;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.AuditLogDAO;

public class AuditLogDaoMocker implements AuditLogDAO {

    private List<AuditLog> records = new ArrayList<>();

    @Override
    public List<AuditLog> getAllWithQuery(String query) {
        return null;
    }

    @Override
    public AuditLog get(long id) {
        return null;
    }

    @Override
    public List<AuditLog> getAllAfterDate(Date cutoff) {
        return null;
    }

    @Override
    public List<AuditLog> getAll(Guid userID, boolean isFiltered) {
        return records;
    }

    @Override
    public List<AuditLog> getAllByVMId(Guid vmId) {
        return null;
    }

    @Override
    public List<AuditLog> getAllByVMId(Guid vmId, Guid userID, boolean isFiltered) {
        return null;
    }

    @Override
    public List<AuditLog> getAllByVMTemplateId(Guid vmTemplateId) {
        return null;
    }

    @Override
    public List<AuditLog> getAllByVMTemplateId(Guid vmTemplateId, Guid userID, boolean isFiltered) {
        return null;
    }

    @Override
    public void save(AuditLog entry) {
        records.add(entry);
    }

    @Override
    public void update(AuditLog entry) {

    }

    @Override
    public void remove(long id) {

    }

    @Override
    public void removeAllBeforeDate(Date cutoff) {
    }

    @Override
    public void removeAllForVds(Guid id, boolean configAlerts) {
    }

    @Override
    public void removeAllOfTypeForVds(Guid id, int type) {
    }

    @Override
    public int getTimeToWaitForNextPmOp(String vdsName, String event) {
        return 0;
    }

    @Override
    public AuditLog getByOriginAndCustomEventId(String origin, int customEventId) {
        return null;
    }

    @Override
    public void clearAllDismissed() {
    }

}
