package org.ovirt.engine.core.bll.storage.domain;

import java.util.ArrayList;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.validator.storage.StorageDomainValidator;
import org.ovirt.engine.core.common.action.AddSANStorageDomainParameters;
import org.ovirt.engine.core.common.businessentities.storage.LUNs;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.vdscommands.CreateVGVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.GetDeviceListVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.GetVGInfoVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.StorageDomainStaticDao;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

public class AddSANStorageDomainCommand<T extends AddSANStorageDomainParameters> extends AddStorageDomainCommand<T> {

    @Inject
    private BlockStorageDomainHelper blockStorageDomainHelper;
    @Inject
    private StorageDomainStaticDao storageDomainStaticDao;

    /**
     * Constructor for command creation when compensation is applied on startup
     */
    public AddSANStorageDomainCommand(Guid commandId) {
        super(commandId);
    }

    public AddSANStorageDomainCommand(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
    }

    @Override
    protected void executeCommand() {
        initializeStorageDomain();
        // save storage if got from parameters in order to save first empty
        // storage in db and use it later
        String storage = (getStorageDomain().getStorage() != null) ? getStorageDomain().getStorage() : "";
        // set domain storage to empty because not nullable in db and for shared
        // status to be locked
        getStorageDomain().setStorage("");
        addStorageDomainInDb();
        if(StringUtils.isEmpty(storage)) {
              storage = createVG();
        }
        getStorageDomain().setStorage(storage);
        if (StringUtils.isNotEmpty(getStorageDomain().getStorage()) && addStorageDomainInIrs()) {
            updateStorageDomainFromIrs();
            proceedVGLunsInDb();
            blockStorageDomainHelper.fillMetadataDevicesInfo(getStorageDomain().getStorageStaticData(),
                    getVds().getId());
            storageDomainStaticDao.update(getStorageDomain().getStorageStaticData());
            setSucceeded(true);
        }
    }

    protected void proceedVGLunsInDb() {
        final ArrayList<LUNs> luns = (ArrayList<LUNs>) runVdsCommand(VDSCommandType.GetVGInfo,
                        new GetVGInfoVDSCommandParameters(getVds().getId(), getStorageDomain().getStorage()))
                .getReturnValue();

        TransactionSupport.executeInNewTransaction(() -> {
            for (LUNs lun : luns) {
                lunHelper.proceedLUNInDb(lun, getStorageDomain().getStorageType(), getStorageDomain().getStorage());
            }
            return null;
        });

    }

    private String createVG() {
        VDSReturnValue returnValue = runVdsCommand(
                VDSCommandType.CreateVG,
                new CreateVGVDSCommandParameters(getVds().getId(), getStorageDomain().getId(),
                        getParameters().getLunIds(), getParameters().isForce()));
        return (String) ((returnValue.getReturnValue() instanceof String) ? returnValue
                .getReturnValue() : null);
    }

    @Override
    protected boolean canAddDomain() {
        if ((getParameters().getLunIds() == null || getParameters().getLunIds().isEmpty()) && StringUtils
                .isEmpty(getStorageDomain().getStorage())) {
            return failValidation(EngineMessage.ERROR_CANNOT_CREATE_STORAGE_DOMAIN_WITHOUT_VG_LV);
        }
        if (isLunsAlreadyInUse(getParameters().getLunIds())) {
            return false;
        }
        return true;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected boolean validateDiscardAfterDeleteLegal(StorageDomainValidator storageDomainValidator) {
        if (!getStorageDomain().getDiscardAfterDelete()) {
            return true;
        }
        ArrayList<LUNs> luns = (ArrayList<LUNs>) runVdsCommand(VDSCommandType.GetDeviceList,
                new GetDeviceListVDSCommandParameters(getVds().getId(), getStorageDomain().getStorageType(), false,
                        getParameters().getLunIds())).getReturnValue();
        return validate(storageDomainValidator.isDiscardAfterDeleteLegalForNewBlockStorageDomain(luns));
    }
}
