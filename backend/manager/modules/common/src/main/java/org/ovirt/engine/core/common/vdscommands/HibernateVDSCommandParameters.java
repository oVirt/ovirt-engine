package org.ovirt.engine.core.common.vdscommands;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

import org.ovirt.engine.core.compat.Guid;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "HibernateVDSCommandParameters")
public class HibernateVDSCommandParameters extends VdsAndVmIDVDSParametersBase {
    public HibernateVDSCommandParameters(Guid vdsId, Guid vmId, String hibernationVolHandle) {
        super(vdsId, vmId);
        setHibernationVolHandle(hibernationVolHandle);
    }

    private String privateHibernationVolHandle;

    public String getHibernationVolHandle() {
        return privateHibernationVolHandle;
    }

    private void setHibernationVolHandle(String value) {
        privateHibernationVolHandle = value;
    }

    public HibernateVDSCommandParameters() {
    }

    @Override
    public String toString() {
        return String.format("%s, hibernationVolHandle=%s", super.toString(), getHibernationVolHandle());
    }
}
