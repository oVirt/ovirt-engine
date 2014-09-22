package org.ovirt.engine.ui.webadmin.section.main.view.popup.qos;

import org.ovirt.engine.core.common.businessentities.qos.StorageQos;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.idhandler.WithElementId;
import org.ovirt.engine.ui.common.widget.Align;
import org.ovirt.engine.ui.common.widget.editor.generic.EntityModelCheckBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.generic.IntegerEntityModelTextBoxOnlyEditor;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.qos.StorageQosParametersModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;

public class StorageQosWidget extends QosWidget<StorageQos, StorageQosParametersModel> {

    interface Driver extends SimpleBeanEditorDriver<StorageQosParametersModel, StorageQosWidget> {
    }

    interface ViewUiBinder extends UiBinder<FlowPanel, StorageQosWidget> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    interface ViewIdHandler extends ElementIdHandler<StorageQosWidget> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    interface WidgetStyle extends CssResource {
        String valueWidth();
    }

    @UiField
    WidgetStyle style;

    @UiField(provided = true)
    @Path(value = "throughput.enabled.entity")
    @WithElementId
    EntityModelCheckBoxEditor throughputEnabled;

    @UiField(provided = true)
    @Path(value = "iops.enabled.entity")
    @WithElementId
    EntityModelCheckBoxEditor iopsEnabled;

    @UiField
    @Path(value = "throughput.total.entity")
    @WithElementId
    IntegerEntityModelTextBoxOnlyEditor throughputTotalEditor;

    @UiField
    @Path(value = "throughput.read.entity")
    @WithElementId
    IntegerEntityModelTextBoxOnlyEditor throughputReadEditor;

    @UiField
    @Path(value = "throughput.write.entity")
    @WithElementId
    IntegerEntityModelTextBoxOnlyEditor throughputWriteEditor;

    @UiField
    @Path(value = "iops.total.entity")
    @WithElementId
    IntegerEntityModelTextBoxOnlyEditor iopsTotalEditor;

    @UiField
    @Path(value = "iops.read.entity")
    @WithElementId
    IntegerEntityModelTextBoxOnlyEditor iopsReadEditor;

    @UiField
    @Path(value = "iops.write.entity")
    @WithElementId
    IntegerEntityModelTextBoxOnlyEditor iopsWriteEditor;

    public StorageQosWidget(ApplicationConstants constants) {
        throughputEnabled = new EntityModelCheckBoxEditor(Align.RIGHT);
        iopsEnabled = new EntityModelCheckBoxEditor(Align.RIGHT);
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        ViewIdHandler.idHandler.generateAndSetIds(this);

        setStyle();
        localize(constants);

        driver = GWT.create(Driver.class);
        driver.initialize(this);
    }

    private void setStyle() {
        throughputTotalEditor.setContentWidgetStyleName(style.valueWidth());
        throughputReadEditor.setContentWidgetStyleName(style.valueWidth());
        throughputWriteEditor.setContentWidgetStyleName(style.valueWidth());
        iopsTotalEditor.setContentWidgetStyleName(style.valueWidth());
        iopsReadEditor.setContentWidgetStyleName(style.valueWidth());
        iopsWriteEditor.setContentWidgetStyleName(style.valueWidth());
    }

    private void localize(ApplicationConstants constants) {
        throughputEnabled.setLabel(constants.throughputLabelQosPopup());
        iopsEnabled.setLabel(constants.iopsLabelQosPopup());
        throughputTotalEditor.setTitle(constants.totalStorageQosPopup() + constants.mbpsLabelStorageQosPopup());
        throughputReadEditor.setTitle(constants.readStorageQosPopup() + constants.mbpsLabelStorageQosPopup());
        throughputWriteEditor.setTitle(constants.writeStorageQosPopup() + constants.mbpsLabelStorageQosPopup());
        iopsTotalEditor.setTitle(constants.totalStorageQosPopup() + constants.iopsCountLabelQosPopup());
        iopsReadEditor.setTitle(constants.readStorageQosPopup() + constants.iopsCountLabelQosPopup());
        iopsWriteEditor.setTitle(constants.writeStorageQosPopup() + constants.iopsCountLabelQosPopup());
    }

}
