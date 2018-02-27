package org.ovirt.engine.core.common.vdscommands;

import java.util.Date;
import java.util.Map;

import org.ovirt.engine.core.common.action.SysPrepParams;
import org.ovirt.engine.core.common.businessentities.InitializationType;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmPayload;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.utils.ToStringBuilder;
import org.ovirt.engine.core.compat.Guid;

public class CreateVDSCommandParameters extends VdsAndVmIDVDSParametersBase {

    private VM vm;
    private SysPrepParams sysPrepParams;
    private boolean runInUnknownStatus;
    private InitializationType initializationType;
    private VmPayload vmPayload;
    private String hibernationVolHandle;
    private Map<Guid, String> passthroughVnicToVfMap;
    private boolean volatileRun;
    private Date downSince;
    private DiskImage memoryDumpImage;
    private DiskImage memoryConfImage;

    public CreateVDSCommandParameters() {
    }

    public CreateVDSCommandParameters(Guid vdsId, VM vm) {
        super(vdsId, vm.getId());
        this.vm = vm;
        initializationType = InitializationType.None;
    }

    public VM getVm() {
        return vm;
    }

    public SysPrepParams getSysPrepParams() {
        return sysPrepParams;
    }

    public void setSysPrepParams(SysPrepParams sysPrepParams) {
        this.sysPrepParams = sysPrepParams;
    }

    public boolean isRunInUnknownStatus() {
        return runInUnknownStatus;
    }

    public void setRunInUnknownStatus(boolean runUnknown) {
        this.runInUnknownStatus = runUnknown;
    }

    public InitializationType getInitializationType() {
        return initializationType;
    }

    public void setInitializationType(InitializationType value) {
        initializationType = value;
    }

    public VmPayload getVmPayload() {
        return vmPayload;
    }

    public void setVmPayload(VmPayload vmPayload) {
        this.vmPayload = vmPayload;
    }

    public String getHibernationVolHandle() {
        return hibernationVolHandle;
    }

    public void setHibernationVolHandle(String value) {
        this.hibernationVolHandle = value;
    }

    public DiskImage getMemoryDumpImage() {
        return memoryDumpImage;
    }

    public void setMemoryDumpImage(DiskImage memoryDumpImage) {
        this.memoryDumpImage = memoryDumpImage;
    }

    public DiskImage getMemoryConfImage() {
        return memoryConfImage;
    }

    public void setMemoryConfImage(DiskImage memoryConfImage) {
        this.memoryConfImage = memoryConfImage;
    }

    public Map<Guid, String> getPassthroughVnicToVfMap() {
        return passthroughVnicToVfMap;
    }

    public void setPassthroughVnicToVfMap(Map<Guid, String> passthroughVnicToVfMap) {
        this.passthroughVnicToVfMap = passthroughVnicToVfMap;
    }

    public boolean isVolatileRun() {
        return volatileRun;
    }

    public void setVolatileRun(boolean volatileRun) {
        this.volatileRun = volatileRun;
    }

    public Date getDownSince() {
        return downSince;
    }

    public void setDownSince(Date downSince) {
        this.downSince = downSince;
    }

    @Override
    protected ToStringBuilder appendAttributes(ToStringBuilder tsb) {
        return super.appendAttributes(tsb)
                .append("vm", getVm());
    }
}
