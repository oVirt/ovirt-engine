package org.ovirt.engine.ui.webadmin.section.main.view.popup.gluster;

import java.util.Date;

import org.ovirt.engine.core.common.businessentities.gluster.GlusterGeoRepSessionDetails;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.idhandler.WithElementId;
import org.ovirt.engine.ui.common.view.popup.AbstractModelBoundPopupView;
import org.ovirt.engine.ui.common.widget.dialog.SimpleDialogPanel;
import org.ovirt.engine.ui.common.widget.editor.EntityModelCellTable;
import org.ovirt.engine.ui.common.widget.editor.generic.EntityModelLabelEditor;
import org.ovirt.engine.ui.common.widget.renderer.FullDateTimeRenderer;
import org.ovirt.engine.ui.common.widget.table.column.AbstractEntityModelTextColumn;
import org.ovirt.engine.ui.common.widget.table.column.AbstractFullDateTimeColumn;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.gluster.VolumeGeoRepSessionDetailsModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationMessages;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.gluster.VolumeGeoRepSessionDetailsPopUpPresenterWidget;
import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.text.shared.AbstractRenderer;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Label;
import com.google.inject.Inject;

public class VolumeGeoRepSessionDetailsPopUpView extends AbstractModelBoundPopupView<VolumeGeoRepSessionDetailsModel> implements VolumeGeoRepSessionDetailsPopUpPresenterWidget.ViewDef{

    interface Driver extends SimpleBeanEditorDriver<VolumeGeoRepSessionDetailsModel, VolumeGeoRepSessionDetailsPopUpView> {
    }

    interface ViewUiBinder extends UiBinder<SimpleDialogPanel, VolumeGeoRepSessionDetailsPopUpView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    interface ViewIdHandler extends ElementIdHandler<VolumeGeoRepSessionDetailsPopUpView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    @UiField(provided = true)
    @Ignore
    @WithElementId
    EntityModelCellTable<ListModel<EntityModel<GlusterGeoRepSessionDetails>>> geoRepSessionSummaryTable;

    @UiField
    @Ignore
    @WithElementId
    Label georepSessionDetailsHeader;

    @UiField(provided = true)
    @Ignore
    @WithElementId
    EntityModelLabelEditor<GlusterGeoRepSessionDetails> checkPointStatus;

    @UiField(provided = true)
    @Ignore
    @WithElementId
    EntityModelLabelEditor<GlusterGeoRepSessionDetails> crawlStatus;

    @UiField(provided = true)
    @Ignore
    @WithElementId
    EntityModelLabelEditor<GlusterGeoRepSessionDetails> dataOpsPending;

    @UiField(provided = true)
    @Ignore
    @WithElementId
    EntityModelLabelEditor<GlusterGeoRepSessionDetails> metaOpsPending;

    @UiField(provided = true)
    @Ignore
    @WithElementId
    EntityModelLabelEditor<GlusterGeoRepSessionDetails> entryOpsPending;

    @UiField(provided = true)
    @Ignore
    @WithElementId
    EntityModelLabelEditor<GlusterGeoRepSessionDetails> failures;

    @UiField(provided = true)
    @Ignore
    @WithElementId
    EntityModelLabelEditor<GlusterGeoRepSessionDetails> checkPointTime;

    @UiField(provided = true)
    @Ignore
    @WithElementId
    EntityModelLabelEditor<GlusterGeoRepSessionDetails> checkPointCompletedAt;

    ApplicationResources resources;
    ApplicationConstants constants;
    ApplicationMessages messages;

    private final Driver driver = GWT.create(Driver.class);

    @Inject
    public VolumeGeoRepSessionDetailsPopUpView(EventBus eventBus, ApplicationResources resources, ApplicationConstants constants, ApplicationMessages messages) {
        super(eventBus);
        this.resources = resources;
        this.constants = constants;
        this.messages = messages;
        intiEditors(constants);
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        ViewIdHandler.idHandler.generateAndSetIds(this);
        localise();
        driver.initialize(this);
    }

    private void localise() {
        checkPointStatus.setLabel(constants.geoRepCheckPointStatus());
        crawlStatus.setLabel(constants.georepCrawlStatus());
        dataOpsPending.setLabel(constants.geoRepDataOpsPending());
        metaOpsPending.setLabel(constants.geoRepMetaOpsPending());
        entryOpsPending.setLabel(constants.geoRepEntryOpsPending());
        failures.setLabel(constants.geoRepFailures());
        georepSessionDetailsHeader.setText(constants.geoRepSessionDetailHeader());
        checkPointTime.setLabel(constants.geoRepCheckPointTime());
        checkPointCompletedAt.setLabel(constants.geoRepCheckPointCompletedAt());
    }

