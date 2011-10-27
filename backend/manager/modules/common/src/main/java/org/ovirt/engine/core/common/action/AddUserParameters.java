package org.ovirt.engine.core.common.action;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import org.ovirt.engine.core.common.businessentities.AdUser;
import org.ovirt.engine.core.common.businessentities.ad_groups;
import org.ovirt.engine.core.common.users.VdcUser;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "AddUserParameters")
public class AddUserParameters extends VdcActionParametersBase implements Serializable {
    private static final long serialVersionUID = 3345484510595493227L;

    @XmlElement(name = "VdcUser")
    private VdcUser vdcUser;

    @XmlElement(name = "AdGroup")
    private ad_groups adGroup;

    private AdUser adUser;

    public void setVdcUser(VdcUser vdcUser) {
        this.vdcUser = vdcUser;
    }

    public VdcUser getVdcUser() {
        return vdcUser;
    }

    public void setAdGroup(ad_groups adGroup) {
        this.adGroup = adGroup;
    }

    public ad_groups getAdGroup() {
        return adGroup;
    }

    public void setAdUser(AdUser adUser) {
        this.adUser = adUser;
    }

    public AdUser getAdUser() {
        return adUser;
    }

}
