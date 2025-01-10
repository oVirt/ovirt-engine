package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.common.businessentities.EngineBackupScope;

public class GetLastEngineBackupParameters extends QueryParametersBase {

    private EngineBackupScope scope;

    public GetLastEngineBackupParameters() {
    }

    public GetLastEngineBackupParameters(EngineBackupScope scope) {
        this.scope = scope;
    }

    public void setEngineBackupScope(EngineBackupScope scope) {
        this.scope = scope;
    }

    public String getEngineBackupScope() {
        return scope.getName();
    }
}
