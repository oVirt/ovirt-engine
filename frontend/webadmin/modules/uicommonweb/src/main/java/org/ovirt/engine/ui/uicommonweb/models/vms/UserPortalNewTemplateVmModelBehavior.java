package org.ovirt.engine.ui.uicommonweb.models.vms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.VmTemplateStatus;
import org.ovirt.engine.core.common.businessentities.comparators.NameableComparator;
import org.ovirt.engine.core.common.queries.GetEntitiesWithPermittedActionParameters;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.FrontendMultipleQueryAsyncResult;
import org.ovirt.engine.ui.uicompat.IFrontendMultipleQueryAsyncCallback;
import org.ovirt.engine.ui.uicompat.UIConstants;

public class UserPortalNewTemplateVmModelBehavior extends NewTemplateVmModelBehavior implements IFrontendMultipleQueryAsyncCallback {

    private static final ActionGroup CREATE_VM = ActionGroup.CREATE_VM;

    private final UIConstants constants = ConstantsManager.getInstance().getConstants();

    public UserPortalNewTemplateVmModelBehavior(VM vm) {
        super(vm);
    }

    public UserPortalNewTemplateVmModelBehavior() {
    }

    @Override
    protected void updateTemplate() {
        ArrayList<VdcQueryType> queryTypeList = new ArrayList<>();
        queryTypeList.add(VdcQueryType.GetVmTemplatesWithPermittedAction);

        GetEntitiesWithPermittedActionParameters getEntitiesWithPermittedActionParameters =
                new GetEntitiesWithPermittedActionParameters();
        getEntitiesWithPermittedActionParameters.setActionGroup(CREATE_VM);

        ArrayList<VdcQueryParametersBase> parametersList =
                new ArrayList<>(Arrays.asList(new VdcQueryParametersBase[]{getEntitiesWithPermittedActionParameters}));

        Frontend.getInstance().runMultipleQueries(queryTypeList, parametersList, this, getModel());
    }

    @Override
    public void executed(FrontendMultipleQueryAsyncResult result) {
        List<VdcQueryReturnValue> returnValueList = result.getReturnValues();
        ArrayList<VmTemplate> templates = returnValueList.get(0).getReturnValue();
        initTemplates(templates);
    }

    private void initTemplates(List<VmTemplate> templates) {
        List<VmTemplate> rootTemplates = keepBaseTemplates(templates);

        // Filter templates list (include only templates that belong to the selected datacenter)
        List<VmTemplate> templatesList = new ArrayList<>();
        DataCenterWithCluster dataCenterWithCluster = getModel().getDataCenterWithClustersList().getSelectedItem();
        StoragePool selectedDataCenter = dataCenterWithCluster.getDataCenter();
        Guid selectedDataCenterId = selectedDataCenter.getId();
        if (selectedDataCenterId == null) {
            return;
        }

        for (VmTemplate template : rootTemplates) {
            Guid datacenterId =
                    template.getStoragePoolId() == null ? Guid.Empty : template.getStoragePoolId();

            if (!template.isBlank()
                    && selectedDataCenterId.equals(datacenterId)
                    && template.getStatus() == VmTemplateStatus.OK) {
                templatesList.add(template);
            }
        }

        Collections.sort(templatesList, new NameableComparator());

        List<VmTemplate> filteredTemplates = AsyncDataProvider.getInstance().filterTemplatesByArchitecture(templatesList,
                dataCenterWithCluster.getCluster().getArchitecture());

        getModel().getIsSubTemplate().setEntity(false);
        if (filteredTemplates.isEmpty()) {
            // it is not allowed to create sub-templates of Blank template
            getModel().getIsSubTemplate().setIsChangeable(false,
                    constants.someNonDefaultTemplateHasToExistFirst());
            return;
        }
        getModel().getIsSubTemplate().setIsChangeable(true);

        VmTemplate currentTemplate = Linq.firstOrNull(templates,
                new Linq.IdPredicate<>(getVm().getVmtGuid()));

        getModel().getBaseTemplate().setItems(filteredTemplates);

        getModel().getBaseTemplate().setSelectedItem(Linq.firstOrNull(filteredTemplates,
                new Linq.IdPredicate<>(currentTemplate.getBaseTemplateId())));
    }
}
