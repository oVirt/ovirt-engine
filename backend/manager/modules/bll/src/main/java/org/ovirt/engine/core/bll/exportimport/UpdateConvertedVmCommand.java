package org.ovirt.engine.core.bll.exportimport;

import java.util.ArrayList;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.VmCommand;
import org.ovirt.engine.core.bll.VmHandler;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.storage.ovfstore.OvfHelper;
import org.ovirt.engine.core.bll.utils.VmDeviceUtils;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.ConvertVmParameters;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.errors.EngineException;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.common.vdscommands.VdsAndVmIDVDSParametersBase;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.DiskVmElementDao;
import org.ovirt.engine.core.utils.ovf.OvfReaderException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@NonTransactiveCommandAttribute
public class UpdateConvertedVmCommand<T extends ConvertVmParameters> extends VmCommand<T> {
    private static final Logger log = LoggerFactory.getLogger(ConvertVmCommand.class);

    @Inject
    private ImportUtils importUtils;
    @Inject
    private OvfHelper ovfHelper;
    @Inject
    private DiskVmElementDao diskVmElementDao;
    @Inject
    private VmDeviceUtils vmDeviceUtils;
    @Inject
    private VmHandler vmHandler;

    public UpdateConvertedVmCommand(Guid cmdId) {
        super(cmdId);
    }

    public UpdateConvertedVmCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected void init() {
        super.init();
        setVmName(getParameters().getVmName());
        setVdsId(getParameters().getProxyHostId());
        setClusterId(getParameters().getClusterId());
        setStoragePoolId(getParameters().getStoragePoolId());
        setStorageDomainId(getParameters().getStorageDomainId());
    }

    @Override
    protected void executeVmCommand() {
        try {
            VM vm = readVmFromOvf(getOvfOfConvertedVm());
            vm.setClusterId(getParameters().getClusterId());
            vm.setInterfaces(getParameters().getNetworkInterfaces());
            updateDiskVmElements(vm);
            addImportedDevices(vm);
            addExtraData(vm);
            setSucceeded(true);
        } finally {
            deleteV2VJob();
        }
    }

    private VM readVmFromOvf(String ovf) {
        try {
            return ovfHelper.readVmFromOvf(ovf).getVm();
        } catch (OvfReaderException e) {
            log.debug("failed to parse a given ovf configuration: \n " + ovf, e);
            auditLog(this, AuditLogType.IMPORTEXPORT_INVALID_OVF);
            throw new EngineException();
        }
    }

    private String getOvfOfConvertedVm() {
        VDSReturnValue retValue = runVdsCommand(
                VDSCommandType.GetConvertedOvf,
                new VdsAndVmIDVDSParametersBase(getVdsId(), getVmId()));
        if (!retValue.getSucceeded()) {
            auditLog(this, AuditLogType.IMPORTEXPORT_CANNOT_GET_OVF);
            throw new EngineException();
        }
        return (String) retValue.getReturnValue();
    }

    private void updateDiskVmElements(VM vm) {
        vm.getImages().stream().map(disk -> disk.getDiskVmElementForVm(vm.getId())).forEach(diskVmElementDao::update);
    }

    private void addImportedDevices(VM vm) {
        VmStatic vmStatic = vm.getStaticData();
        // Disk devices were already added
        vmStatic.setImages(new ArrayList<>());
        importUtils.updateGraphicsDevices(vmStatic, getStoragePool().getCompatibilityVersion());
        getVmDeviceUtils().addImportedDevices(vmStatic, false, false);
    }

    private void addExtraData(VM ovfVm) {
        VmStatic oldVm = getVm().getStaticData();
        VmStatic newVm = new VmStatic(oldVm);
        boolean forceVmStaticUpdate = false;

        if (newVm.getDefaultDisplayType() != ovfVm.getDefaultDisplayType()) {
            newVm.setDefaultDisplayType(ovfVm.getDefaultDisplayType());
            forceVmStaticUpdate = true;
        }

        if (newVm.getBiosType() != ovfVm.getBiosType()) {
            newVm.setBiosType(ovfVm.getBiosType());
        }

        vmHandler.updateToQ35(oldVm,
                newVm,
                getCluster(),
                null,
                forceVmStaticUpdate);
    }

    private void deleteV2VJob() {
        runVdsCommand(VDSCommandType.DeleteV2VJob,
                new VdsAndVmIDVDSParametersBase(getVdsId(), getVmId()));
    }
}
