package org.ovirt.engine.ui.uicommonweb.models.templates;

import java.util.HashMap;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.Disk;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.queries.SearchParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.ObservableCollection;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.IIsObjectInSetup;
import org.ovirt.engine.ui.uicommonweb.models.vms.ImportVmModel;

@SuppressWarnings("unused")
public class ImportTemplateModel extends ImportVmModel implements IIsObjectInSetup
{

    private HashMap<Guid, VmTemplate> alreadyInSystem;
    private TemplateImportDiskListModel templateImportDiskListModel;

    public ImportTemplateModel() {
        super();
        disksToConvert = null;
        getCollapseSnapshots().setIsAvailable(false);
    }

    @Override
    protected void InitDetailModels()
    {
        super.InitDetailModels();

        ObservableCollection<EntityModel> list = new ObservableCollection<EntityModel>();
        list.add(new TemplateGeneralModel());
        list.add(new TemplateImportInterfaceListModel());
        this.templateImportDiskListModel = new TemplateImportDiskListModel();
        list.add(templateImportDiskListModel);
        setDetailModels(list);
    }

    @Override
    public void setItems(final Iterable value)
    {
        String vmt_guidKey = "_VMT_ID ="; //$NON-NLS-1$
        String orKey = " or "; //$NON-NLS-1$
        StringBuilder searchPattern = new StringBuilder();
        searchPattern.append("Template: "); //$NON-NLS-1$

        final List<VmTemplate> list = (List<VmTemplate>) value;
        for (int i = 0; i < list.size(); i++) {
            VmTemplate vmTemplate = list.get(i);

            searchPattern.append(vmt_guidKey);
            searchPattern.append(vmTemplate.getId().toString());
            if (i < list.size() - 1) {
                searchPattern.append(orKey);
            }
        }

        Frontend.RunQuery(VdcQueryType.Search,
                new SearchParameters(searchPattern.toString(), SearchType.VmTemplate),
                new AsyncQuery(this, new INewAsyncCallback() {

                    @Override
                    public void OnSuccess(Object model, Object returnValue) {
                        List<VmTemplate> vmtList =
                                (List<VmTemplate>) ((VdcQueryReturnValue) returnValue).getReturnValue();

                        alreadyInSystem = new HashMap<Guid, VmTemplate>();
                        for (VmTemplate template : vmtList) {
                            alreadyInSystem.put(template.getId(), template);
                        }
                        if (vmtList.size() == list.size()) {
                            getCloneAll().setEntity(true);
                            getCloneAll().setIsChangable(false);
                        }
                        ImportTemplateModel.super.setSuperItems(value);
                    }
                }));

    }

    @Override
    protected void checkDestFormatCompatibility() {
    }

    @Override
    protected void initDisksStorageDomainsList() {
        for (Object item : getItems()) {
            VmTemplate template = (VmTemplate) item;
            for (Disk disk : template.getDiskList()) {
                DiskImage diskImage = (DiskImage) disk;
                setDiskImportData(diskImage.getId(),
                        filteredStorageDomains, diskImage.getvolume_type());
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
    public boolean isObjectInSetup(Object vmTemplate) {
        if (alreadyInSystem == null) {
            return false;
        }
        return alreadyInSystem.containsKey(((VmTemplate) vmTemplate).getId());
    }
}
