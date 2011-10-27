package org.ovirt.engine.core.common.action;

import javax.validation.Valid;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VdsStatic;
import org.ovirt.engine.core.compat.StringHelper;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "VdsOperationActionParameters")
public class VdsOperationActionParameters extends VdsActionParameters {
    private static final long serialVersionUID = 4156122527623908516L;

    @Valid
    private VdsStatic _vdsStatic;

    @XmlElement(name = "RootPassword")
    private String _rootPassword;

    @XmlElement(name = "OverrideFirewall")
    private boolean overrideFirewall;

    public VdsOperationActionParameters(VdsStatic vdsStatic, String rootPassword) {
        super(vdsStatic.getId());
        if (StringHelper.EqOp(vdsStatic.getManagmentIp(), "")) {
            vdsStatic.setManagmentIp(null);
        }
        _vdsStatic = vdsStatic;
        _rootPassword = rootPassword;
    }

    public VdsOperationActionParameters(VdsStatic vdsStatic) {
        this(vdsStatic, null);
    }

    public VdsStatic getVdsStaticData() {
        return _vdsStatic;
    }

    public String getRootPassword() {
        return _rootPassword;
    }

    public void setRootPassword(String value) {
        _rootPassword = value;
    }

    public VdsOperationActionParameters() {
    }

    @XmlElement(name = "vds")
    public VDS getvds() {
        VDS vds = new VDS();
        vds.setStaticData(_vdsStatic);
        return vds;
    }

    public void setvds(VDS value) {
        _vdsStatic = value.getStaticData();
    }

    public void setOverrideFirewall(boolean overrideFirewall) {
        this.overrideFirewall = overrideFirewall;
    }

    public boolean getOverrideFirewall() {
        return overrideFirewall;
    }
}
