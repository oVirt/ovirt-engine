package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.queries.GetVmTemplateParameters;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;


public class GetVmDataByPoolIdQuery<P extends IdQueryParameters> extends QueriesCommandBase<P> {

    public GetVmDataByPoolIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        VM vm = DbFacade.getInstance()
                .getVmPoolDao()
                .getVmDataFromPoolByPoolGuid(getParameters().getId(), getUserID(), getParameters().isFiltered());

        if (vm != null) {
            boolean isLatestLoad = vm.isUseLatestVersion();
            boolean loadTemplateData = false;
            Guid vmtGuid = vm.getVmtGuid();
            if (vm.isNextRunConfigurationExists()) {
                VdcQueryReturnValue nextRunRet = getBackend().runInternalQuery(VdcQueryType.GetVmNextRunConfiguration, new IdQueryParameters(vm.getId()));
                if (nextRunRet != null) {
                    VM nextRunVm = nextRunRet.getReturnValue();
                    if (nextRunVm != null) { // template version was changed -> load data from template
                        isLatestLoad = nextRunVm.isUseLatestVersion();
                        vmtGuid = nextRunVm.getVmtGuid();
                        loadTemplateData = true;
                    }
                }
            }

            VmTemplate templateData = null;
            if (isLatestLoad) {
                VdcQueryReturnValue latestRet = getBackend().runInternalQuery(VdcQueryType.GetLatestTemplateInChain, new IdQueryParameters(vmtGuid));
                if (latestRet != null) {
                    templateData = latestRet.getReturnValue();
                }
            } else if (loadTemplateData) {
                VdcQueryReturnValue templateRet = getBackend().runInternalQuery(VdcQueryType.GetVmTemplate, new GetVmTemplateParameters(vmtGuid));
                if (templateRet != null) {
                    templateData = templateRet.getReturnValue();
                }
            }

            if (templateData != null) {
                VmStatic temp = vm.getStaticData();
                temp.setVmtGuid(vmtGuid);
                temp.setUseLatestVersion(isLatestLoad);
                VmHandler.copyData(templateData, temp);
                vm.setStaticData(temp);
            }

            VmHandler.updateDisksFromDb(vm);
            VmHandler.updateVmInitFromDB(vm.getStaticData(), true);
        }

        getQueryReturnValue().setReturnValue(vm);


    }
}
