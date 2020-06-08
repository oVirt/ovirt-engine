package org.ovirt.engine.ui.webadmin.section.main.view.popup.gluster;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.gwtbootstrap3.client.ui.Alert;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeSnapshotScheduleRecurrence;
import org.ovirt.engine.core.compat.DayOfWeek;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.ui.common.editor.UiCommonEditorDriver;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.idhandler.WithElementId;
import org.ovirt.engine.ui.common.view.popup.AbstractModelBoundPopupView;
import org.ovirt.engine.ui.common.widget.EntityModelWidgetWithInfo;
import org.ovirt.engine.ui.common.widget.dialog.SimpleDialogPanel;
import org.ovirt.engine.ui.common.widget.dialog.tab.DialogTab;
import org.ovirt.engine.ui.common.widget.dialog.tab.DialogTabPanel;
import org.ovirt.engine.ui.common.widget.editor.EntityModelDateTimeBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.GwtBootstrapDateTimePicker;
import org.ovirt.engine.ui.common.widget.editor.ListModelCheckBoxGroupEditor;
import org.ovirt.engine.ui.common.widget.editor.ListModelDaysOfMonthSelectorEditor;
import org.ovirt.engine.ui.common.widget.editor.ListModelListBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.ListModelRadioGroupEditor;
import org.ovirt.engine.ui.common.widget.editor.generic.StringEntityModelLabelEditor;
import org.ovirt.engine.ui.common.widget.editor.generic.StringEntityModelTextBoxEditor;
import org.ovirt.engine.ui.common.widget.label.EnableableFormLabel;
import org.ovirt.engine.ui.uicommonweb.models.gluster.GlusterVolumeSnapshotModel;
import org.ovirt.engine.ui.uicommonweb.models.gluster.GlusterVolumeSnapshotModel.EndDateOptions;
import org.ovirt.engine.ui.uicompat.EnumTranslator;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationTemplates;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.gluster.GlusterVolumeSnapshotCreatePopupPresenterWidget;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.text.shared.AbstractRenderer;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Label;
import com.google.inject.Inject;

