package org.ovirt.engine.ui.webadmin.section.main.view.popup.qos;

import org.ovirt.engine.core.common.businessentities.qos.CpuQos;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.idhandler.WithElementId;
import org.ovirt.engine.ui.common.widget.editor.generic.IntegerEntityModelTextBoxEditor;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.qos.CpuQosParametersModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;

public class CpuQosWidget extends QosWidget<CpuQos, CpuQosParametersModel> {

    interface Driver extends SimpleBeanEditorDriver<CpuQosParametersModel, CpuQosWidget> {
    }

    interface ViewUiBinder extends UiBinder<FlowPanel, CpuQosWidget> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    interface ViewIdHandler extends ElementIdHandler<CpuQosWidget> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    @UiField
    @Path(value = "cpuLimit.entity")
    @WithElementId
    IntegerEntityModelTextBoxEditor cpuLimitEditor;

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    public CpuQosWidget() {
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        ViewIdHandler.idHandler.generateAndSetIds(this);

        cpuLimitEditor.setLabel(constants.cpuQosCpuLimit());

        driver = GWT.create(Driver.class);
        driver.initialize(this);
    }

}
