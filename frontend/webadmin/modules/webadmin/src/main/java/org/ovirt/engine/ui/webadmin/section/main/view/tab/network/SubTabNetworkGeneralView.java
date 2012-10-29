package org.ovirt.engine.ui.webadmin.section.main.view.tab.network;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.Network;
import org.ovirt.engine.ui.common.uicommon.model.DetailModelProvider;
import org.ovirt.engine.ui.common.view.AbstractSubTabFormView;
import org.ovirt.engine.ui.common.widget.form.FormBuilder;
import org.ovirt.engine.ui.common.widget.form.FormItem;
import org.ovirt.engine.ui.common.widget.form.GeneralFormPanel;
import org.ovirt.engine.ui.common.widget.label.BooleanLabel;
import org.ovirt.engine.ui.common.widget.label.TextBoxLabel;
import org.ovirt.engine.ui.uicommonweb.models.networks.NetworkGeneralModel;
import org.ovirt.engine.ui.uicommonweb.models.networks.NetworkListModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.ClientGinjectorProvider;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.network.SubTabNetworkGeneralPresenter;
import org.ovirt.engine.ui.webadmin.widget.label.NullableNumberLabel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.Editor;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;

public class SubTabNetworkGeneralView extends AbstractSubTabFormView<Network, NetworkListModel, NetworkGeneralModel> implements SubTabNetworkGeneralPresenter.ViewDef, Editor<NetworkGeneralModel> {

    interface ViewUiBinder extends UiBinder<Widget, SubTabNetworkGeneralView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    interface Driver extends SimpleBeanEditorDriver<NetworkGeneralModel, SubTabNetworkGeneralView> {
        Driver driver = GWT.create(Driver.class);
    }

    TextBoxLabel name = new TextBoxLabel();
    TextBoxLabel description = new TextBoxLabel();
    BooleanLabel vm = new BooleanLabel();
    NullableNumberLabel<Integer> vlan = new  NullableNumberLabel<Integer>();
    NullableNumberLabel<Integer> mtu = new  NullableNumberLabel<Integer>();

    @UiField(provided = true)
    GeneralFormPanel formPanel;

    FormBuilder formBuilder;

    private final ApplicationConstants constants = ClientGinjectorProvider.instance().getApplicationConstants();

    @Inject
    public SubTabNetworkGeneralView(DetailModelProvider<NetworkListModel, NetworkGeneralModel> modelProvider) {
        super(modelProvider);

        // Init formPanel
        formPanel = new GeneralFormPanel();

        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        Driver.driver.initialize(this);

        // Build a form using the FormBuilder
        formBuilder = new FormBuilder(formPanel, 2, 6);

        formBuilder.setColumnsWidth("300px"); //$NON-NLS-1$
        formBuilder.addFormItem(new FormItem(constants.nameNetwork(), name, 0, 0));
        formBuilder.addFormItem(new FormItem(constants.descriptionNetwork(), description, 1, 0));
        formBuilder.addFormItem(new FormItem(constants.vmNetwork(), vm, 0, 1));
        formBuilder.addFormItem(new FormItem(constants.vlanNetwork(), vlan, 1, 1));
        formBuilder.addFormItem(new FormItem(constants.mtuNetwork(), mtu, 2, 1));

    }

    @Override
    public void setMainTabSelectedItem(Network selectedItem) {
        Driver.driver.edit(getDetailModel());
        formBuilder.showForm(getDetailModel());
    }

}

