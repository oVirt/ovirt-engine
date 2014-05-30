package org.ovirt.engine.core.common.action;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.MigrationSupport;
import org.ovirt.engine.core.common.businessentities.NumaTuneMode;
import org.ovirt.engine.core.common.businessentities.VmNumaNode;
import org.ovirt.engine.core.compat.Guid;

public class VmNumaNodeOperationParameters extends VmOperationParameterBase {

    private static final long serialVersionUID = -1955959985341097257L;

    private List<VmNumaNode> vmNumaNodeList;

    private NumaTuneMode numaTuneMode;
    private Guid dedicatedHost;
    private MigrationSupport migrationSupport;

    public VmNumaNodeOperationParameters() {
    }

    public VmNumaNodeOperationParameters(Guid vmId, VmNumaNode vmNumaNode) {
        super(vmId);
        vmNumaNodeList = new ArrayList<VmNumaNode>();
        vmNumaNodeList.add(vmNumaNode);
    }

    public VmNumaNodeOperationParameters(Guid vmId, List<VmNumaNode> vmNumaNodes) {
        super(vmId);
        vmNumaNodeList = vmNumaNodes;
    }

    public List<VmNumaNode> getVmNumaNodeList() {
        return vmNumaNodeList;
    }

    public NumaTuneMode getNumaTuneMode() {
        return numaTuneMode;
    }

    public void setNumaTuneMode(NumaTuneMode numaTuneMode) {
        this.numaTuneMode = numaTuneMode;
    }

    public Guid getDedicatedHost() {
        return dedicatedHost;
    }

    public void setDedicatedHost(Guid dedicatedHost) {
        this.dedicatedHost = dedicatedHost;
    }

    public MigrationSupport getMigrationSupport() {
        return migrationSupport;
    }

    public void setMigrationSupport(MigrationSupport migrationSupport) {
        this.migrationSupport = migrationSupport;
    }

}
