package org.ovirt.engine.ui.webadmin.section.main.view.popup.qos;

import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.qos.CpuQos;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.idhandler.WithElementId;
import org.ovirt.engine.ui.common.view.popup.AbstractModelBoundPopupView;
import org.ovirt.engine.ui.common.widget.dialog.SimpleDialogPanel;
import org.ovirt.engine.ui.common.widget.editor.ListModelListBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.generic.IntegerEntityModelTextBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.generic.StringEntityModelTextBoxEditor;
import org.ovirt.engine.ui.common.widget.renderer.NullSafeRenderer;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.qos.CpuQosParametersModel;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.qos.QosModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.CpuQosPopupPresenterWidget;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.inject.Inject;

public class CpuQosPopupView extends AbstractModelBoundPopupView<QosModel<CpuQos, CpuQosParametersModel>>
        implements CpuQosPopupPresenterWidget.ViewDef {

    @UiField(provided = true)
    @Path(value = "dataCenters.selectedItem")
    @WithElementId
    ListModelListBoxEditor<StoragePool> dataCenterEditor;

    @UiField
    @Path(value = "name.entity")
    @WithElementId
    StringEntityModelTextBoxEditor nameEditor;

    @UiField
    @Path(value = "description.entity")
    @WithElementId
    StringEntityModelTextBoxEditor descriptionEditor;

    @UiField
    @Path(value = "qosParametersModel.cpuLimit.entity")
    @WithElementId
    IntegerEntityModelTextBoxEditor cpuLimitEditor;

    interface Driver extends SimpleBeanEditorDriver<QosModel<CpuQos, CpuQosParametersModel>, CpuQosPopupView> {
    }

    interface ViewUiBinder extends UiBinder<SimpleDialogPanel, CpuQosPopupView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    interface ViewIdHandler extends ElementIdHandler<CpuQosPopupView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    private final Driver driver = GWT.create(Driver.class);

    @Inject
    public CpuQosPopupView(EventBus eventBus, ApplicationResources resources, ApplicationConstants constants) {
        super(eventBus, resources);
        initListBoxEditors();
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        ViewIdHandler.idHandler.generateAndSetIds(this);

        localize(constants);
        driver.initialize(this);
    }

    private void initListBoxEditors() {
        dataCenterEditor = new ListModelListBoxEditor<StoragePool>(new NullSafeRenderer<StoragePool>() {
            @Override
            public String renderNullSafe(StoragePool dataCenter) {
                return dataCenter.getName();
            }
        });
    }

    private void localize(ApplicationConstants constants) {
        nameEditor.setLabel(constants.storageQosName());
        descriptionEditor.setLabel(constants.storageQosDescription());
        dataCenterEditor.setLabel(constants.dataCenterQosPopup());
        cpuLimitEditor.setLabel(constants.cpuQosCpuLimit());
    }

    @Override
    public void edit(QosModel<CpuQos, CpuQosParametersModel> object) {
        driver.edit(object);
    }

    @Override
    public QosModel<CpuQos, CpuQosParametersModel> flush() {
        return driver.flush();
    }

}
