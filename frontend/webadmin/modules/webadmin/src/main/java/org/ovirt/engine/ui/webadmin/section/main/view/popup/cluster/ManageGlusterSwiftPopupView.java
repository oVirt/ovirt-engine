package org.ovirt.engine.ui.webadmin.section.main.view.popup.cluster;

import org.ovirt.engine.core.common.businessentities.gluster.GlusterServerService;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterServiceStatus;
import org.ovirt.engine.core.common.businessentities.gluster.ServiceType;
import org.ovirt.engine.ui.common.editor.UiCommonEditorDriver;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.view.popup.AbstractModelBoundPopupView;
import org.ovirt.engine.ui.common.widget.Align;
import org.ovirt.engine.ui.common.widget.dialog.SimpleDialogPanel;
import org.ovirt.engine.ui.common.widget.editor.EntityModelCellTable;
import org.ovirt.engine.ui.common.widget.editor.generic.EntityModelCheckBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.generic.EntityModelLabelEditor;
import org.ovirt.engine.ui.common.widget.editor.generic.EntityModelRadioButtonEditor;
import org.ovirt.engine.ui.common.widget.renderer.EnumRenderer;
import org.ovirt.engine.ui.common.widget.table.cell.RadioboxCell;
import org.ovirt.engine.ui.common.widget.table.column.AbstractEntityModelTextColumn;
import org.ovirt.engine.ui.common.widget.table.column.AbstractEnumColumn;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.clusters.GlusterSwiftServiceModel;
import org.ovirt.engine.ui.uicommonweb.models.clusters.ManageGlusterSwiftModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.cluster.ManageGlusterSwiftPopupPresenterWidget;

import com.google.gwt.cell.client.Cell.Context;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.client.ui.Label;
import com.google.inject.Inject;

public class ManageGlusterSwiftPopupView extends AbstractModelBoundPopupView<ManageGlusterSwiftModel> implements ManageGlusterSwiftPopupPresenterWidget.ViewDef {

    interface Driver extends UiCommonEditorDriver<ManageGlusterSwiftModel, ManageGlusterSwiftPopupView> {
    }

    interface ViewUiBinder extends UiBinder<SimpleDialogPanel, ManageGlusterSwiftPopupView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    interface ViewIdHandler extends ElementIdHandler<ManageGlusterSwiftPopupView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    private final Driver driver = GWT.create(Driver.class);

    @UiField
    WidgetStyle style;

    @UiField(provided = true)
    @Path("swiftStatus.entity")
    EntityModelLabelEditor<GlusterServiceStatus> swiftStatusEditor;

    @UiField(provided = true)
    @Path("startSwift.entity")
    EntityModelRadioButtonEditor startSwift;

    @UiField(provided = true)
    @Path("stopSwift.entity")
    EntityModelRadioButtonEditor stopSwift;

    @UiField(provided = true)
    @Path("restartSwift.entity")
    EntityModelRadioButtonEditor restartSwift;

    @UiField(provided = true)
    @Path("isManageServerLevel.entity")
    EntityModelCheckBoxEditor manageSwiftServerLevel;

    @UiField(provided = true)
    @Ignore
    EntityModelCellTable<ListModel> hostServicesTable;

    @UiField
    @Ignore
    Label messageLabel;

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    @Inject
    public ManageGlusterSwiftPopupView(EventBus eventBus) {
        super(eventBus);
        initEditors();
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        ViewIdHandler.idHandler.generateAndSetIds(this);
        localize();
        applyStyles();
        driver.initialize(this);
    }