    private void intiEditors(final ApplicationConstants constants) {
        checkPointStatus =
                new EntityModelLabelEditor<>(new AbstractRenderer<GlusterGeoRepSessionDetails>() {
                    @Override
                    public String render(GlusterGeoRepSessionDetails object) {
                        String checkPointStatusValue = object.getCheckPointStatus();
                        return checkPointStatusValue == null || checkPointStatusValue.isEmpty() ? constants.notAvailableLabel()
                                : checkPointStatusValue;
                    }
                });
        crawlStatus = new EntityModelLabelEditor<>(new AbstractRenderer<GlusterGeoRepSessionDetails>() {
            @Override
            public String render(GlusterGeoRepSessionDetails object) {
                return object.getCrawlStatus().toString();
            }
        });
        dataOpsPending = new EntityModelLabelEditor<>(new AbstractRenderer<GlusterGeoRepSessionDetails>() {
            @Override
            public String render(GlusterGeoRepSessionDetails object) {
                return object.getDataOpsPending().toString();
            }
        });
        metaOpsPending = new EntityModelLabelEditor<>(new AbstractRenderer<GlusterGeoRepSessionDetails>() {
            @Override
            public String render(GlusterGeoRepSessionDetails object) {
                return object.getMetaOpsPending().toString();
            }
        });
        entryOpsPending = new EntityModelLabelEditor<>(new AbstractRenderer<GlusterGeoRepSessionDetails>() {
            @Override
            public String render(GlusterGeoRepSessionDetails object) {
                return object.getEntryOpsPending().toString();
            }
        });
        failures = new EntityModelLabelEditor<>(new AbstractRenderer<GlusterGeoRepSessionDetails>() {
            @Override
            public String render(GlusterGeoRepSessionDetails object) {
                return object.getFailures().toString();
            }
        });

        checkPointTime = new EntityModelLabelEditor<>(new AbstractRenderer<GlusterGeoRepSessionDetails>() {
            @Override
            public String render(GlusterGeoRepSessionDetails object) {
                return new FullDateTimeRenderer().render(object.getCheckPointTime());
            }
        });

        checkPointCompletedAt = new EntityModelLabelEditor<>(new AbstractRenderer<GlusterGeoRepSessionDetails>() {
            @Override
            public String render(GlusterGeoRepSessionDetails object) {
                return new FullDateTimeRenderer().render(object.getCheckPointCompletedAt());
            }
        });

        geoRepSessionSummaryTable = new EntityModelCellTable<>(false, true);

        geoRepSessionSummaryTable.addColumn(new AbstractEntityModelTextColumn<GlusterGeoRepSessionDetails>() {
            @Override
            public String getText(GlusterGeoRepSessionDetails object) {
                return object.getMasterBrickHostName() == null ? constants.notAvailableLabel() : object.getMasterBrickHostName();
            }
        }, constants.geoRepSessionHostName());
        geoRepSessionSummaryTable.addColumn(new AbstractEntityModelTextColumn<GlusterGeoRepSessionDetails>() {
            @Override
            protected String getText(GlusterGeoRepSessionDetails entity) {
                return (entity == null || entity.getStatus() == null) ? constants.notAvailableLabel() : entity.getStatus().toString();
            }
        }, constants.geoRepSessionStatus());
        geoRepSessionSummaryTable.addColumn(new AbstractFullDateTimeColumn<EntityModel<GlusterGeoRepSessionDetails>>() {
            @Override
            protected Date getRawValue(EntityModel<GlusterGeoRepSessionDetails> object) {
                GlusterGeoRepSessionDetails sessionDetail = object.getEntity();
                return (sessionDetail == null || sessionDetail.getLastSyncedAt() == null) ? new Date() : sessionDetail.getLastSyncedAt();
            }
        }, constants.geoRepLastSyncedAt());

    }

    @Override
    public void setCheckPointCompletedAtVisibility(boolean visible)  {
        checkPointCompletedAt.setVisible(visible);
    }

    @Override
    public void updateSessionDetailProperties(GlusterGeoRepSessionDetails selectedSessionDetail) {
        checkPointStatus.asValueBox().setValue(selectedSessionDetail);
        crawlStatus.asValueBox().setValue(selectedSessionDetail);
        dataOpsPending.asValueBox().setValue(selectedSessionDetail);
        metaOpsPending.asValueBox().setValue(selectedSessionDetail);
        entryOpsPending.asValueBox().setValue(selectedSessionDetail);
        failures.asValueBox().setValue(selectedSessionDetail);
        checkPointTime.asValueBox().setValue(selectedSessionDetail);
        checkPointCompletedAt.asValueBox().setValue(selectedSessionDetail);
    }

    @Override
    public void edit(final VolumeGeoRepSessionDetailsModel object) {
        driver.edit(object);

        geoRepSessionSummaryTable.asEditor().edit(object.getGeoRepSessionSummary());
    }

    @Override
    public VolumeGeoRepSessionDetailsModel flush() {
        return driver.flush();
    }

}
