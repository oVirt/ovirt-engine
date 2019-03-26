package org.ovirt.engine.core.common.queries;

import java.util.Collections;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.compat.Guid;

public class GetValidHostsForVmsParameters extends QueryParametersBase {
    private static final long serialVersionUID = -8417122714295216837L;

    private List<VM> vms;

    public List<VM> getVms() {
        return this.vms;
    }

    private void setVms(List<VM> vms) {
        this.vms = vms;
    }

    private Guid clusterId;

    public Guid getClusterId() {
        return clusterId;
    }

    public void setClusterId(Guid clusterId) {
        this.clusterId = clusterId;
    }

    public boolean checkVmsInAffinityClosure = false;

    public boolean isCheckVmsInAffinityClosure() {
        return checkVmsInAffinityClosure;
    }

    public void setCheckVmsInAffinityClosure(boolean checkVmsInAffinityClosure) {
        this.checkVmsInAffinityClosure = checkVmsInAffinityClosure;
    }

    public GetValidHostsForVmsParameters(List<VM> vms, Guid clusterId) {
        this.setVms(vms);
        this.setClusterId(clusterId);
    }

    public GetValidHostsForVmsParameters(List<VM> vms) {
        this (vms, Guid.Empty);
    }

    public GetValidHostsForVmsParameters() {
        this (Collections.emptyList(), Guid.Empty);
    }
}
