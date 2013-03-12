package org.ovirt.engine.ui.common.widget.uicommon.vm;

import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.uicommon.model.ModelProvider;
import org.ovirt.engine.ui.common.widget.form.FormItem;
import org.ovirt.engine.ui.common.widget.label.TextBoxLabel;
import org.ovirt.engine.ui.common.widget.uicommon.AbstractModelBoundFormWidget;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmSessionsModel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;

public class VmSessionsModelForm extends AbstractModelBoundFormWidget<VmSessionsModel> {

    interface Driver extends SimpleBeanEditorDriver<VmSessionsModel, VmSessionsModelForm> {
    }

    private final Driver driver = GWT.create(Driver.class);

    TextBoxLabel guestUserName = new TextBoxLabel();
    TextBoxLabel consoleUserName = new TextBoxLabel();

    public VmSessionsModelForm(ModelProvider<VmSessionsModel> modelProvider, CommonApplicationConstants constants) {
        super(modelProvider, 2, 2);

        driver.initialize(this);

        formBuilder.setColumnsWidth("120px", "240px", "160px"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        formBuilder.addFormItem(new FormItem(constants.loggedInUserVm(), guestUserName, 0, 0));
        formBuilder.addFormItem(new FormItem(constants.consoleConnectedUserVm(), consoleUserName, 0, 1));
    }

    @Override
    protected void doEdit(VmSessionsModel model) {
        driver.edit(model);
    }

}
