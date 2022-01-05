package org.ovirt.engine.ui.webadmin.section.main.view.tab;

import java.util.Arrays;

import org.gwtbootstrap3.client.ui.constants.Styles;
import org.ovirt.engine.core.common.businessentities.Permission;
import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.gin.AssetProvider;
import org.ovirt.engine.ui.common.system.ClientStorage;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.common.view.AbstractSubTabTableWidgetView;
import org.ovirt.engine.ui.common.view.ViewRadioGroup;
import org.ovirt.engine.ui.common.widget.action.PermissionActionPanelPresenterWidget;
import org.ovirt.engine.ui.common.widget.uicommon.permissions.PermissionWithInheritedPermissionListModelTable;
import org.ovirt.engine.ui.uicommonweb.models.ListWithDetailsModel;
import org.ovirt.engine.ui.uicommonweb.models.configure.PermissionFilter;
import org.ovirt.engine.ui.uicommonweb.models.configure.PermissionListModel;

import com.google.gwt.dom.client.Style;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.inject.Inject;

public abstract class AbstractSubTabPermissionsView<I, M extends ListWithDetailsModel, T extends PermissionListModel<I>> extends AbstractSubTabTableWidgetView<I, Permission, M, T> {

    private static final CommonApplicationConstants constants = AssetProvider.getConstants();

    @Inject
    public AbstractSubTabPermissionsView(
            SearchableDetailModelProvider<Permission, M, T> modelProvider,
            EventBus eventBus, ClientStorage clientStorage, PermissionActionPanelPresenterWidget<I, M, T> actionPanel) {
        super(new PermissionWithInheritedPermissionListModelTable<>(
                modelProvider, eventBus, actionPanel, clientStorage));
        generateIds();
        initTable();
        getTable().setTableOverhead(createOverheadPanel());
        initWidget(getModelBoundTableWidget());

    }

    protected abstract void generateIds();

    private FlowPanel createOverheadPanel() {
        Label label = new Label();
        label.setText(constants.permissionFilter() + ":"); //$NON-NLS-1$
        label.addStyleName(Styles.PULL_LEFT);
        label.getElement().getStyle().setMarginTop(3, Style.Unit.PX);
        label.getElement().getStyle().setMarginRight(5, Style.Unit.PX);

        ViewRadioGroup<PermissionFilter> viewRadioGroup = new ViewRadioGroup<>(Arrays.asList(PermissionFilter.values()));
        viewRadioGroup.setSelectedValue(PermissionFilter.ALL_PERMISSIONS);
        viewRadioGroup.addChangeHandler(this::onFilterChange);

        FlowPanel overheadPanel = new FlowPanel();
        overheadPanel.add(label);
        overheadPanel.add(viewRadioGroup);

        return overheadPanel;
    }

    private void onFilterChange(PermissionFilter selected) {
        getModelBoundTableWidget().getModel().setDirectOnly(selected.getValue() == PermissionFilter.DIRECT_PERMISSIONS);
    }
}
