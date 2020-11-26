package org.ovirt.engine.core.bll;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.action.SaveVmExternalDataParameters;
import org.ovirt.engine.core.common.businessentities.VmDeviceGeneralType;
import org.ovirt.engine.core.common.errors.EngineError;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VmDao;
import org.ovirt.engine.core.dao.VmDeviceDao;
import org.ovirt.engine.core.vdsbroker.vdsbroker.GetVmExternalDataVDSCommand;
import org.ovirt.engine.core.vdsbroker.vdsbroker.VmExternalDataReturn;


/**
 * Handling VM external data.
 * VM external data is VM data stored in a host local file system while the VM is running.
 * It must be retrieved and stored before the VM is destroyed and it must be restored on a host
 * before the VM is started again. Examples of VM external data are TPM data or secure boot data.
 */
public class SaveVmExternalDataCommand<T extends SaveVmExternalDataParameters> extends VmCommand<T> {

    @Inject
    private VmDao vmDao;
    @Inject
    private VmDeviceDao vmDeviceDao;

    private static final int MAX_DATA_RETRIEVAL_ATTEMPTS = 3;

    public SaveVmExternalDataCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    private boolean hasExternalData() {
        return !vmDeviceDao.getVmDeviceByVmIdAndType(getParameters().getVmId(), VmDeviceGeneralType.TPM).isEmpty();
    }

    /**
     * Retrieve VM external data, such as TPM data, from Vdsm and save it to the database.
     *
     * @param incrementMethod a method to be used to count failed data retrieval updates
     * @param forceUpdate whether to force data update in Vdsm before retrieval; this should be used
     *        if and only if the VM is down
     * @return false if the data couldn't be retrieved and it makes sense to retrieve the attempt later,
     *         true if the data was retrieved or if the data retreival was given up and is not going to be
     *         attempted anymore (e.g. because the host doesn't provide the required API or the VM is no
     *         longer accessible or there were too many failed attempts)
     */

    @Override
    protected void executeCommand() {
        if (!hasExternalData()) {
            setSucceeded(true);
            return;
        }

        Guid vmId = getVmId();
        Guid vdsId = getVm().getRunOnVds();
        VDSReturnValue returnValue = runVdsCommand(
                VDSCommandType.GetVmExternalData,
                new GetVmExternalDataVDSCommand.Parameters(vdsId, vmId, getParameters().getForceUpdate()));
        if (!returnValue.getSucceeded()) {
            if (returnValue.getVdsError().getCode() == EngineError.METHOD_NOT_FOUND
                    || returnValue.getVdsError().getCode() == EngineError.noVM) {
                log.error("VM external data not updated: {}; retrieval API not supported on the host: {}", vmId, vdsId);
                setSucceeded(true);
            } else if (getParameters().getIncrementMethod().get() < MAX_DATA_RETRIEVAL_ATTEMPTS) {
                log.info("Failed to retrieve VM external data: {}", vmId);
                setSucceeded(false);
            } else {
                log.error("Failed to retrieve VM external data repeatedly, giving up: {}", vmId);
                setSucceeded(true);
            }
            return;
        }
        String data = ((VmExternalDataReturn)returnValue.getReturnValue()).data;
        if (data != null) {
            vmDao.updateTpmData(vmId, data);
        }
        setSucceeded(true);
    }
}
