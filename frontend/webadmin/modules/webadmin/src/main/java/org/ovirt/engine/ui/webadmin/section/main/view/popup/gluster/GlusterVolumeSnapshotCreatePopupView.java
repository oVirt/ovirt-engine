package org.ovirt.engine.ui.webadmin.section.main.view.popup.gluster;

import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeSnapshotScheduleRecurrence;
import org.ovirt.engine.core.compat.DayOfWeek;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.idhandler.WithElementId;
import org.ovirt.engine.ui.common.view.popup.AbstractModelBoundPopupView;
import org.ovirt.engine.ui.common.widget.dialog.InfoIcon;
import org.ovirt.engine.ui.common.widget.dialog.SimpleDialogPanel;
import org.ovirt.engine.ui.common.widget.dialog.tab.DialogTab;
import org.ovirt.engine.ui.common.widget.editor.EntityModelDateTimeBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.ListModelCheckBoxGroupEditor;
import org.ovirt.engine.ui.common.widget.editor.ListModelDaysOfMonthSelectorEditor;
import org.ovirt.engine.ui.common.widget.editor.ListModelListBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.ListModelRadioGroupEditor;
import org.ovirt.engine.ui.common.widget.editor.generic.StringEntityModelLabelEditor;
import org.ovirt.engine.ui.common.widget.editor.generic.StringEntityModelTextBoxEditor;
import org.ovirt.engine.ui.uicommonweb.models.gluster.GlusterVolumeSnapshotModel;
import org.ovirt.engine.ui.uicommonweb.models.gluster.GlusterVolumeSnapshotModel.EndDateOptions;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.ApplicationTemplates;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.gluster.GlusterVolumeSnapshotCreatePopupPresenterWidget;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.text.shared.AbstractRenderer;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Label;
import com.google.inject.Inject;

