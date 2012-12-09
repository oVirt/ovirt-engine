package org.ovirt.engine.core.vdsbroker.irsbroker;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.common.errors.VdcBllErrors;
import org.ovirt.engine.core.common.vdscommands.UpdateVMVDSCommandParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.KeyValuePairCompat;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.core.vdsbroker.vdsbroker.VDSExceptionBase;

public class UpdateVMVDSCommand<P extends UpdateVMVDSCommandParameters> extends IrsBrokerCommand<P> {
    public UpdateVMVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void ExecuteIrsBrokerCommand() {
        Map[] vms = BuildVmsStuctListFromParameters();
        if (getParameters().getStorageDomainId().equals(Guid.Empty)) {
            status = getIrsProxy().updateVM(getParameters().getStoragePoolId().toString(), vms);
        } else {
            status = getIrsProxy().updateVMInImportExport(getParameters().getStoragePoolId().toString(), vms,
                    getParameters().getStorageDomainId().toString());
        }
        ProceedProxyReturnValue();
    }

    private Map[] BuildVmsStuctListFromParameters() {
        // ("vm":vmGUID, "ovf":metaOVF, "imglist":imgList)
        Map[] result = new Map[getParameters().getInfoDictionary().entrySet().size()];
        int counter = 0;
        for (Map.Entry<Guid, KeyValuePairCompat<String, List<Guid>>> data : getParameters().getInfoDictionary()
                .entrySet()) {
            Map vmToSend = new HashMap();
            vmToSend.put("vm", data.getKey().toString());
            vmToSend.put("ovf", data.getValue().getKey());
            java.util.List<Guid> imagesGuidList = data.getValue().getValue();
            String[] imageList = new String[imagesGuidList.size()];
            for (int i = 0; i < imagesGuidList.size(); i++) {
                imageList[i] = imagesGuidList.get(i).toString();
            }

            vmToSend.put("imglist", StringHelper.join(",", imageList));
            result[counter] = vmToSend;
            counter++;
        }
        return result;
    }

    @Override
    protected void ProceedProxyReturnValue() {
        VdcBllErrors returnStatus = GetReturnValueFromStatus(getReturnStatus());
        switch (returnStatus) {
        case Done:
            return;
        default:
            VDSExceptionBase outEx = createDefaultConcreteException(getReturnStatus().mMessage);
            InitializeVdsError(returnStatus);
            outEx.setVdsError(getVDSReturnValue().getVdsError());
            throw outEx;
        }
    }

    @Override
    protected VDSExceptionBase createDefaultConcreteException(String errorMessage) {
        return new IrsOperationFailedNoFailoverException(errorMessage);
    }
}