    private void initEditors() {
        swiftStatusEditor = new EntityModelLabelEditor<>(new EnumRenderer<GlusterServiceStatus>());
        startSwift = new EntityModelRadioButtonEditor("swift_action", Align.RIGHT); //$NON-NLS-1$
        stopSwift = new EntityModelRadioButtonEditor("swift_action", Align.RIGHT); //$NON-NLS-1$
        restartSwift = new EntityModelRadioButtonEditor("swift_action", Align.RIGHT); //$NON-NLS-1$
        manageSwiftServerLevel = new EntityModelCheckBoxEditor(Align.RIGHT);

        hostServicesTable = new EntityModelCellTable<>(false, true);
        hostServicesTable.addColumn(new AbstractEntityModelTextColumn<GlusterServerService>() {
            @Override
            public String getText(GlusterServerService entity) {
                return entity.getHostName();
            }
        }, constants.hostGlusterSwift());

        hostServicesTable.addColumn(new AbstractEnumColumn<EntityModel, ServiceType>() {
            @Override
            protected ServiceType getRawValue(EntityModel object) {
                return ((GlusterSwiftServiceModel) object).getEntity().getServiceType();
            }
        }, constants.serviceNameGlusterSwift());

        hostServicesTable.addColumn(new AbstractEnumColumn<EntityModel, GlusterServiceStatus>() {
            @Override
            protected GlusterServiceStatus getRawValue(EntityModel object) {
                return ((GlusterSwiftServiceModel) object).getEntity().getStatus();
            }
        }, constants.serviceStatusGlusterSwift());

        Column<EntityModel, Boolean> startSwiftColumn =
                new Column<EntityModel, Boolean>(new RadioboxCell(false, true)) {
                    @Override
                    public Boolean getValue(EntityModel object) {
                        GlusterSwiftServiceModel swiftServiceModel = (GlusterSwiftServiceModel) object;
                        return swiftServiceModel.getStartSwift().getEntity();
                    }

                    @Override
                    public void render(Context context, EntityModel object, SafeHtmlBuilder sb) {
                        GlusterSwiftServiceModel swiftServiceModel = (GlusterSwiftServiceModel) object;
                        if (swiftServiceModel.getStartSwift().getIsChangable()) {
                            super.render(context, object, sb);
                        }
                    }
                };
        startSwiftColumn.setFieldUpdater((index, object, value) -> {
            GlusterSwiftServiceModel swiftModel = (GlusterSwiftServiceModel) object;
            swiftModel.getStartSwift().setEntity(value);
            if (value) {
                swiftModel.getStopSwift().setEntity(false);
                swiftModel.getRestartSwift().setEntity(false);
                hostServicesTable.redraw();
            }
        });
        hostServicesTable.addColumn(startSwiftColumn, constants.startGlusterSwift());

        Column<EntityModel, Boolean> stopSwiftColumn = new Column<EntityModel, Boolean>(new RadioboxCell(false, true)) {
            @Override
            public Boolean getValue(EntityModel object) {
                GlusterSwiftServiceModel swiftServiceModel = (GlusterSwiftServiceModel) object;
                return swiftServiceModel.getStopSwift().getEntity();
            }

            @Override
            public void render(Context context, EntityModel object, SafeHtmlBuilder sb) {
                GlusterSwiftServiceModel swiftServiceModel = (GlusterSwiftServiceModel) object;
                if (swiftServiceModel.getStopSwift().getIsChangable()) {
                    super.render(context, object, sb);
                }
            }
        };
        stopSwiftColumn.setFieldUpdater((index, object, value) -> {
            GlusterSwiftServiceModel swiftModel = (GlusterSwiftServiceModel) object;
            if (swiftModel.getStopSwift().getIsChangable()) {
                swiftModel.getStopSwift().setEntity(value);
                if (value) {
                    swiftModel.getStartSwift().setEntity(false);
                    swiftModel.getRestartSwift().setEntity(false);
                    hostServicesTable.redraw();
                }
            }
        });
        hostServicesTable.addColumn(stopSwiftColumn, constants.stopGlusterSwift());

        Column<EntityModel, Boolean> restartSwiftColumn =
                new Column<EntityModel, Boolean>(new RadioboxCell(false, true)) {
                    @Override
                    public Boolean getValue(EntityModel object) {
                        GlusterSwiftServiceModel swiftServiceModel = (GlusterSwiftServiceModel) object;
                        return swiftServiceModel.getRestartSwift().getEntity();
                    }

                    @Override
                    public void render(Context context, EntityModel object, SafeHtmlBuilder sb) {
                        GlusterSwiftServiceModel swiftServiceModel = (GlusterSwiftServiceModel) object;
                        if (swiftServiceModel.getRestartSwift().getIsChangable()) {
                            super.render(context, object, sb);
                        }
                    }
                };
        restartSwiftColumn.setFieldUpdater((index, object, value) -> {
            GlusterSwiftServiceModel swiftModel = (GlusterSwiftServiceModel) object;
            swiftModel.getRestartSwift().setEntity(value);
            if (value) {
                swiftModel.getStartSwift().setEntity(false);
                swiftModel.getStopSwift().setEntity(false);
                hostServicesTable.redraw();
            }
        });
        hostServicesTable.addColumn(restartSwiftColumn, constants.restartGlusterSwift());
    }

    private void localize() {
        swiftStatusEditor.setLabel(constants.clusterGlusterSwiftLabel());
        startSwift.setLabel(constants.startGlusterSwift());
        stopSwift.setLabel(constants.stopGlusterSwift());
        restartSwift.setLabel(constants.restartGlusterSwift());
        manageSwiftServerLevel.setLabel(constants.manageServerLevelGlusterSwift());
    }

    private void applyStyles() {
        swiftStatusEditor.addContentWidgetContainerStyleName(style.swiftStatusWidget());
    }

    @Override
    public void edit(ManageGlusterSwiftModel object) {
        driver.edit(object);
        hostServicesTable.asEditor().edit(object.getHostServicesList());
    }

    @Override
    public ManageGlusterSwiftModel flush() {
        hostServicesTable.flush();
        return driver.flush();
    }

    @Override
    public void cleanup() {
        driver.cleanup();
    }

    @Override
    public void setMessage(String message) {
        super.setMessage(message);
        messageLabel.setText(message);
    }

    interface WidgetStyle extends CssResource {

        String swiftStatusWidget();
    }
}
