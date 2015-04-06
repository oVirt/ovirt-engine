package org.ovirt.engine.core.vdsbroker;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.AuditLog;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.AuditLogDAO;

public class AuditLogDaoMocker implements AuditLogDAO {

    private List<AuditLog> recoreds = new ArrayList<AuditLog>();
    @Override
    public List<AuditLog> getAllWithQuery(String query) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public AuditLog get(long id) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<AuditLog> getAllAfterDate(Date cutoff) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<AuditLog> getAll(Guid userID, boolean isFiltered) {
        return recoreds;
    }

    @Override
    public List<AuditLog> getAllByVMId(Guid vmId) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<AuditLog> getAllByVMId(Guid vmId, Guid userID, boolean isFiltered) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<AuditLog> getAllByVMTemplateId(Guid vmTemplateId) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<AuditLog> getAllByVMTemplateId(Guid vmTemplateId, Guid userID, boolean isFiltered) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void save(AuditLog entry) {
        recoreds.add(entry);
    }

    @Override
    public void update(AuditLog entry) {

    }

    @Override
    public void remove(long id) {
        // TODO Auto-generated method stub

    }

    @Override
    public void removeAllBeforeDate(Date cutoff) {
        // TODO Auto-generated method stub

    }

    @Override
    public void removeAllForVds(Guid id, boolean configAlerts) {
        // TODO Auto-generated method stub

    }

    @Override
    public void removeAllOfTypeForVds(Guid id, int type) {
        // TODO Auto-generated method stub

    }

    @Override
    public void removeAllofTypeForBrick(Guid id, int type) {
        // TODO Auto-generated method stub

    }

    @Override
    public int getTimeToWaitForNextPmOp(String vdsName, String event) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public AuditLog getByOriginAndCustomEventId(String origin, int customEventId) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void clearAllDismissed() {
    }

    @Override
    public List<AuditLog> getByVolumeIdAndType(Guid volumeId, int type) {
        return null;
    }

    @Override
    public void removeAllOfTypeForVolume(Guid volumeId, int type) {
    }
}
