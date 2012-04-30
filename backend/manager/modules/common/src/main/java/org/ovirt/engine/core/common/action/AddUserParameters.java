package org.ovirt.engine.core.common.action;

import java.io.Serializable;

import org.ovirt.engine.core.common.businessentities.AdUser;
import org.ovirt.engine.core.common.businessentities.ad_groups;
import org.ovirt.engine.core.common.users.VdcUser;

public class AddUserParameters extends VdcActionParametersBase implements Serializable {
    private static final long serialVersionUID = 3345484510595493227L;

    private VdcUser vdcUser;

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
