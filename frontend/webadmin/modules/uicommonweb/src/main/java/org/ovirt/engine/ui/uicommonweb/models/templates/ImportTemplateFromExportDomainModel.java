package org.ovirt.engine.ui.uicommonweb.models.templates;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.ImportVmTemplateParameters;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.profiles.CpuProfile;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.queries.QueryReturnValue;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.common.queries.SearchParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.frontend.AsyncCallback;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.HasEntity;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.uicommonweb.models.clusters.ClusterListModel;
import org.ovirt.engine.ui.uicommonweb.models.quota.QuotaListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.ImportTemplateData;
import org.ovirt.engine.ui.uicommonweb.models.vms.ImportVmFromExportDomainModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmImportAppListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmImportDiskListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmImportInterfaceListModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.IFrontendMultipleActionAsyncCallback;
import org.ovirt.engine.ui.uicompat.UIConstants;

import com.google.inject.Inject;

public class ImportTemplateFromExportDomainModel extends ImportVmFromExportDomainModel {

    private final TemplateImportDiskListModel templateImportDiskListModel;
    protected Map<Guid, Object> cloneObjectMap;

    @Inject
    public ImportTemplateFromExportDomainModel(final VmImportDiskListModel vmImportDiskListModel,
            final ClusterListModel<Void> cluster, final QuotaListModel clusterQuota,
            final TemplateImportGeneralModel templateImportGeneralModel, final VmImportInterfaceListModel vmImportInterfaceListModel,
            final VmImportAppListModel vmImportAppListModel, final TemplateImportDiskListModel templateImportDiskListModel,
            final TemplateImportInterfaceListModel templateImportInterfaceListModel) {
        super(vmImportDiskListModel, cluster, clusterQuota, null, vmImportInterfaceListModel,
                vmImportAppListModel);
        this.templateImportDiskListModel = templateImportDiskListModel;
        setDetailList(templateImportGeneralModel, templateImportInterfaceListModel);
    }

    private void setDetailList(final TemplateImportGeneralModel templateImportGeneralModel,
            final TemplateImportInterfaceListModel templateImportInterfaceListModel) {
        List<HasEntity> list = new ArrayList<>();
        list.add(templateImportGeneralModel);
        list.add(templateImportInterfaceListModel);
        list.add(templateImportDiskListModel);
        setDetailModels(list);
    }

    protected String createSearchPattern(Collection<VmTemplate> templates) {
        String vmt_guidKey = "_VMT_ID = "; //$NON-NLS-1$
        String orKey = " or "; //$NON-NLS-1$
        String prefix = "Template: "; //$NON-NLS-1$

        StringJoiner sj = new StringJoiner(orKey, prefix, "");

        templates.forEach(template -> {
            sj.add(vmt_guidKey + template.getId().toString());
            sj.add(vmt_guidKey + template.getBaseTemplateId().toString());
        });

        return sj.toString();
    }

    public void init(final Collection<VmTemplate> externalTemplates, final Guid storageDomainId) {
        Frontend.getInstance().runQuery(QueryType.Search,
                new SearchParameters(createSearchPattern(externalTemplates), SearchType.VmTemplate),
                new AsyncQuery<>(new AsyncCallback<QueryReturnValue>() {

                    @Override
                    public void onSuccess(QueryReturnValue returnValue) {
                        UIConstants constants = ConstantsManager.getInstance().getConstants();
                        List<VmTemplate> vmtList = returnValue.getReturnValue();

                        List<ImportTemplateData> templateDataList = new ArrayList<>();
                        for (VmTemplate template : externalTemplates) {
                            ImportTemplateData templateData = new ImportTemplateData(template);
                            boolean templateExistsInSystem = vmtList.contains(template);
                            templateData.setExistsInSystem(templateExistsInSystem);
                            if (templateExistsInSystem) {
                                templateData.enforceClone(constants.importTemplateThatExistsInSystemMustClone());
                            } else if (!template.isBaseTemplate() && findAnyVmTemplateById(vmtList, template.getBaseTemplateId()) == null) {
                                templateData.enforceClone(constants.importTemplateWithoutBaseMustClone());
                            }
                            templateDataList.add(templateData);
                        }
                        setItems(templateDataList);
                        withDataCenterLoaded(storageDomainId, r -> doInit());
                    }

                    private VmTemplate findAnyVmTemplateById(List<VmTemplate> vmtList, Guid templateId) {
                        for (VmTemplate vmt : vmtList) {
                            if (templateId.equals(vmt.getId())) {
                                return vmt;
                            }
                        }
                        return null;
                    }
                }));

    }

