package org.ovirt.engine.ui.common.widget.uicommon.popup.instancetypes;

import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.gin.AssetProvider;
import org.ovirt.engine.ui.common.uicommon.model.ModelProvider;
import org.ovirt.engine.ui.common.widget.form.FormItem;
import org.ovirt.engine.ui.common.widget.label.TextBoxLabel;
import org.ovirt.engine.ui.common.widget.uicommon.AbstractModelBoundFormWidget;
import org.ovirt.engine.ui.uicommonweb.models.configure.instancetypes.InstanceTypeGeneralModel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;

public class InstanceTypeGeneralModelForm extends AbstractModelBoundFormWidget<InstanceTypeGeneralModel> {

    interface Driver extends SimpleBeanEditorDriver<InstanceTypeGeneralModel, InstanceTypeGeneralModelForm> {
    }

    TextBoxLabel name = new TextBoxLabel();
    TextBoxLabel description = new TextBoxLabel();

    private final Driver driver = GWT.create(Driver.class);

    private final static CommonApplicationConstants constants = AssetProvider.getConstants();

    public InstanceTypeGeneralModelForm(ModelProvider<InstanceTypeGeneralModel> modelProvider) {
        super(modelProvider, 1, 2);
        driver.initialize(this);

        formBuilder.addFormItem(new FormItem(constants.nameInstanceTypeGeneral(), name, 0, 0));
        formBuilder.addFormItem(new FormItem(constants.descriptionInstanceTypeGeneral(), description, 1, 0));
    }

    @Override
    protected void doEdit(InstanceTypeGeneralModel model) {
        driver.edit(model);
    }
}
