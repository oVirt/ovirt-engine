package org.ovirt.engine.ui.webadmin.section.main.view.popup.gluster;

import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeTaskStatusForHost;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.idhandler.WithElementId;
import org.ovirt.engine.ui.common.view.popup.AbstractModelBoundPopupView;
import org.ovirt.engine.ui.common.widget.dialog.SimpleDialogPanel;
import org.ovirt.engine.ui.common.widget.editor.EntityModelCellTable;
import org.ovirt.engine.ui.common.widget.editor.EntityModelLabelEditor;
import org.ovirt.engine.ui.common.widget.table.column.EntityModelTextColumn;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.gluster.VolumeRebalanceStatusModel;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.IEventListener;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationMessages;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.gluster.VolumeRebalanceStatusPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.widget.table.column.RebalanceFileSizeColumn;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.inject.Inject;

public class VolumeRebalanceStatusPopupView extends AbstractModelBoundPopupView<VolumeRebalanceStatusModel> implements VolumeRebalanceStatusPopupPresenterWidget.ViewDef {

    interface Driver extends SimpleBeanEditorDriver<VolumeRebalanceStatusModel, VolumeRebalanceStatusPopupView> {
    }

    interface ViewUiBinder extends UiBinder<SimpleDialogPanel, VolumeRebalanceStatusPopupView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    interface ViewIdHandler extends ElementIdHandler<VolumeRebalanceStatusPopupView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    @UiField
    @Ignore
    @WithElementId
    SimpleDialogPanel panel;

    @UiField
    @Path("volume.entity")
    @WithElementId
    EntityModelLabelEditor volumeEditor;

    @UiField
    @Path("cluster.entity")
    @WithElementId
    EntityModelLabelEditor clusterEditor;

    @UiField
    @Path("startedTime.entity")
    @WithElementId
    EntityModelLabelEditor startedTimeEditor;

    @UiField
    @Path("statusTime.entity")
    @WithElementId
    EntityModelLabelEditor statusTimeEditor;

    @UiField
    @Ignore
    @WithElementId
    Label statusLabel;

    @UiField
    @Ignore
    @WithElementId
    ScrollPanel sPanel;

    @UiField(provided = true)
    @Ignore
    @WithElementId
    EntityModelCellTable<ListModel> rebalanceHostsTable;

    @UiField
    @Ignore
    Label messageLabel;

    ApplicationMessages messages;

    ApplicationConstants constants;

    private final Driver driver = GWT.create(Driver.class);

    @Inject
    public VolumeRebalanceStatusPopupView(EventBus eventBus, ApplicationResources resources, ApplicationConstants constants,ApplicationMessages messages) {
        super(eventBus, resources);
        initEditors(constants);
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        ViewIdHandler.idHandler.generateAndSetIds(this);
        localize(constants);
        driver.initialize(this);
        this.messages = messages;
        this.constants = constants;
    }

    private void localize(final ApplicationConstants constants) {
        startedTimeEditor.setLabel(constants.rebalanceStartTime());
        volumeEditor.setLabel(constants.rebalanceVolumeName());
        clusterEditor.setLabel(constants.rebalanceClusterVolume());
        statusTimeEditor.setLabel(constants.rebalanceStatusTime());
        statusLabel.setText(constants.rebalanceComplete());
        statusLabel.setVisible(false);
    }

    void initEditors(ApplicationConstants constants) {
        rebalanceHostsTable = new EntityModelCellTable<ListModel>(false, true);

        rebalanceHostsTable.addEntityModelColumn(new EntityModelTextColumn<GlusterVolumeTaskStatusForHost>() {
            @Override
            protected String getText(GlusterVolumeTaskStatusForHost entity) {
                return entity.getHostName();
            }
        }, constants.rebalanceSessionHost());

        rebalanceHostsTable.addEntityModelColumn(new EntityModelTextColumn<GlusterVolumeTaskStatusForHost>() {
            @Override
            protected String getText(GlusterVolumeTaskStatusForHost entity) {
                return entity.getFilesMoved() + "";
            }
        }, constants.rebalanceFileCount());

        rebalanceHostsTable.addEntityModelColumn(new RebalanceFileSizeColumn<EntityModel>(messages) {

            @Override
            protected Long getRawValue(EntityModel object) {
                return ((GlusterVolumeTaskStatusForHost)(object.getEntity())).getTotalSizeMoved();
            }
        }, constants.rebalanceSize());

        rebalanceHostsTable.addEntityModelColumn(new EntityModelTextColumn<GlusterVolumeTaskStatusForHost>() {
            @Override
            protected String getText(GlusterVolumeTaskStatusForHost entity) {
                return String.valueOf(entity.getFilesFailed());
            }
        }, constants.rebalanceFailedFileCount());

        rebalanceHostsTable.addEntityModelColumn(new EntityModelTextColumn<GlusterVolumeTaskStatusForHost>() {
            @Override
            protected String getText(GlusterVolumeTaskStatusForHost entity) {
                return String.valueOf(entity.getFilesSkipped());
            }
        }, constants.rebalanceSkippedFileCount());

        rebalanceHostsTable.addEntityModelColumn(new EntityModelTextColumn<GlusterVolumeTaskStatusForHost>() {

            @Override
            protected String getText(GlusterVolumeTaskStatusForHost entity) {
                return String.valueOf(entity.getFilesScanned());
            }
        }, constants.rebalanceScannedFileCount());

        rebalanceHostsTable.addEntityModelColumn(new EntityModelTextColumn<GlusterVolumeTaskStatusForHost>() {
            @Override
            protected String getText(GlusterVolumeTaskStatusForHost entity) {
                return String.valueOf(entity.getRunTime());
            }
        }, constants.rebalanceRunTime());

        rebalanceHostsTable.addEntityModelColumn(new EntityModelTextColumn<GlusterVolumeTaskStatusForHost>() {
            @Override
            protected String getText(GlusterVolumeTaskStatusForHost entity) {
                return entity.getStatus().toString();
            }
        }, constants.rebalanceStatus());
    }

    @Override
    public void edit(final VolumeRebalanceStatusModel object) {
        driver.edit(object);
        rebalanceHostsTable.asEditor().edit(object.getRebalanceSessions());

        object.getPropertyChangedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                PropertyChangedEventArgs e = (PropertyChangedEventArgs) args;
                if(e.PropertyName.equals("IS_STATUS_APPLICABLE")) {//$NON-NLS-1$
                    statusLabel.setVisible(object.isStatusAvailable());
                }
            }
        });
    }

    @Override
    public VolumeRebalanceStatusModel flush() {
        return driver.flush();
    }

    @Override
    public void setMessage(String message) {
        super.setMessage(message);
        messageLabel.setText(message);
    }
}
