package org.ovirt.engine.ui.uicommonweb.models.hosts;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.mode.ApplicationMode;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryReturnValue;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;

@SuppressWarnings("unused")
public class HostHooksListModel extends SearchableListModel<VDS, Map<String, String>> {

    public HostHooksListModel() {
        setTitle(ConstantsManager.getInstance().getConstants().hostHooksTitle());
        setHelpTag(HelpTag.host_hooks);
        setHashName("host_hooks"); // $//$NON-NLS-1$
        setAvailableInModes(ApplicationMode.VirtOnly);
    }

    @Override
    protected void onEntityChanged() {
        super.onEntityChanged();

        getSearchCommand().execute();
    }

    @Override
    protected void entityPropertyChanged(Object sender, PropertyChangedEventArgs e) {
        super.entityPropertyChanged(sender, e);

        if (e.propertyName.equals("status")) { //$NON-NLS-1$
            getSearchCommand().execute();
        }
    }

    @Override
    public void search() {
        if (getEntity() != null) {
            super.search();
        }
    }

    @Override
    protected void syncSearch() {
        if (getEntity() == null) {
            return;
        }

        if (!getEntity().isContainingHooks()) {
            setIsEmpty(true);
            setItems(new ArrayList<>());
            return;
        }

        super.syncSearch();

        setIsEmpty(false);
        IdQueryParameters tempVar = new IdQueryParameters(getEntity().getId());
        tempVar.setRefresh(getIsQueryFirstTime());
        Frontend.getInstance().runQuery(QueryType.GetVdsHooksById, tempVar, new AsyncQuery<QueryReturnValue>(returnValue -> {
            List<Map<String, String>> list = new ArrayList<>();
            Map<String, Map<String, Map<String, String>>> dictionary = returnValue.getReturnValue();
            Map<String, String> row;
            for (Map.Entry<String, Map<String, Map<String, String>>> keyValuePair : dictionary.entrySet()) {
                for (Map.Entry<String, Map<String, String>> keyValuePair1 : keyValuePair.getValue().entrySet()) {
                    for (Map.Entry<String, String> keyValuePair2 : keyValuePair1.getValue().entrySet()) {
                        row = new HashMap<>();
                        row.put("EventName", keyValuePair.getKey()); //$NON-NLS-1$
                        row.put("ScriptName", keyValuePair1.getKey()); //$NON-NLS-1$
                        row.put("PropertyName", keyValuePair2.getKey()); //$NON-NLS-1$
                        row.put("PropertyValue", keyValuePair2.getValue()); //$NON-NLS-1$
                        list.add(row);
                    }
                }
            }
            setItems(list);
        }));

    }

    @Override
    protected String getListName() {
        return "HostHooksListModel"; //$NON-NLS-1$
    }
}
