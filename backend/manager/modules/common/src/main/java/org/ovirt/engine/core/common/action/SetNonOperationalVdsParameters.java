package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.common.businessentities.NonOperationalReason;
import org.ovirt.engine.core.compat.Guid;

public class SetNonOperationalVdsParameters extends MaintananceVdsParameters {
    private static final long serialVersionUID = -2719283555117621122L;

    private boolean privateSaveToDb;

    public boolean getSaveToDb() {
        return privateSaveToDb;
    }

    public void setSaveToDb(boolean value) {
        privateSaveToDb = value;
    }

    private NonOperationalReason nonOperationalReason = NonOperationalReason.forValue(0);

    private Guid privateStorageDomainId = new Guid();

    public Guid getStorageDomainId() {
        return privateStorageDomainId;
    }

    public void setStorageDomainId(Guid value) {
        privateStorageDomainId = value;
    }

    public SetNonOperationalVdsParameters(Guid vdsId, NonOperationalReason reason) {
        super(vdsId, true);
        setNonOperationalReason(reason);
    }

    public SetNonOperationalVdsParameters() {
    }

    public void setNonOperationalReason(NonOperationalReason nonOperationalReason) {
        this.nonOperationalReason = nonOperationalReason;
    }

    public NonOperationalReason getNonOperationalReason() {
        return nonOperationalReason;
    }

}
