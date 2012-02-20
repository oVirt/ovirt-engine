package org.ovirt.engine.ui.userportal.section.main.view.tab.extended.vm;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.ui.common.view.AbstractSubTabFormView;
import org.ovirt.engine.ui.common.widget.form.FormBuilder;
import org.ovirt.engine.ui.common.widget.form.FormItem;
import org.ovirt.engine.ui.common.widget.form.GeneralFormPanel;
import org.ovirt.engine.ui.common.widget.label.TextBoxLabel;
import org.ovirt.engine.ui.uicommonweb.models.userportal.UserPortalItemModel;
import org.ovirt.engine.ui.uicommonweb.models.userportal.UserPortalListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmGeneralModel;
import org.ovirt.engine.ui.userportal.section.main.presenter.tab.extended.vm.SubTabExtendedVmGeneralPresenter;
import org.ovirt.engine.ui.userportal.uicommon.model.vm.VmGeneralModelProvider;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.Editor;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class SubTabExtendedVmGeneralView extends AbstractSubTabFormView<UserPortalItemModel, UserPortalListModel, VmGeneralModel>
        implements SubTabExtendedVmGeneralPresenter.ViewDef, Editor<VmGeneralModel> {

    interface ViewUiBinder extends UiBinder<Widget, SubTabExtendedVmGeneralView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    interface Driver extends SimpleBeanEditorDriver<VmGeneralModel, SubTabExtendedVmGeneralView> {
        Driver driver = GWT.create(Driver.class);
    }

    TextBoxLabel name = new TextBoxLabel();

    @UiField(provided = true)
    GeneralFormPanel formPanel;

    FormBuilder formBuilder;

    @Inject
    public SubTabExtendedVmGeneralView(VmGeneralModelProvider modelProvider) {
        super(modelProvider);

        formPanel = new GeneralFormPanel();
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        Driver.driver.initialize(this);

        formBuilder = new FormBuilder(formPanel, 3, 6);
        formBuilder.setColumnsWidth("120px", "240px", "160px");
        formBuilder.addFormItem(new FormItem("Name", name, 0, 0));
        // TODO add more items, consult with SubTabVirtualMachineGeneralView
    }

    @Override
    public void setMainTabSelectedItem(UserPortalItemModel selectedItem) {
        update();
    }

    @Override
    public void editVm(VM entity) {
        update();
    }

    void update() {
        Driver.driver.edit(getDetailModel());
        formBuilder.showForm(getDetailModel());
    }

}
