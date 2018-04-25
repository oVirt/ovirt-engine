package org.ovirt.engine.core.common.queries;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.compat.Guid;

public class GetValidHostsForVmsParameters extends QueryParametersBase {
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

    private List<Guid> blackList;

    public List<Guid> getBlackList() {
        return blackList;
    }

    public void setBlackList(List<Guid> blackList) {
        this.blackList = blackList;
    }

    private List<Guid> whiteList;

    public List<Guid> getWhiteList() {
        return whiteList;
    }

    public void setWhiteList(List<Guid> whiteList) {
        this.whiteList = whiteList;
    }

    private List<String> messages;

    public List<String> getMessages() {
        return messages;
    }

    public void setMessages(List<String> messages) {
        this.messages = messages;
    }

    public GetValidHostsForVmsParameters(List<VM> vms,
                                         Guid clusterId,
                                         List<Guid> blackList,
                                         List<Guid> whiteList,
                                         List<String> messages) {
        this.setVms(vms);
        this.setClusterId(clusterId);
        this.setBlackList(blackList);
        this.setWhiteList(whiteList);
        this.setMessages(messages);
    }

    public GetValidHostsForVmsParameters(List<VM> vms) {
        this (vms, Guid.Empty, new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
    }

    public GetValidHostsForVmsParameters(List<VM> vms, Guid clusterId) {
        this (vms, clusterId, new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
    }

    public GetValidHostsForVmsParameters() {
        this (new ArrayList<>(), Guid.Empty, new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
    }
}
