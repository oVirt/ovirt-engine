package org.ovirt.engine.core.common.action;

import java.util.Collections;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.NonOperationalReason;
import org.ovirt.engine.core.compat.Guid;

public class SetNonOperationalVdsParameters extends MaintenanceVdsParameters {
    private static final long serialVersionUID = -2719283555117621122L;

    private Map<String, String> customLogValues;

    private NonOperationalReason nonOperationalReason;

    private Guid privateStorageDomainId;

    public Guid getStorageDomainId() {
        return privateStorageDomainId;
    }

    public void setStorageDomainId(Guid value) {
        privateStorageDomainId = value;
    }

    public SetNonOperationalVdsParameters(Guid vdsId,
            NonOperationalReason reason) {
        this(vdsId, reason, null);
    }

    public SetNonOperationalVdsParameters(Guid vdsId,
            NonOperationalReason reason,
            Map<String, String> customLogValues) {
        super(vdsId, true);
        setNonOperationalReason(reason);
        this.customLogValues =
                (Map<String, String>) (customLogValues == null ? Collections.emptyMap() : customLogValues);
        privateStorageDomainId = Guid.Empty;
    }

    public SetNonOperationalVdsParameters() {
        nonOperationalReason = NonOperationalReason.NONE;
        privateStorageDomainId = Guid.Empty;
    }

    public void setNonOperationalReason(NonOperationalReason nonOperationalReason) {
        this.nonOperationalReason = nonOperationalReason;
    }

    public NonOperationalReason getNonOperationalReason() {
        return nonOperationalReason;
    }

    public Map<String, String> getCustomLogValues() {
        return customLogValues;
    }

}
