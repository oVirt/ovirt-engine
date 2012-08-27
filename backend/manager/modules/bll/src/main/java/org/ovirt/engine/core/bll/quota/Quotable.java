package org.ovirt.engine.core.bll.quota;

import java.util.List;

import org.ovirt.engine.core.compat.Guid;

import org.ovirt.engine.core.bll.utils.PermissionSubject;

public interface Quotable {

    boolean validateAndSetQuota();

    void rollbackQuota();

    Guid getQuotaId();

    void addQuotaPermissionSubject(List<PermissionSubject> quotaPermissionList);
}
