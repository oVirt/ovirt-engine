package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.bll.job.ExecutionHandler;
import org.ovirt.engine.core.common.action.AddVmAndAttachToPoolParameters;
import org.ovirt.engine.core.common.action.AddVmFromScratchParameters;
import org.ovirt.engine.core.common.action.AddVmToPoolParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.action.VmManagementParametersBase;
import org.ovirt.engine.core.common.businessentities.VmStatic;

@InternalCommandAttribute
public class AddVmAndAttachToPoolCommand<T extends AddVmAndAttachToPoolParameters> extends AddVmCommand<T> {
    public AddVmAndAttachToPoolCommand(T parameters) {
        super(parameters);
    }

    /**
     * This operation may take much time.
     */
    @Override
    protected void executeCommand() {
        VmStatic vmStatic = getParameters().getVmStaticData();
        boolean vmAddedSuccessfully = false;
        VdcReturnValueBase returnValueFromAddVm = null;
        if (VmTemplateHandler.BlankVmTemplateId.equals(vmStatic.getvmt_guid())) {
            // Vm from scratch
            AddVmFromScratchParameters tempVar = new AddVmFromScratchParameters(vmStatic, getParameters()
                    .getDiskInfoList(), getParameters().getStorageDomainId());
            tempVar.setSessionId(getParameters().getSessionId());
            tempVar.setDontAttachToDefaultTag(true);
            returnValueFromAddVm =
                    Backend.getInstance().runInternalAction(VdcActionType.AddVmFromScratch,
                            tempVar,
                            ExecutionHandler.createDefaultContexForTasks(getExecutionContext()));
        } else {
            VmManagementParametersBase tempVar2 = new VmManagementParametersBase(vmStatic);
            tempVar2.setSessionId(getParameters().getSessionId());
            tempVar2.setDontCheckTemplateImages(true);
            tempVar2.setDontAttachToDefaultTag(true);
            tempVar2.setImageToDestinationDomainMap(getParameters().getImageToDestinationDomainMap());
            returnValueFromAddVm =
                    Backend.getInstance().runInternalAction(VdcActionType.AddVm,
                            tempVar2,
                            ExecutionHandler.createDefaultContexForTasks(getExecutionContext()));
        }
        vmAddedSuccessfully = returnValueFromAddVm.getSucceeded();

        if (vmAddedSuccessfully) {
            getTaskIdList().addAll(returnValueFromAddVm.getInternalTaskIdList());
            AddVmToPoolParameters tempVar3 = new AddVmToPoolParameters(getParameters().getPoolId(),
                    vmStatic.getId());
            tempVar3.setShouldBeLogged(false);
            setSucceeded(Backend.getInstance().runInternalAction(VdcActionType.AddVmToPool, tempVar3).getSucceeded());
            addVmPermission();
        }
    }
}
