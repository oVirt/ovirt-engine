package org.ovirt.engine.ui.webadmin.section.main.view.popup.macpool;

import org.ovirt.engine.core.common.businessentities.MacPool;
import org.ovirt.engine.ui.common.MainTableHeaderlessResources;
import org.ovirt.engine.ui.common.MainTableResources;
import org.ovirt.engine.ui.common.system.ClientStorage;
import org.ovirt.engine.ui.common.widget.table.SimpleActionTable;
import org.ovirt.engine.ui.common.widget.table.column.TextColumnWithTooltip;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.uicommon.model.SharedMacPoolModelProvider;
import org.ovirt.engine.ui.webadmin.widget.action.WebAdminButtonDefinition;
import org.ovirt.engine.ui.webadmin.widget.table.column.WebAdminImageResourceColumn;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.SplitLayoutPanel;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.inject.Inject;

public class SharedMacPoolView extends Composite {

    private SplitLayoutPanel rootPanel;
    private SimpleActionTable<MacPool> macPoolTable;

    @Inject
    public SharedMacPoolView(final SharedMacPoolModelProvider sharedMacPoolModelProvider,
            EventBus eventBus,
            ClientStorage clientStorage,
            MainTableHeaderlessResources headerlessResources,
            MainTableResources tableResources,
            final ApplicationConstants constants,
            final ApplicationResources resources) {

        rootPanel = new SplitLayoutPanel();
        rootPanel.setHeight("495px"); //$NON-NLS-1$
        rootPanel.setWidth("100%"); //$NON-NLS-1$
        initWidget(rootPanel);

        macPoolTable =
                new SimpleActionTable<MacPool>(sharedMacPoolModelProvider,
                        headerlessResources,
                        tableResources,
                        eventBus,
                        clientStorage);
        macPoolTable.addColumn(new WebAdminImageResourceColumn<MacPool>() {

            @Override
            public ImageResource getValue(MacPool macPool) {
                return macPool.isDefaultPool() ? resources.lockImage() : null;
            }
        }, constants.empty(), "20px"); //$NON-NLS-1$
        macPoolTable.addColumn(new TextColumnWithTooltip<MacPool>() {

            @Override
            public String getValue(MacPool macPool) {
                return macPool.getName();
            }
        }, constants.configureMacPoolNameColumn(), "100px"); //$NON-NLS-1$
        macPoolTable.addColumn(new TextColumnWithTooltip<MacPool>() {

            @Override
            public String getValue(MacPool macPool) {
                return macPool.getDescription();
            }
        }, constants.configureMacPoolDescriptionColumn(), "300px"); //$NON-NLS-1$
        macPoolTable.addActionButton(new WebAdminButtonDefinition<MacPool>(constants.configureMacPoolAddButton()) {

            @Override
            protected UICommand resolveCommand() {
                return sharedMacPoolModelProvider.getModel().getNewCommand();
            }
        });
        macPoolTable.addActionButton(new WebAdminButtonDefinition<MacPool>(constants.configureMacPoolEditButton()) {

            @Override
            protected UICommand resolveCommand() {
                return sharedMacPoolModelProvider.getModel().getEditCommand();
            }
        });
        macPoolTable.addActionButton(new WebAdminButtonDefinition<MacPool>(constants.configureMacPoolRemoveButton()) {

            @Override
            protected UICommand resolveCommand() {
                return sharedMacPoolModelProvider.getModel().getRemoveCommand();
            }
        });
        macPoolTable.getSelectionModel().addSelectionChangeHandler(new SelectionChangeEvent.Handler() {

            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                sharedMacPoolModelProvider.setSelectedItems(macPoolTable.getSelectedItems());
            }
        });
        rootPanel.add(macPoolTable);
    }

}
