package org.ovirt.engine.ui.webadmin.section.main.view.tab.host;

import java.util.Map;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.common.widget.table.column.TextColumnWithTooltip;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostHooksListModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostListModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.host.SubTabHostHookPresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.AbstractSubTabTableView;

public class SubTabHostHookView extends AbstractSubTabTableView<VDS, Map<String, String>, HostListModel, HostHooksListModel>
        implements SubTabHostHookPresenter.ViewDef {

    @Inject
    public SubTabHostHookView(SearchableDetailModelProvider<Map<String, String>, HostListModel, HostHooksListModel> modelProvider, ApplicationConstants constants) {
        super(modelProvider);
        initTable(constants);
        initWidget(getTable());
    }

    void initTable(ApplicationConstants constants) {
        getTable().enableColumnResizing();

        TextColumnWithTooltip<Map<String, String>> eventColumn = new TextColumnWithTooltip<Map<String, String>>() {
            @Override
            public String getValue(Map<String, String> object) {
                return object.get("EventName"); //$NON-NLS-1$
            }
        };
        getTable().addColumn(eventColumn, constants.eventNameHook(), "220px"); //$NON-NLS-1$

        TextColumnWithTooltip<Map<String, String>> scriptColumn = new TextColumnWithTooltip<Map<String, String>>() {
            @Override
            public String getValue(Map<String, String> object) {
                return object.get("ScriptName"); //$NON-NLS-1$
            }
        };
        getTable().addColumn(scriptColumn, constants.scriptNameHook(), "220px"); //$NON-NLS-1$

        TextColumnWithTooltip<Map<String, String>> propNameColumn = new TextColumnWithTooltip<Map<String, String>>() {
            @Override
            public String getValue(Map<String, String> object) {
                return object.get("PropertyName"); //$NON-NLS-1$
            }
        };
        getTable().addColumn(propNameColumn, constants.propertyNameHook(), "220px"); //$NON-NLS-1$

        TextColumnWithTooltip<Map<String, String>> propValueColumn = new TextColumnWithTooltip<Map<String, String>>() {
            @Override
            public String getValue(Map<String, String> object) {
                return object.get("PropertyValue"); //$NON-NLS-1$
            }
        };
        getTable().addColumn(propValueColumn, constants.propertyValueHook(), "250px"); //$NON-NLS-1$
    }

}
