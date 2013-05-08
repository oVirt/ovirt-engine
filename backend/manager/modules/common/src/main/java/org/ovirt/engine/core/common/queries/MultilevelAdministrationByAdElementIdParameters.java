package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.compat.Guid;

public class MultilevelAdministrationByAdElementIdParameters extends MultilevelAdministrationsQueriesParameters {
    private static final long serialVersionUID = 7614186603701768993L;

    public MultilevelAdministrationByAdElementIdParameters(Guid adElementId) {
        setAdElementId(adElementId);
    }

    private Guid privateAdElementId = new Guid();

    public Guid getAdElementId() {
        return privateAdElementId;
    }

    private void setAdElementId(Guid value) {
        privateAdElementId = value;
    }

    public MultilevelAdministrationByAdElementIdParameters() {
    }
}
