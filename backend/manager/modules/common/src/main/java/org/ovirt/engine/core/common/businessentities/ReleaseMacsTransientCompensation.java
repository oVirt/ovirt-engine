package org.ovirt.engine.core.common.businessentities;

import java.util.List;

import org.ovirt.engine.core.compat.Guid;

public class ReleaseMacsTransientCompensation extends TransientCompensationBusinessEntity {
    private List<String> macs;
    private Guid macPoolId;

    //hide me. No-arg constructor should not be needed for deserialization. Preset only to please static analysis.
    private ReleaseMacsTransientCompensation() {
        super(TransientEntityType.RELEASE_MACS);
    }

    public ReleaseMacsTransientCompensation(Guid transientCompensationEntityId, Guid macPoolId, List<String> macs) {
        super(transientCompensationEntityId, TransientEntityType.RELEASE_MACS);

        this.macs = macs;
        this.macPoolId = macPoolId;
    }

    public List<String> getMacs() {
        return macs;
    }

    public Guid getMacPoolId() {
        return macPoolId;
    }
}
