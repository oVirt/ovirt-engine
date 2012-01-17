package org.ovirt.engine.core.common.vdscommands;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import org.ovirt.engine.core.common.businessentities.storage_domain_static;
import org.ovirt.engine.core.compat.Guid;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "CreateStorageDomainVDSCommandParameters")
public class CreateStorageDomainVDSCommandParameters extends VdsIdVDSCommandParametersBase {
    @XmlElement(name = "StorageDomain")
    private storage_domain_static privateStorageDomain;

    public storage_domain_static getStorageDomain() {
        return privateStorageDomain;
    }

    private void setStorageDomain(storage_domain_static value) {
        privateStorageDomain = value;
    }

    @XmlElement(name = "Args")
    private String privateArgs;

    public String getArgs() {
        return privateArgs;
    }

    private void setArgs(String value) {
        privateArgs = value;
    }

    public CreateStorageDomainVDSCommandParameters(Guid vdsId, storage_domain_static storageDomain, String args) {
        super(vdsId);
        setStorageDomain(storageDomain);
        setArgs(args);
    }

    public CreateStorageDomainVDSCommandParameters() {
    }

    @Override
    public String toString() {
        return String.format("%s, storageDomain=%s, args=%s", super.toString(), getStorageDomain(), getArgs());
    }


}
