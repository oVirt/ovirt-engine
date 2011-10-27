package org.ovirt.engine.core.common.action;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import org.ovirt.engine.core.common.businessentities.NonOperationalReason;
import org.ovirt.engine.core.compat.Guid;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "SetNonOperationalVdsParameters")
public class SetNonOperationalVdsParameters extends MaintananceVdsParameters {
    private static final long serialVersionUID = -2719283555117621122L;

    @XmlElement(name = "SaveToDb")
    private boolean privateSaveToDb;

    public boolean getSaveToDb() {
        return privateSaveToDb;
    }

    public void setSaveToDb(boolean value) {
        privateSaveToDb = value;
    }

    @XmlElement(name = "NonOperationalReason")
    private NonOperationalReason nonOperationalReason = NonOperationalReason.forValue(0);

    @XmlElement(name = "StorageDomainId")
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
