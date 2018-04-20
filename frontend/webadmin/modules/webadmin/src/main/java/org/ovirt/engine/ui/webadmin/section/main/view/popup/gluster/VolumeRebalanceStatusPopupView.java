package org.ovirt.engine.ui.webadmin.section.main.view.popup.gluster;

import java.util.Date;

import org.gwtbootstrap3.client.ui.Column;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeTaskStatusForHost;
import org.ovirt.engine.ui.common.editor.UiCommonEditorDriver;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.idhandler.WithElementId;
import org.ovirt.engine.ui.common.view.popup.AbstractModelBoundPopupView;
import org.ovirt.engine.ui.common.widget.dialog.SimpleDialogPanel;
import org.ovirt.engine.ui.common.widget.editor.EntityModelCellTable;
import org.ovirt.engine.ui.common.widget.editor.generic.EntityModelLabelEditor;
import org.ovirt.engine.ui.common.widget.editor.generic.StringEntityModelLabelEditor;
import org.ovirt.engine.ui.common.widget.renderer.FullDateTimeRenderer;
import org.ovirt.engine.ui.common.widget.table.column.AbstractEntityModelTextColumn;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.gluster.VolumeRebalanceStatusModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.gluster.VolumeRebalanceStatusPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.widget.table.column.AbstractHumanReadableTimeColumn;
import org.ovirt.engine.ui.webadmin.widget.table.column.AbstractRebalanceFileSizeColumn;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.text.shared.AbstractRenderer;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Label;
import com.google.inject.Inject;

public class VolumeRebalanceStatusPopupView extends AbstractModelBoundPopupView<VolumeRebalanceStatusModel> implements VolumeRebalanceStatusPopupPresenterWidget.ViewDef {

    interface Driver extends UiCommonEditorDriver<VolumeRebalanceStatusModel, VolumeRebalanceStatusPopupView> {
    }

    interface ViewUiBinder extends UiBinder<SimpleDialogPanel, VolumeRebalanceStatusPopupView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    interface ViewIdHandler extends ElementIdHandler<VolumeRebalanceStatusPopupView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    @UiField
    @Path("volume.entity")
    @WithElementId
    StringEntityModelLabelEditor volumeEditor;

    @UiField
    @Path("cluster.entity")
    @WithElementId
    StringEntityModelLabelEditor clusterEditor;

    @UiField(provided = true)
    @Path("startTime.entity")
    @WithElementId
    EntityModelLabelEditor<Date> startTimeEditor;

    @UiField(provided = true)
    @Path("statusTime.entity")
    @WithElementId
    EntityModelLabelEditor<Date> statusTimeEditor;

    @UiField
    @Ignore
    @WithElementId
    Label status;

    @UiField(provided = true)
    @Ignore
    @WithElementId
    EntityModelCellTable<ListModel> rebalanceHostsTable;

    @UiField
    @Ignore
    Label messageLabel;

    @UiField(provided = true)
    @Path("stopTime.entity")
    @WithElementId
    EntityModelLabelEditor<Date> stopTimeEditor;

    @UiField
    @Ignore
    @WithElementId
    Column stopTimeColumn;

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    protected FullDateTimeRenderer renderer = new FullDateTimeRenderer(true);

    private final Driver driver = GWT.create(Driver.class);

    @Inject
    public VolumeRebalanceStatusPopupView(EventBus eventBus) {
        super(eventBus);
        initEditors();
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        ViewIdHandler.idHandler.generateAndSetIds(this);
        localize();
        setVisibilities();
        driver.initialize(this);
    }

    private void setVisibilities() {
        status.setVisible(false);
    }

    private void localize() {
        status.setText(constants.rebalanceComplete());
        startTimeEditor.setLabel(constants.rebalanceStartTime());
        volumeEditor.setLabel(constants.rebalanceVolumeName());
        clusterEditor.setLabel(constants.rebalanceClusterVolume());
        statusTimeEditor.setLabel(constants.rebalanceStatusTime());
        stopTimeEditor.setLabel(constants.rebalanceStopTime());
    }

