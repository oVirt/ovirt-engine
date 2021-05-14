package org.ovirt.engine.ui.webadmin.section.main.view.popup.storage;

import org.ovirt.engine.core.common.businessentities.gluster.GlusterGeoRepSession;
import org.ovirt.engine.core.common.businessentities.gluster.StorageSyncSchedule;
import org.ovirt.engine.ui.common.editor.UiCommonEditorDriver;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.idhandler.WithElementId;
import org.ovirt.engine.ui.common.view.popup.AbstractModelBoundPopupView;
import org.ovirt.engine.ui.common.widget.dialog.SimpleDialogPanel;
import org.ovirt.engine.ui.common.widget.editor.ListModelCheckBoxGroupEditor;
import org.ovirt.engine.ui.common.widget.editor.ListModelListBoxEditor;
import org.ovirt.engine.ui.common.widget.renderer.NullSafeRenderer;
import org.ovirt.engine.ui.uicommonweb.models.storage.StorageDRModel;
import org.ovirt.engine.ui.uicompat.EnumTranslator;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationMessages;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.storage.StorageDRPopupPresenterWidget;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.text.shared.AbstractRenderer;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Label;
import com.google.inject.Inject;

public class StorageDRPopupView extends
        AbstractModelBoundPopupView<StorageDRModel> implements
        StorageDRPopupPresenterWidget.ViewDef {

    interface Driver extends UiCommonEditorDriver<StorageDRModel, StorageDRPopupView> {
    }

    interface ViewUiBinder extends
            UiBinder<SimpleDialogPanel, StorageDRPopupView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    interface ViewIdHandler extends
            ElementIdHandler<StorageDRPopupView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    @UiField
    WidgetStyle style;


    @UiField
    @Ignore
    @WithElementId
    Label errorMsgLabel;

    @UiField(provided = true)
    @Path(value = "geoRepSession.selectedItem")
    @WithElementId
    ListModelListBoxEditor<GlusterGeoRepSession> geoRepSessionEditor;

    @UiField(provided = true)
    @Path(value = "frequency.selectedItem")
    @WithElementId
    ListModelListBoxEditor<StorageSyncSchedule.Frequency> frequencyEditor;


    @UiField(provided = true)
    @Path(value = "days.selectedItem")
    @WithElementId
    ListModelCheckBoxGroupEditor<StorageSyncSchedule.Day> daysEditor;

    @UiField
    @Path(value = "hour.selectedItem")
    @WithElementId
    ListModelListBoxEditor<Integer> hoursEditor;

    @UiField
    @Path(value = "mins.selectedItem")
    @WithElementId
    ListModelListBoxEditor<Integer> minsEditor;


    private final Driver driver = GWT.create(Driver.class);

    private static final ApplicationConstants constants = AssetProvider.getConstants();
    private static final ApplicationMessages messages = AssetProvider.getMessages();

    @Inject
    public StorageDRPopupView(EventBus eventBus) {
        super(eventBus);
        initEditors();
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        ViewIdHandler.idHandler.generateAndSetIds(this);
        localize();
        setVisibilities();
        driver.initialize(this);
        daysEditor.asCheckBoxGroup().clearAllSelections();
    }

    private void initEditors() {

        daysEditor = new ListModelCheckBoxGroupEditor<>(new AbstractRenderer<StorageSyncSchedule.Day>() {
            @Override
            public String render(StorageSyncSchedule.Day object) {
                return object.toString().substring(0, 3);
            }
        });
        frequencyEditor =
                new ListModelListBoxEditor<>(new AbstractRenderer<StorageSyncSchedule.Frequency>() {
                    @Override
                    public String render(StorageSyncSchedule.Frequency object) {
                        return EnumTranslator.getInstance().translate(object);
                    }
        });
        geoRepSessionEditor = new ListModelListBoxEditor<>(new NullSafeRenderer<GlusterGeoRepSession>() {
            @Override
            protected String renderNullSafe(GlusterGeoRepSession geoRepSession) {
                if (geoRepSession == null) {
                    return "";
                } else {
                    return messages.geoRepRemoteSessionName(geoRepSession.getSlaveHostName(),
                            geoRepSession.getSlaveVolumeName());
                }
            }
        });
        hoursEditor = new ListModelListBoxEditor<>();
        minsEditor = new ListModelListBoxEditor<>();
    }

    private void localize() {
        daysEditor.setLabel(constants.daysOfWeekLabel());
        frequencyEditor.setLabel(constants.recurrenceLabel());
        hoursEditor.setLabel(constants.storageDRHoursLabel());
        minsEditor.setLabel(constants.storageDRMinuteLabel());

        geoRepSessionEditor.setLabel(constants.storageDRGeoRepSessionLabel());
    }

    private void setVisibilities() {
        errorMsgLabel.setVisible(false);
    }

    @Override
    public void edit(final StorageDRModel object) {
        driver.edit(object);

        updateVisibilities(object);
    }

    @Override
    public void updateVisibilities(StorageDRModel object) {
        StorageSyncSchedule.Frequency frequency = object.getFrequency().getSelectedItem();

        daysEditor.setVisible(frequency == StorageSyncSchedule.Frequency.WEEKLY);
        hoursEditor.setVisible(frequency != StorageSyncSchedule.Frequency.NONE);
        minsEditor.setVisible(frequency != StorageSyncSchedule.Frequency.NONE);
    }

    @Override
    public void setMessage(String msg) {
        super.setMessage(msg);
        errorMsgLabel.setText(msg);
        errorMsgLabel.setVisible(true);
    }

    @Override
    public StorageDRModel flush() {
        return driver.flush();
    }

    @Override
    public void cleanup() {
        driver.cleanup();
    }

    interface WidgetStyle extends CssResource {
        String editorContentWidget();
    }
}
