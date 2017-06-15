package org.ovirt.engine.ui.webadmin.section.main.view.tab.host;

import java.util.Comparator;
import java.util.Map;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.comparators.LexoNumericComparator;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.common.widget.table.column.AbstractTextColumn;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostHooksListModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostListModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.host.SubTabHostHookPresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.AbstractSubTabTableView;

import com.google.gwt.core.client.GWT;

public class SubTabHostHookView extends AbstractSubTabTableView<VDS, Map<String, String>, HostListModel<Void>, HostHooksListModel>
        implements SubTabHostHookPresenter.ViewDef {

    interface ViewIdHandler extends ElementIdHandler<SubTabHostHookView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    private static final String EVENT_NAME = "EventName"; //$NON-NLS-1$
    private static final String SCRIPT_NAME = "ScriptName"; //$NON-NLS-1$
    private static final String PROPERTY_NAME = "PropertyName"; //$NON-NLS-1$
    private static final String PROPERTY_VALUE = "PropertyValue"; //$NON-NLS-1$

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    @Inject
    public SubTabHostHookView(SearchableDetailModelProvider<Map<String, String>, HostListModel<Void>, HostHooksListModel> modelProvider) {
        super(modelProvider);
        initTable();
        initWidget(getTable());
    }

    @Override
    protected void generateIds() {
        ViewIdHandler.idHandler.generateAndSetIds(this);
    }

    void initTable() {
        getTable().enableColumnResizing();

        AbstractTextColumn<Map<String, String>> eventColumn = new AbstractTextColumn<Map<String, String>>() {
            @Override
            public String getValue(Map<String, String> object) {
                return object.get(EVENT_NAME);
            }
        };
        eventColumn.makeSortable(new HostHookComparator(EVENT_NAME));
        getTable().addColumn(eventColumn, constants.eventNameHook(), "220px"); //$NON-NLS-1$

        AbstractTextColumn<Map<String, String>> scriptColumn = new AbstractTextColumn<Map<String, String>>() {
            @Override
            public String getValue(Map<String, String> object) {
                return object.get(SCRIPT_NAME);
            }
        };
        scriptColumn.makeSortable(new HostHookComparator(SCRIPT_NAME));
        getTable().addColumn(scriptColumn, constants.scriptNameHook(), "220px"); //$NON-NLS-1$

        AbstractTextColumn<Map<String, String>> propNameColumn = new AbstractTextColumn<Map<String, String>>() {
            @Override
            public String getValue(Map<String, String> object) {
                return object.get(PROPERTY_NAME);
            }
        };
        propNameColumn.makeSortable(new HostHookComparator(PROPERTY_NAME));
        getTable().addColumn(propNameColumn, constants.propertyNameHook(), "220px"); //$NON-NLS-1$

        AbstractTextColumn<Map<String, String>> propValueColumn = new AbstractTextColumn<Map<String, String>>() {
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
