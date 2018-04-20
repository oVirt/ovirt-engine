package org.ovirt.engine.ui.uicommonweb.models.storage;

import java.util.ArrayList;
import java.util.Collections;

import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.comparators.LexoNumericNameableComparator;
import org.ovirt.engine.core.common.queries.GetVmTemplatesFromStorageDomainParameters;
import org.ovirt.engine.core.common.queries.QueryReturnValue;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

@SuppressWarnings("unused")
public class StorageTemplateListModel extends SearchableListModel<StorageDomain, VmTemplate> {

    public StorageTemplateListModel() {
        setTitle(ConstantsManager.getInstance().getConstants().templatesTitle());
        setHelpTag(HelpTag.templates);
        setHashName("templates"); // $//$NON-NLS-1$
    }

    @Override
    protected void onEntityChanged() {
        super.onEntityChanged();

        getSearchCommand().execute();
    }

    @Override
    public void search() {
        if (getEntity() != null) {
            super.search();
        } else {
            setItems(null);
        }
    }

    @Override
    protected void syncSearch() {
        if (getEntity() == null) {
            return;
        }

        super.syncSearch();

        GetVmTemplatesFromStorageDomainParameters tempVar =
                new GetVmTemplatesFromStorageDomainParameters(getEntity().getId(), true);
        tempVar.setRefresh(getIsQueryFirstTime());
        Frontend.getInstance().runQuery(QueryType.GetVmTemplatesFromStorageDomain, tempVar, new AsyncQuery<QueryReturnValue>(returnValue -> {
            ArrayList<VmTemplate> templates = returnValue.getReturnValue();
            Collections.sort(templates, new LexoNumericNameableComparator<>());
            setItems(templates);
            setIsEmpty(templates.size() == 0);
        }));
    }

    @Override
    protected String getListName() {
        return "StorageTemplateListModel"; //$NON-NLS-1$
    }
}
