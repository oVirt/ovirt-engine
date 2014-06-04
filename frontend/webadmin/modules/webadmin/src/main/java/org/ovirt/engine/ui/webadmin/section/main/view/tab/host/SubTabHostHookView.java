package org.ovirt.engine.ui.webadmin.section.main.view.tab.host;

import java.util.Comparator;
import java.util.Map;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.comparators.LexoNumericComparator;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.common.widget.table.column.TextColumnWithTooltip;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostHooksListModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostListModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.host.SubTabHostHookPresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.AbstractSubTabTableView;

public class SubTabHostHookView extends AbstractSubTabTableView<VDS, Map<String, String>, HostListModel, HostHooksListModel>
        implements SubTabHostHookPresenter.ViewDef {
    private static final String EVENT_NAME = "EventName"; //$NON-NLS-1$
    private static final String SCRIPT_NAME = "ScriptName"; //$NON-NLS-1$
    private static final String PROPERTY_NAME = "PropertyName"; //$NON-NLS-1$
    private static final String PROPERTY_VALUE = "PropertyValue"; //$NON-NLS-1$

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
                return object.get(EVENT_NAME);
            }
        };
        eventColumn.makeSortable(new HostHookComparator(EVENT_NAME));
        getTable().addColumn(eventColumn, constants.eventNameHook(), "220px"); //$NON-NLS-1$

        TextColumnWithTooltip<Map<String, String>> scriptColumn = new TextColumnWithTooltip<Map<String, String>>() {
            @Override
            public String getValue(Map<String, String> object) {
                return object.get(SCRIPT_NAME);
            }
        };
        scriptColumn.makeSortable(new HostHookComparator(SCRIPT_NAME));
        getTable().addColumn(scriptColumn, constants.scriptNameHook(), "220px"); //$NON-NLS-1$

        TextColumnWithTooltip<Map<String, String>> propNameColumn = new TextColumnWithTooltip<Map<String, String>>() {
            @Override
            public String getValue(Map<String, String> object) {
                return object.get(PROPERTY_NAME);
            }
        };
        propNameColumn.makeSortable(new HostHookComparator(PROPERTY_NAME));
        getTable().addColumn(propNameColumn, constants.propertyNameHook(), "220px"); //$NON-NLS-1$

        TextColumnWithTooltip<Map<String, String>> propValueColumn = new TextColumnWithTooltip<Map<String, String>>() {
            @Override
            public String getValue(Map<String, String> object) {
                return object.get(PROPERTY_VALUE);
            }
        };
        propValueColumn.makeSortable(new HostHookComparator(PROPERTY_VALUE));
        getTable().addColumn(propValueColumn, constants.propertyValueHook(), "250px"); //$NON-NLS-1$
    }

    private static class HostHookComparator implements Comparator<Map<String, String>> {
        private static final LexoNumericComparator lexoNumericComparator = new LexoNumericComparator();
        private final String key;

        HostHookComparator(String key) {
            this.key = key;
        }

        @Override
        public int compare(Map<String, String> map1, Map<String, String> map2) {
            return lexoNumericComparator.compare(map1.get(key), map2.get(key));
        }
    };
}
