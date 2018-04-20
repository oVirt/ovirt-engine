package org.ovirt.engine.ui.webadmin.section.main.view.popup.scheduling;

import org.ovirt.engine.core.common.scheduling.PolicyUnit;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.view.popup.AbstractModelBoundPopupView;
import org.ovirt.engine.ui.common.widget.dialog.SimpleDialogPanel;
import org.ovirt.engine.ui.common.widget.editor.ListModelObjectCellTable;
import org.ovirt.engine.ui.common.widget.table.column.AbstractImageResourceColumn;
import org.ovirt.engine.ui.common.widget.table.column.AbstractTextColumn;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.configure.scheduling.ManagePolicyUnitModel;
import org.ovirt.engine.ui.uicompat.EnumTranslator;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.scheduling.ManagePolicyUnitPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.widget.table.cell.NullableButtonCell;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.inject.Inject;

public class ManagePolicyUnitPopupView extends AbstractModelBoundPopupView<ManagePolicyUnitModel> implements ManagePolicyUnitPopupPresenterWidget.ViewDef {

    interface ViewUiBinder extends UiBinder<SimpleDialogPanel, ManagePolicyUnitPopupView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    interface ViewIdHandler extends ElementIdHandler<ManagePolicyUnitPopupView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    @UiField
    FlowPanel policyUnitTableContainer;

    ListModelObjectCellTable<PolicyUnit, ListModel> policyUnitTable;

    private ManagePolicyUnitModel model;

    private static final ApplicationResources resources = AssetProvider.getResources();
    private static final ApplicationConstants constants = AssetProvider.getConstants();

    @Inject
    public ManagePolicyUnitPopupView(EventBus eventBus) {
        super(eventBus);
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        ViewIdHandler.idHandler.generateAndSetIds(this);
        initTable();
    }

    private void initTable() {
        policyUnitTable = new ListModelObjectCellTable<>();
        policyUnitTable.enableColumnResizing();
        policyUnitTableContainer.add(policyUnitTable);

        policyUnitTable.addColumn(new AbstractImageResourceColumn<PolicyUnit>() {
            @Override
            public ImageResource getValue(PolicyUnit object) {
                if (object.isInternal()) {
                    return resources.lockImage();
                }
                return resources.exteranlPolicyUnitImage();
            }

            @Override
            public SafeHtml getTooltip(PolicyUnit object) {
                String tooltipContent = null;
                if (object.isInternal()) {
                    tooltipContent = constants.internalPolicyUnit();
                } else {
                    tooltipContent = constants.externalPolicyUnit();
                }
                return SafeHtmlUtils.fromSafeConstant(tooltipContent);
            }
        }, constants.empty(), "20px"); //$NON-NLS-1$
        policyUnitTable.addColumn(new AbstractTextColumn<PolicyUnit>() {
            @Override
            public String getValue(PolicyUnit object) {
                return object.getName();
            }
        }, constants.policyUnitName(), "180px"); //$NON-NLS-1$

        policyUnitTable.addColumn(new AbstractTextColumn<PolicyUnit>() {
            @Override
            public String getValue(PolicyUnit object) {
                return EnumTranslator.getInstance().translate(object.getPolicyUnitType());
            }
        }, constants.policyUnitType(), "100px"); //$NON-NLS-1$

        policyUnitTable.addColumn(new AbstractTextColumn<PolicyUnit>() {
            @Override
            public String getValue(PolicyUnit object) {
                return object.isEnabled() ? constants.enabledPolicyUnit() : constants.disabledPolicyUnit();
            }
        }, constants.policyUnitEnabledStatus(), "75px"); //$NON-NLS-1$

        Column<PolicyUnit, String> removeButtonColumn = new Column<PolicyUnit, String>(new NullableButtonCell()) {
            @Override
            public String getValue(PolicyUnit object) {
                if (!object.isEnabled()) {
                    return constants.removePolicyUnit();
                }
                return null;
            }
        };

        policyUnitTable.addColumn(removeButtonColumn, constants.removePolicyUnit(), "80px"); //$NON-NLS-1$
        removeButtonColumn.setFieldUpdater((index, object, value) -> model.remove(object));
    }

    @Override
    public void edit(ManagePolicyUnitModel model) {
        this.model = model;
        policyUnitTable.asEditor().edit(model.getPolicyUnits());
    }

    @Override
    public ManagePolicyUnitModel flush() {
        return model;
    }

    @Override
    public void cleanup() {
        model.cleanup();
    }
}
