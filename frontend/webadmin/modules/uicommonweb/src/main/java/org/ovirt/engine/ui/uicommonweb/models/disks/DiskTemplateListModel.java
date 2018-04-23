package org.ovirt.engine.ui.uicommonweb.models.disks;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryReturnValue;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

@SuppressWarnings("unused")
public class DiskTemplateListModel extends SearchableListModel<DiskImage, VmTemplate> {
    public DiskTemplateListModel() {
        setTitle(ConstantsManager.getInstance().getConstants().templatesTitle());
        setHelpTag(HelpTag.templates);
        setHashName("templates"); //$NON-NLS-1$
    }

    @Override
    protected void onEntityChanged() {
        super.onEntityChanged();

        if (getEntity() != null) {
            getSearchCommand().execute();
        }
    }

    @Override
    protected void syncSearch() {
        DiskImage diskImage = getEntity();
        if (diskImage == null) {
            return;
        }

        IdQueryParameters getVmTemplatesByImageGuidParameters = new IdQueryParameters(diskImage.getImageId());
        getVmTemplatesByImageGuidParameters.setRefresh(getIsQueryFirstTime());

        Frontend.getInstance().runQuery(QueryType.GetVmTemplatesByImageGuid, getVmTemplatesByImageGuidParameters, new AsyncQuery<QueryReturnValue>(
                returnValue -> {
                    Map<Boolean, VmTemplate> map = returnValue.getReturnValue();
                    List<VmTemplate> templates = new ArrayList<>();
                    templates.add(map.get(true));
                    setItems(templates);
                }));

        setIsQueryFirstTime(false);
    }

    @Override
    protected void onSelectedItemChanged() {
        super.onSelectedItemChanged();
        updateActionAvailability();
    }

    @Override
    protected void selectedItemsChanged() {
        super.selectedItemsChanged();
        updateActionAvailability();
    }

    private void updateActionAvailability() {
    }

    @Override
    protected String getListName() {
        return "DiskTemplateListModel"; //$NON-NLS-1$
    }
}
