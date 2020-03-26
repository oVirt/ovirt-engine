package org.ovirt.engine.ui.uicommonweb.models.storage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.ImportVmTemplateFromConfParameters;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.Quota;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.uicommonweb.models.vms.ImportTemplateData;

public class RegisterTemplateModel extends RegisterEntityModel<VmTemplate, ImportTemplateData> {

    public RegisterTemplateModel() {
    }

    @Override
    public void initialize() {
        addVnicProfileMappingCommand();
        super.initialize();
    }

    protected void onSave() {
        List<ActionParametersBase> parameters = prepareActionParameters();
        ActionType actionType = ActionType.ImportVmTemplateFromConfiguration;
        onSave(actionType, parameters);
    }

    private List<ActionParametersBase> prepareActionParameters() {
        ArrayList<ActionParametersBase> parameters = new ArrayList<>();
        for (ImportTemplateData entityData : getEntities().getItems()) {
            VmTemplate vmTemplate = entityData.getEntity();
            Cluster cluster = entityData.getCluster().getSelectedItem();

            ImportVmTemplateFromConfParameters params = new ImportVmTemplateFromConfParameters();
            params.setExternalVnicProfileMappings(cloneExternalVnicProfiles(cluster));
            params.setContainerId(vmTemplate.getId());
            params.setStorageDomainId(getStorageDomainId());
            params.setImagesExistOnTargetStorageDomain(true);
            params.setClusterId(cluster != null ? cluster.getId() : null);
            params.setStoragePoolId(cluster != null ? cluster.getStoragePoolId() : null);

            if (isQuotaEnabled()) {
                Quota quota = entityData.getClusterQuota().getSelectedItem();
                params.setQuotaId(quota != null ? quota.getId() : null);
                params.setDiskTemplateMap(vmTemplate.getDiskTemplateMap());
                updateDiskQuotas(new ArrayList<Disk>(params.getDiskTemplateMap().values()));
            }

            parameters.add(params);
        }
        return parameters;
    }

    protected List<VmNetworkInterface> getInterfaces(ImportTemplateData importEntityData) {
        return importEntityData.getEntity().getInterfaces();
    }

    protected String createSearchPattern(Collection<ImportTemplateData> entities) {
        String vmt_guidKey = "_VMT_ID = "; //$NON-NLS-1$
        String orKey = " or "; //$NON-NLS-1$
        String prefix = "Template: "; //$NON-NLS-1$

        StringJoiner sj = new StringJoiner(orKey, prefix, "");
        entities.stream().map(ImportTemplateData::getTemplate).forEach(template -> {
            sj.add(vmt_guidKey + template.getId().toString());
            sj.add(vmt_guidKey + template.getBaseTemplateId().toString());
        });

        return sj.toString();
    }

    protected SearchType getSearchType() {
        return SearchType.VmTemplate;
    }

    @Override
    protected void updateExistingEntities(List<VmTemplate> templates, Guid storagePoolId) {
        Set<String> existingNames = templates
                .stream()
                .filter(template -> template.getStoragePoolId().equals(storagePoolId))
                .map(VmTemplate::getName)
                .collect(Collectors.toSet());

        for (ImportTemplateData vmTemplateData : getEntities().getItems()) {
            if (templates.contains(vmTemplateData.getTemplate())) {
                vmTemplateData.setExistsInSystem(true);
            }
            vmTemplateData.setNameExistsInSystem(existingNames.contains(vmTemplateData.getTemplate().getName()));
        }
    }
}
