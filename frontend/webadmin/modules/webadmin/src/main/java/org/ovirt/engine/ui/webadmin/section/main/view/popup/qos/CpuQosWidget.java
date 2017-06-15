package org.ovirt.engine.ui.webadmin.section.main.view.popup.qos;

import org.ovirt.engine.core.common.businessentities.qos.CpuQos;
import org.ovirt.engine.ui.common.editor.UiCommonEditorDriver;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.idhandler.WithElementId;
import org.ovirt.engine.ui.common.widget.editor.generic.IntegerEntityModelTextBoxEditor;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.qos.CpuQosParametersModel;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;

public class CpuQosWidget extends QosWidget<CpuQos, CpuQosParametersModel> {

    interface Driver extends UiCommonEditorDriver<CpuQosParametersModel, CpuQosWidget> {
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

    public CpuQosWidget() {
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        ViewIdHandler.idHandler.generateAndSetIds(this);

        driver = GWT.create(Driver.class);
        driver.initialize(this);
    }

}
