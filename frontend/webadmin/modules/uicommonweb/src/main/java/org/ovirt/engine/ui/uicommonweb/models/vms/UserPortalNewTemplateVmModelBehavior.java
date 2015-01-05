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
import org.ovirt.engine.ui.uicompat.FrontendMultipleQueryAsyncResult;
import org.ovirt.engine.ui.uicompat.IFrontendMultipleQueryAsyncCallback;

public class UserPortalNewTemplateVmModelBehavior extends NewTemplateVmModelBehavior implements IFrontendMultipleQueryAsyncCallback {

    private static final ActionGroup CREATE_VM = ActionGroup.CREATE_VM;

    public UserPortalNewTemplateVmModelBehavior(VM vm) {
        super(vm);
    }

    @Override
    protected void updateTemplate() {
        ArrayList<VdcQueryType> queryTypeList = new ArrayList<VdcQueryType>();
        queryTypeList.add(VdcQueryType.GetVmTemplatesWithPermittedAction);

        GetEntitiesWithPermittedActionParameters getEntitiesWithPermittedActionParameters =
                new GetEntitiesWithPermittedActionParameters();
        getEntitiesWithPermittedActionParameters.setActionGroup(CREATE_VM);

        ArrayList<VdcQueryParametersBase> parametersList =
                new ArrayList<VdcQueryParametersBase>(Arrays.asList(new VdcQueryParametersBase[] {
                        getEntitiesWithPermittedActionParameters }));

        Frontend.getInstance().runMultipleQueries(queryTypeList, parametersList, this, getModel());
    }

    @Override
    public void executed(FrontendMultipleQueryAsyncResult result) {
        List<VdcQueryReturnValue> returnValueList = result.getReturnValues();
        ArrayList<VmTemplate> templates =
                (ArrayList<VmTemplate>) returnValueList.get(0).getReturnValue();
        initTemplates(templates);
    }

    private void initTemplates(List<VmTemplate> templates) {
        List<VmTemplate> rootTemplates = keepBaseTemplates(templates);

        // Filter templates list (include only templates that belong to the selected datacenter)
        List<VmTemplate> templatesList = new ArrayList<>();
        VmTemplate blankTemplate = null;
        DataCenterWithCluster dataCenterWithCluster = getModel().getDataCenterWithClustersList().getSelectedItem();
        StoragePool selectedDataCenter = dataCenterWithCluster.getDataCenter();
        Guid selectedDataCenterId = selectedDataCenter.getId();
        if (selectedDataCenterId == null) {
            return;
        }

        for (VmTemplate template : rootTemplates) {
            Guid datacenterId =
                    template.getStoragePoolId() == null ? Guid.Empty : template.getStoragePoolId();

            if (template.getId().equals(Guid.Empty)) {
                blankTemplate = template;
            } else if (!selectedDataCenterId.equals(datacenterId)) {
                continue;
            } else if (template.getStatus() == VmTemplateStatus.OK) {
                templatesList.add(template);
            }
        }

        // Sort list and position "Blank" template as first
        Collections.sort(templatesList, new NameableComparator());
        if (blankTemplate != null && rootTemplates.contains(blankTemplate)) {
            templatesList.add(0, blankTemplate);
        }

        List<VmTemplate> filteredTemplates = AsyncDataProvider.getInstance().filterTemplatesByArchitecture(templatesList,
                dataCenterWithCluster.getCluster().getArchitecture());

        VmTemplate currentTemplate = Linq.firstOrDefault(templates,
                new Linq.TemplatePredicate(getVm().getVmtGuid()));

        getModel().getBaseTemplate().setItems(filteredTemplates);

        getModel().getBaseTemplate().setSelectedItem(Linq.firstOrDefault(filteredTemplates,
                new Linq.TemplatePredicate(currentTemplate.getBaseTemplateId())));
    }
}
