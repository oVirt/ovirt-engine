package org.ovirt.engine.core.common;

import java.util.List;

import org.ovirt.engine.core.compat.Guid;

public interface Quotable {

    boolean validateAndSetQuota();

    void rollbackQuota();

    Guid getQuotaId();

    void addQuotaPermissionSubject(List<PermissionSubject> quotaPermissionList);
}