public class GlusterVolumeSnapshotCreatePopupView extends
        AbstractModelBoundPopupView<GlusterVolumeSnapshotModel> implements
        GlusterVolumeSnapshotCreatePopupPresenterWidget.ViewDef {

    interface Driver extends UiCommonEditorDriver<GlusterVolumeSnapshotModel, GlusterVolumeSnapshotCreatePopupView> {
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
    DialogTabPanel tabContainer;

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

    @UiField(provided = true)
    @Ignore
    public EntityModelWidgetWithInfo snapshotNameEditorWithInfo;

    @Path(value = "snapshotName.entity")
    @WithElementId
    StringEntityModelTextBoxEditor snapshotNameEditor;

    @UiField
    @Path(value = "description.entity")
    @WithElementId
    StringEntityModelTextBoxEditor snapshotDescriptionEditor;

    @UiField
    @Ignore
    @WithElementId
    Alert generalTabErrorMsg;

    @UiField
    DialogTab scheduleTab;

    @UiField(provided = true)
    @Path(value = "recurrence.selectedItem")
    @WithElementId
    ListModelListBoxEditor<GlusterVolumeSnapshotScheduleRecurrence> recurrenceEditor;

    @UiField
    @Path(value = "timeZones.selectedItem")
    @WithElementId
    ListModelListBoxEditor<Map.Entry<String, String>> timeZoneEditor;

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
    Alert scheduleTabErrorMessage;

    @UiField
    @Ignore
    @WithElementId
    Label disableCliScheduleMessageLabel;

    private final Driver driver = GWT.create(Driver.class);

    private static final ApplicationTemplates templates = AssetProvider.getTemplates();
    private static final ApplicationConstants constants = AssetProvider.getConstants();

    @Inject
    public GlusterVolumeSnapshotCreatePopupView(EventBus eventBus) {
        super(eventBus);
        initEditors();
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        ViewIdHandler.idHandler.generateAndSetIds(this);
        driver.initialize(this);
        daysOfWeekEditor.asCheckBoxGroup().clearAllSelections();
        asWidget().setModal(false);
    }

    private void initEditors() {
        snapshotNameEditor = new StringEntityModelTextBoxEditor();
        snapshotNameEditor.hideLabel();
        EnableableFormLabel label = new EnableableFormLabel();
        label.setText(constants.volumeSnapshotNamePrefixLabel());
        snapshotNameEditorWithInfo = new EntityModelWidgetWithInfo(label, snapshotNameEditor);
        snapshotNameEditorWithInfo.setExplanation(templates.italicText(constants.snapshotNameInfo()));

        startAtEditor = new EntityModelDateTimeBoxEditor();
        startAtEditor.getContentWidget().setDateTimeFormat(GwtBootstrapDateTimePicker.DEFAULT_DATE_TIME_FORMAT);
        startAtEditor.getContentWidget().showDateAndTime();
        daysOfWeekEditor = new ListModelCheckBoxGroupEditor<>(new AbstractRenderer<DayOfWeek>() {
            @Override
            public String render(DayOfWeek object) {
                return object.toString().substring(0, 3);
            }
        });
        endDate = new EntityModelDateTimeBoxEditor();
        endDate.getContentWidget().setDateTimeFormat(GwtBootstrapDateTimePicker.DEFAULT_DATE_TIME_FORMAT);
        endDate.getContentWidget().showDateAndTime();
        executionTimeEditor = new EntityModelDateTimeBoxEditor();
        executionTimeEditor.getContentWidget().setDateTimeFormat("hh:ii");//$NON-NLS-1$
        executionTimeEditor.getContentWidget().showTimeOnly();
        recurrenceEditor =
                new ListModelListBoxEditor<>(new AbstractRenderer<GlusterVolumeSnapshotScheduleRecurrence>() {
                    @Override
                    public String render(GlusterVolumeSnapshotScheduleRecurrence object) {
                        return EnumTranslator.getInstance().translate(object);
                    }
        });
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
        timeZoneEditor.setVisible(recurrenceOption != GlusterVolumeSnapshotScheduleRecurrence.UNKNOWN);
        daysOfWeekEditor.setVisible(recurrenceOption == GlusterVolumeSnapshotScheduleRecurrence.WEEKLY);
        daysOfMonthEditor.setVisible(recurrenceOption == GlusterVolumeSnapshotScheduleRecurrence.MONTHLY);
        startAtEditor.setVisible(recurrenceOption != GlusterVolumeSnapshotScheduleRecurrence.UNKNOWN);
        executionTimeEditor.setVisible(recurrenceOption == GlusterVolumeSnapshotScheduleRecurrence.DAILY
                || recurrenceOption == GlusterVolumeSnapshotScheduleRecurrence.WEEKLY
                || recurrenceOption == GlusterVolumeSnapshotScheduleRecurrence.MONTHLY);
        disableCliScheduleMessageLabel.setVisible(object.getDisableCliSchedule().getEntity()
                && recurrenceOption != GlusterVolumeSnapshotScheduleRecurrence.UNKNOWN);

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

    public void setMessage(String msg, Alert alert) {
        alert.setText(msg);
        alert.setVisible(StringHelper.isNotNullOrEmpty(msg));
    }

    @Override
    public void handleValidationErrors(GlusterVolumeSnapshotModel object) {
        String generalTabErrors = collectGeneralTabErrors(object);
        setMessage(generalTabErrors, generalTabErrorMsg);

        String scheduleTabErrors = collectScheduleTabErrors(object);
        setMessage(scheduleTabErrors, scheduleTabErrorMessage);
    }

    private String collectScheduleTabErrors(GlusterVolumeSnapshotModel object) {
        StringBuilder scheduleTabErrors = new StringBuilder();
        if (!daysOfWeekEditor.isValid()) {
            appendErrors(object.getDaysOfTheWeek().getInvalidityReasons(), scheduleTabErrors);
        }
        if (!daysOfMonthEditor.isValid()) {
            appendErrors(object.getDaysOfMonth().getInvalidityReasons(), scheduleTabErrors);
        }
        if (!endDate.isValid()) {
            appendErrors(object.getEndDate().getInvalidityReasons(), scheduleTabErrors);
        }
        return scheduleTabErrors.toString();
    }

    private String collectGeneralTabErrors(GlusterVolumeSnapshotModel object) {
        StringBuilder generalTabErrorBuilder = new StringBuilder();
        if (!snapshotNameEditor.isValid()){
            appendErrors(Collections.singletonList(constants.volumeSnapshotNamePrefixLabel()), generalTabErrorBuilder);
            appendErrors(object.getSnapshotName().getInvalidityReasons(), generalTabErrorBuilder);
        }
        return generalTabErrorBuilder.toString();
    }

    @Override
    public void switchTabBasedOnEditorInvalidity() {
        if (!clusterNameEditor.isValid() || !volumeNameEditor.isValid() || !snapshotNameEditor.isValid()
                || !snapshotDescriptionEditor.isValid()) {
            tabContainer.switchTab(generalTab);
        } else if (!recurrenceEditor.isValid() || !intervalEditor.isValid() || !timeZoneEditor.isValid()
                || !startAtEditor.isValid() || !executionTimeEditor.isValid() || !daysOfWeekEditor.isValid()
                || !daysOfMonthEditor.isValid() || !endByOptionsEditor.isValid() || !endDate.isValid()) {
            tabContainer.switchTab(scheduleTab);
        }
    }

    private void appendErrors(List<String> errors, StringBuilder sBuilder) {
        for(String currentError : errors) {
            sBuilder.append(currentError);
            sBuilder.append("\n");//$NON-NLS-1$
        }
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

    @Override
    public void cleanup() {
        driver.cleanup();
    }
}