    @Override
    protected void checkDestFormatCompatibility() {
    }

    @Override
    protected void initDisksStorageDomainsList() {
        for (Object item : getItems()) {
            VmTemplate template = ((ImportTemplateData) item).getTemplate();
            for (Disk disk : template.getDiskList()) {
                DiskImage diskImage = (DiskImage) disk;
                addDiskImportData(diskImage.getId(),
                        filteredStorageDomains, diskImage.getVolumeType(), new EntityModel(true));
            }
        }
        postInitDisks();
    }

    @Override
    protected String getListName() {
        return "ImportTemplateModel"; //$NON-NLS-1$
    }

    @Override
    public SearchableListModel getImportDiskListModel() {
        return templateImportDiskListModel;
    }

    @Override
    protected boolean validateNames() {
        return true;
    }

    public void setCloneObjectMap(Map<Guid, Object> cloneObjectMap) {
        this.cloneObjectMap = cloneObjectMap;
    }

    @Override
    public void executeImport(IFrontendMultipleActionAsyncCallback callback) {
        startProgress();
        Frontend.getInstance().runMultipleAction(
                ActionType.ImportVmTemplate,
                buildImportTemplateParameters(),
                callback,
                this);
    }

    private List<ActionParametersBase> buildImportTemplateParameters() {
        List<ActionParametersBase> prms = new ArrayList<>();
        for (Object object : getItems()) {
            ImportTemplateData importData = (ImportTemplateData) object;
            VmTemplate template = importData.getTemplate();

            ImportVmTemplateParameters importVmTemplateParameters =
                    new ImportVmTemplateParameters(getStoragePool().getId(),
                            (Guid) getEntity(),
                            Guid.Empty,
                            getCluster().getSelectedItem().getId(),
                            template);

            if (getClusterQuota().getSelectedItem() != null && getClusterQuota().getIsAvailable()) {
                importVmTemplateParameters.setQuotaId(getClusterQuota().getSelectedItem().getId());
            }

            CpuProfile cpuProfile = getCpuProfiles().getSelectedItem();
            if (cpuProfile != null) {
                importVmTemplateParameters.setCpuProfileId(cpuProfile.getId());
            }

            Map<Guid, Guid> map = new HashMap<>();
            for (DiskImage disk : template.getDiskList()) {
                map.put(disk.getId(), getDiskImportData(disk.getId()).getSelectedStorageDomain().getId());

                if (getDiskImportData(disk.getId()).getSelectedQuota() != null) {
                    disk.setQuotaId(getDiskImportData(disk.getId()).getSelectedQuota().getId());
                }
            }

            importVmTemplateParameters.setImageToDestinationDomainMap(map);

            if (importData.isExistsInSystem() || importData.getClone().getEntity()) {
                if (!cloneObjectMap.containsKey(template.getId())) {
                    continue;
                }
                importVmTemplateParameters.setImportAsNewEntity(true);
                importVmTemplateParameters.getVmTemplate()
                        .setName(((ImportTemplateData) cloneObjectMap.get(template.getId())).getTemplate().getName());
            }

            prms.add(importVmTemplateParameters);
        }

        return prms;
    }
}
