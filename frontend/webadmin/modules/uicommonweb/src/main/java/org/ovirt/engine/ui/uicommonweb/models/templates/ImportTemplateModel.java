package org.ovirt.engine.ui.uicommonweb.models.templates;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.queries.SearchParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
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
import com.google.inject.Inject;

public class ImportTemplateModel extends ImportVmFromExportDomainModel {

    private final TemplateImportDiskListModel templateImportDiskListModel;

    @Inject
    public ImportTemplateModel(final VmImportDiskListModel vmImportDiskListModel,
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

    private String createSearchPattern(Collection<VmTemplate> templates) {
        String vmt_guidKey = "_VMT_ID ="; //$NON-NLS-1$
        String orKey = " or "; //$NON-NLS-1$
        StringBuilder searchPattern = new StringBuilder();
        searchPattern.append("Template: "); //$NON-NLS-1$

        for (VmTemplate template : templates) {
            searchPattern.append(vmt_guidKey);
            searchPattern.append(template.getId().toString());
            searchPattern.append(orKey);
        }

        return searchPattern.substring(0, searchPattern.length() - orKey.length());
    }

    public void init(final Collection<VmTemplate> externalTemplates, final Guid storageDomainId) {
        Frontend.getInstance().runQuery(VdcQueryType.Search,
                new SearchParameters(createSearchPattern(externalTemplates), SearchType.VmTemplate),
                new AsyncQuery(this, new INewAsyncCallback() {

                    @Override
                    public void onSuccess(Object model, Object returnValue) {
                        List<VmTemplate> vmtList = ((VdcQueryReturnValue) returnValue).getReturnValue();

                        List<ImportTemplateData> templateDataList = new ArrayList<>();
                        for (VmTemplate template : externalTemplates) {
                            ImportTemplateData templateData = new ImportTemplateData(template);
                            boolean templateExistsInSystem = vmtList.contains(template);
                            templateData.setExistsInSystem(templateExistsInSystem);
                            if (templateExistsInSystem) {
                                templateData.getClone().setEntity(true);
                                templateData.getClone().setChangeProhibitionReason(ConstantsManager.getInstance()
                                        .getConstants()
                                        .importTemplateThatExistsInSystemMustClone());
                                templateData.getClone().setIsChangeable(false);
                            }
                            templateDataList.add(templateData);
                        }
                        setItems(templateDataList);
                        withDataCenterLoaded(storageDomainId, new INewAsyncCallback() {
                            @Override
                            public void onSuccess(Object model, Object returnValue) {
                                doInit();
                            }
                        });
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
}