    void initEditors() {
        rebalanceHostsTable = new EntityModelCellTable<>(false, true);

        statusTimeEditor = getInstanceOfDateEditor();

        startTimeEditor = getInstanceOfDateEditor();

        stopTimeEditor = getInstanceOfDateEditor();

        rebalanceHostsTable.addColumn(new AbstractEntityModelTextColumn<GlusterVolumeTaskStatusForHost>() {
            @Override
            protected String getText(GlusterVolumeTaskStatusForHost entity) {
                return entity.getHostName();
            }
        }, constants.rebalanceSessionHost());

        rebalanceHostsTable.addColumn(new AbstractEntityModelTextColumn<GlusterVolumeTaskStatusForHost>() {
            @Override
            protected String getText(GlusterVolumeTaskStatusForHost entity) {
                return entity.getFilesMoved() + "";
            }
        }, getColumnHeaderForFilesMoved());

        rebalanceHostsTable.addColumn(new AbstractRebalanceFileSizeColumn<EntityModel>() {

            @Override
            protected Long getRawValue(EntityModel object) {
                return ((GlusterVolumeTaskStatusForHost) object.getEntity()).getTotalSizeMoved();
            }
        }, constants.rebalanceSize());

        rebalanceHostsTable.addColumn(new AbstractEntityModelTextColumn<GlusterVolumeTaskStatusForHost>() {

            @Override
            protected String getText(GlusterVolumeTaskStatusForHost entity) {
                return String.valueOf(entity.getFilesScanned());
            }
        }, constants.rebalanceScannedFileCount());

        rebalanceHostsTable.addColumn(new AbstractEntityModelTextColumn<GlusterVolumeTaskStatusForHost>() {
            @Override
            protected String getText(GlusterVolumeTaskStatusForHost entity) {
                return String.valueOf(entity.getFilesFailed());
            }
        }, constants.rebalanceFailedFileCount());

        if (isSkippedFileCountNeeded()){
            rebalanceHostsTable.addColumn(new AbstractEntityModelTextColumn<GlusterVolumeTaskStatusForHost>() {
                @Override
                protected String getText(GlusterVolumeTaskStatusForHost entity) {
                    return String.valueOf(entity.getFilesSkipped());
                }
            }, constants.rebalanceSkippedFileCount());
        }

        rebalanceHostsTable.addColumn(new AbstractEntityModelTextColumn<GlusterVolumeTaskStatusForHost>() {
            @Override
            protected String getText(GlusterVolumeTaskStatusForHost entity) {
                return entity.getStatus().toString();
            }
        }, constants.rebalanceStatus());

        rebalanceHostsTable.addColumn(new AbstractHumanReadableTimeColumn<EntityModel>() {

            @Override
            protected Double getRawValue(EntityModel object) {
                return ((GlusterVolumeTaskStatusForHost) object.getEntity()).getRunTime();
            }
        }, constants.rebalanceRunTime());
    }

    public boolean isSkippedFileCountNeeded(){
        return true;
    }

    public String getColumnHeaderForFilesMoved() {
        return constants.rebalanceFileCount();
    }

    @Override
    public void edit(final VolumeRebalanceStatusModel object) {
        driver.edit(object);

        rebalanceHostsTable.asEditor().edit(object.getRebalanceSessions());

        object.getPropertyChangedEvent().addListener((ev, sender, args) -> {
            if (args.propertyName.equals("STATUS_UPDATED")) {//$NON-NLS-1$
                status.setVisible(object.isStatusAvailable());
            } else if (args.propertyName.equals("STOP_TIME_UPDATED")) {//$NON-NLS-1$
                stopTimeColumn.setVisible(object.isStopTimeVisible());
            }
        });
    }

    private EntityModelLabelEditor<Date> getInstanceOfDateEditor() {
        return new EntityModelLabelEditor<>(new AbstractRenderer<Date>() {
            @Override
            public String render(Date entity) {
                if(entity == null) {
                    return constants.unAvailablePropertyLabel();
                }
                return renderer.render(entity);
            }
        });
    }

    @Override
    public VolumeRebalanceStatusModel flush() {
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
}
