package org.ovirt.engine.ui.webadmin.section.main.view.tab.host;

import java.util.Map;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostHooksListModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostListModel;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.host.SubTabHostHookPresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.AbstractSubTabTableView;
import org.ovirt.engine.ui.webadmin.uicommon.model.SearchableDetailModelProvider;

import com.google.gwt.user.cellview.client.TextColumn;

public class SubTabHostHookView extends AbstractSubTabTableView<VDS, Map<String, String>, HostListModel, HostHooksListModel>
        implements SubTabHostHookPresenter.ViewDef {

    @Inject
    public SubTabHostHookView(SearchableDetailModelProvider<Map<String, String>, HostListModel, HostHooksListModel> modelProvider) {
        super(modelProvider);
        initTable();
        initWidget(getTable());
    }

    void initTable() {
        TextColumn<Map<String, String>> eventColumn = new TextColumn<Map<String, String>>() {
            @Override
            public String getValue(Map<String, String> object) {
                return object.get("EventName");
            }
        };
        getTable().addColumn(eventColumn, "Event Name");

        TextColumn<Map<String, String>> scriptColumn = new TextColumn<Map<String, String>>() {
            @Override
            public String getValue(Map<String, String> object) {
                return object.get("ScriptName");
            }
        };
        getTable().addColumn(scriptColumn, "Script Name");

        TextColumn<Map<String, String>> propNameColumn = new TextColumn<Map<String, String>>() {
            @Override
            public String getValue(Map<String, String> object) {
                return object.get("PropertyName");
            }
        };
        getTable().addColumn(propNameColumn, "Property Name");

        TextColumn<Map<String, String>> propValueColumn = new TextColumn<Map<String, String>>() {
            @Override
            public String getValue(Map<String, String> object) {
                return object.get("PropertyValue");
            }
        };
        getTable().addColumn(propValueColumn, "Property Value");
    }

}