public class GlusterVolumeSnapshotCreatePopupView extends
        AbstractModelBoundPopupView<GlusterVolumeSnapshotModel> implements
        GlusterVolumeSnapshotCreatePopupPresenterWidget.ViewDef {
    interface Driver
            extends
            SimpleBeanEditorDriver<GlusterVolumeSnapshotModel, GlusterVolumeSnapshotCreatePopupView> {
    }

    interface ViewUiBinder extends
            UiBinder<SimpleDialogPanel, GlusterVolumeSnapshotCreatePopupView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    interface ViewIdHandler extends
            ElementIdHandler<GlusterVolumeSnapshotCreatePopupView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    @UiField
    WidgetStyle style;

    @UiField
    DialogTab generalTab;

    @UiField
    @Path(value = "clusterName.entity")
    @WithElementId
    StringEntityModelLabelEditor clusterNameEditor;

    @UiField
    @Path(value = "volumeName.entity")
    @WithElementId
    StringEntityModelLabelEditor volumeNameEditor;

    @UiField
    @Path(value = "snapshotName.entity")
    @WithElementId
    StringEntityModelTextBoxEditor snapshotNameEditor;

    @UiField(provided = true)
    InfoIcon snapshotNameInfoIcon;

    @UiField
    @Path(value = "description.entity")
    @WithElementId
    StringEntityModelTextBoxEditor snapshotDescriptionEditor;

    @UiField
    DialogTab scheduleTab;

    @UiField(provided = true)
    @Path(value = "recurrence.selectedItem")
    @WithElementId
    ListModelListBoxEditor<GlusterVolumeSnapshotScheduleRecurrence> recurrenceEditor;

    @UiField
    @Path(value = "timeZones.selectedItem")
    @WithElementId
    ListModelListBoxEditor<String> timeZoneEditor;

    @UiField(provided = true)
    @Path(value = "daysOfTheWeek.selectedItem")
    @WithElementId
    ListModelCheckBoxGroupEditor<DayOfWeek> daysOfWeekEditor;

    @UiField
    @Path(value = "interval.selectedItem")
    @WithElementId
    ListModelListBoxEditor<String> intervalEditor;

    @UiField
    @Path(value = "endByOptions.selectedItem")
    @WithElementId
    ListModelRadioGroupEditor<EndDateOptions> endByOptionsEditor;

    @UiField(provided = true)
    @Path(value = "endDate.entity")
    @WithElementId
    EntityModelDateTimeBoxEditor endDate;

    @UiField(provided = true)
    @Path(value = "startAt.entity")
    @WithElementId
    EntityModelDateTimeBoxEditor startAtEditor;

    @UiField(provided = true)
    @Path(value = "executionTime.entity")
    @WithElementId
    EntityModelDateTimeBoxEditor executionTimeEditor;

    @UiField
    @Path(value = "daysOfMonth.selectedItem")
    @WithElementId
    ListModelDaysOfMonthSelectorEditor daysOfMonthEditor;

    @UiField
    @Ignore
    @WithElementId
    Label criticalIntervalLabel;

    @UiField
    @Ignore
    @WithElementId
    Label errorMsgLabel;

    private final ApplicationConstants constants;

    private final Driver driver = GWT.create(Driver.class);

    @Inject
    public GlusterVolumeSnapshotCreatePopupView(EventBus eventBus,
            ApplicationResources resources, ApplicationConstants constants,
            ApplicationTemplates templates) {
        super(eventBus, resources);
        this.constants = constants;
        initEditors(constants, resources, templates);
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        ViewIdHandler.idHandler.generateAndSetIds(this);
        localize();
        setVisibilities();
        driver.initialize(this);
        daysOfWeekEditor.asCheckBoxGroup().clearAllSelections();
    }

    private void initEditors(ApplicationConstants constants,
            ApplicationResources resources, ApplicationTemplates templates) {
        snapshotNameInfoIcon = new InfoIcon(templates.italicText(constants.snapshotNameInfo()), resources);
        startAtEditor = new EntityModelDateTimeBoxEditor();
        daysOfWeekEditor = new ListModelCheckBoxGroupEditor<DayOfWeek>(new AbstractRenderer<DayOfWeek>() {
            @Override
            public String render(DayOfWeek object) {
                return object.toString().substring(0, 3);
            }
        });
        endDate = new EntityModelDateTimeBoxEditor();
        executionTimeEditor = new EntityModelDateTimeBoxEditor();
        executionTimeEditor.getContentWidget().setDateRequired(false);
        recurrenceEditor =
                new ListModelListBoxEditor<GlusterVolumeSnapshotScheduleRecurrence>(new AbstractRenderer<GlusterVolumeSnapshotScheduleRecurrence>() {
                    @Override
                    public String render(GlusterVolumeSnapshotScheduleRecurrence object) {
                        return ConstantsManager.getInstance().getMessages().recurrenceType(object);
                    }
        });
    }

    private void localize() {
        generalTab.setLabel(constants.generalLabel());

        clusterNameEditor.setLabel(constants.volumeClusterLabel());
        volumeNameEditor.setLabel(constants.volumeNameLabel());
        snapshotNameEditor.setLabel(constants.volumeSnapshotNamePrefixLabel());
        snapshotDescriptionEditor.setLabel(constants.volumeSnapshotDescriptionLabel());

        scheduleTab.setLabel(constants.scheduleLabel());

        recurrenceEditor.setLabel(constants.recurrenceLabel());
        intervalEditor.setLabel(constants.intervalLabel());
        endByOptionsEditor.setLabel(constants.endByLabel());

        timeZoneEditor.setLabel(constants.timeZoneLabel());
        daysOfMonthEditor.setLabel(constants.daysOfMonthLabel());
        daysOfWeekEditor.setLabel(constants.daysOfWeekLabel());
        startAtEditor.setLabel(constants.startAtLabel());
        endDate.setLabel(constants.endByDateLabel());
        executionTimeEditor.setLabel(constants.executionTimeLabel());

        criticalIntervalLabel.setText(constants.criticalSnapshotIntervalNote());
    }

    private void setVisibilities() {
        criticalIntervalLabel.setVisible(false);
    }

    @Override
    public void edit(final GlusterVolumeSnapshotModel object) {
        driver.edit(object);
        updateVisibilities(object);
        updateTabVisibilities(object);
    }

    @Override
    public void updateVisibilities(GlusterVolumeSnapshotModel object) {
        GlusterVolumeSnapshotScheduleRecurrence recurrenceOption = object
                .getRecurrence().getSelectedItem();

        intervalEditor.setVisible(recurrenceOption == GlusterVolumeSnapshotScheduleRecurrence.INTERVAL);
        endByOptionsEditor.setVisible(recurrenceOption != GlusterVolumeSnapshotScheduleRecurrence.UNKNOWN);
        timeZoneEditor.setVisible(recurrenceOption == GlusterVolumeSnapshotScheduleRecurrence.DAILY
                || recurrenceOption == GlusterVolumeSnapshotScheduleRecurrence.WEEKLY
                || recurrenceOption == GlusterVolumeSnapshotScheduleRecurrence.MONTHLY);
        daysOfWeekEditor.setVisible(recurrenceOption == GlusterVolumeSnapshotScheduleRecurrence.WEEKLY);
        daysOfMonthEditor.setVisible(recurrenceOption == GlusterVolumeSnapshotScheduleRecurrence.MONTHLY);
        startAtEditor.setVisible(recurrenceOption != GlusterVolumeSnapshotScheduleRecurrence.UNKNOWN);
        executionTimeEditor.setVisible(recurrenceOption == GlusterVolumeSnapshotScheduleRecurrence.DAILY
                || recurrenceOption == GlusterVolumeSnapshotScheduleRecurrence.WEEKLY
                || recurrenceOption == GlusterVolumeSnapshotScheduleRecurrence.MONTHLY);

        setEndDateVisibility(object);
    }

    @Override
    public void setEndDateVisibility(GlusterVolumeSnapshotModel object) {
        endDate.setVisible(object.getRecurrence().getSelectedItem() != GlusterVolumeSnapshotScheduleRecurrence.UNKNOWN
                && object.getEndByOptions().getSelectedItem() == EndDateOptions.HasEndDate);
    }

    @Override
    public void setCriticalIntervalLabelVisibility(GlusterVolumeSnapshotModel object, int interval) {
        if (object.getRecurrence().getSelectedItem() == GlusterVolumeSnapshotScheduleRecurrence.INTERVAL) {
            criticalIntervalLabel.setVisible(interval <= 30);
        } else {
            criticalIntervalLabel.setVisible(false);
        }
    }

    @Override
    public void setMessage(String msg) {
        super.setMessage(msg);
        errorMsgLabel.setText(msg);
    }

    private void updateTabVisibilities(GlusterVolumeSnapshotModel object) {
        generalTab.setVisible(object.isGeneralTabVisible());
        scheduleTab.setVisible(object.isScheduleTabVisible());
        if (object.getRecurrence().getSelectedItem() != GlusterVolumeSnapshotScheduleRecurrence.UNKNOWN) {
            scheduleTab.activate();
        }
    }

    @Override
    public GlusterVolumeSnapshotModel flush() {
        return driver.flush();
    }

    interface WidgetStyle extends CssResource {
        String editorContentWidget();
    }
}
