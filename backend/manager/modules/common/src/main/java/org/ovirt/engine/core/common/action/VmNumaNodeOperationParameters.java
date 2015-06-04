package org.ovirt.engine.core.common.action;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.MigrationSupport;
import org.ovirt.engine.core.common.businessentities.NumaTuneMode;
import org.ovirt.engine.core.common.businessentities.VmNumaNode;
import org.ovirt.engine.core.compat.Guid;

public class VmNumaNodeOperationParameters extends VmOperationParameterBase {

    private static final long serialVersionUID = -1955959985341097257L;

    private List<VmNumaNode> vmNumaNodeList;

    private NumaTuneMode numaTuneMode;
    private List<Guid> dedicatedHostList;
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

    public List<Guid> getDedicatedHostList() {
        if (dedicatedHostList == null){
            dedicatedHostList = new LinkedList<Guid>();
        }
        return dedicatedHostList;
    }

    public void setDedicatedHostList(List<Guid> dedicatedHosts) {
        if (dedicatedHosts == null){
            this.dedicatedHostList = Collections.<Guid>emptyList();
            return;
        }
        this.dedicatedHostList = dedicatedHosts;
    }

    public MigrationSupport getMigrationSupport() {
        return migrationSupport;
    }

    public void setMigrationSupport(MigrationSupport migrationSupport) {
        this.migrationSupport = migrationSupport;
    }

}
