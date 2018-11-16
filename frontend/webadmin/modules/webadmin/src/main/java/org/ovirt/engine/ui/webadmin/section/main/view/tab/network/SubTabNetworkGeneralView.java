package org.ovirt.engine.ui.webadmin.section.main.view.tab.network;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.network.NetworkView;
import org.ovirt.engine.core.common.mode.ApplicationMode;
import org.ovirt.engine.ui.common.editor.UiCommonEditorDriver;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.idhandler.WithElementId;
import org.ovirt.engine.ui.common.uicommon.model.DetailModelProvider;
import org.ovirt.engine.ui.common.view.AbstractSubTabFormView;
import org.ovirt.engine.ui.common.widget.form.FormBuilder;
import org.ovirt.engine.ui.common.widget.form.FormItem;
import org.ovirt.engine.ui.common.widget.form.GeneralFormPanel;
import org.ovirt.engine.ui.common.widget.label.BooleanLabel;
import org.ovirt.engine.ui.common.widget.label.GuidLabel;
import org.ovirt.engine.ui.common.widget.label.StringValueLabel;
import org.ovirt.engine.ui.common.widget.renderer.EmptyValueRenderer;
import org.ovirt.engine.ui.uicommonweb.models.ApplicationModeHelper;
import org.ovirt.engine.ui.uicommonweb.models.networks.NetworkGeneralModel;
import org.ovirt.engine.ui.uicommonweb.models.networks.NetworkListModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.network.SubTabNetworkGeneralPresenter;
import org.ovirt.engine.ui.webadmin.widget.renderer.MtuRenderer;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.Editor;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.ValueLabel;
import com.google.gwt.user.client.ui.Widget;

public class SubTabNetworkGeneralView extends AbstractSubTabFormView<NetworkView, NetworkListModel, NetworkGeneralModel> implements SubTabNetworkGeneralPresenter.ViewDef, Editor<NetworkGeneralModel> {

    interface ViewUiBinder extends UiBinder<Widget, SubTabNetworkGeneralView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    interface ViewIdHandler extends ElementIdHandler<SubTabNetworkGeneralView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    interface Driver extends UiCommonEditorDriver<NetworkGeneralModel, SubTabNetworkGeneralView> {
    }

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    StringValueLabel name = new StringValueLabel();
    GuidLabel id = new GuidLabel();
    StringValueLabel description = new StringValueLabel();
    BooleanLabel vmNetwork = new BooleanLabel(constants.trueVmNetwork(), constants.falseVmNetwork());
    ValueLabel<Integer> vlan = new ValueLabel<>(new EmptyValueRenderer<Integer>(constants.noneVlan()));
    ValueLabel<Integer> mtu = new ValueLabel<>(new MtuRenderer());
    StringValueLabel externalId = new StringValueLabel();
    StringValueLabel vdsmName = new StringValueLabel();

    @UiField(provided = true)
    @WithElementId
    GeneralFormPanel formPanel;

    FormBuilder formBuilder;

    private final Driver driver = GWT.create(Driver.class);

    @Inject
    public SubTabNetworkGeneralView(DetailModelProvider<NetworkListModel, NetworkGeneralModel> modelProvider) {
        super(modelProvider);

        // Init formPanel
        formPanel = new GeneralFormPanel();

        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        driver.initialize(this);

        generateIds();

        // Build a form using the FormBuilder
        formBuilder = new FormBuilder(formPanel, 2, 5);
        formBuilder.setRelativeColumnWidth(0, 5);
        formBuilder.setRelativeColumnWidth(1, 5);
        formBuilder.addFormItem(new FormItem(constants.nameNetwork(), name, 0, 0), 3, 9);
        formBuilder.addFormItem(new FormItem(constants.idNetwork(), id, 1, 0), 3, 9);
        formBuilder.addFormItem(new FormItem(constants.descriptionNetwork(), description, 2, 0), 3, 9);
        formBuilder.addFormItem(new FormItem(constants.vdsmName(), vdsmName, 3, 0), 3, 9);

        formBuilder.addFormItem(new FormItem(constants.vmNetwork(), vmNetwork, 0, 1) {
            @Override
            public boolean getIsAvailable() {
                return ApplicationModeHelper.isModeSupported(ApplicationMode.VirtOnly);
            }
        }, 3, 9);

        formBuilder.addFormItem(new FormItem(constants.vlanNetwork(), vlan, 1, 1) {
            @Override
            public boolean getIsAvailable() {
                return ApplicationModeHelper.isModeSupported(ApplicationMode.VirtOnly);
            }
        }, 3, 9);

        formBuilder.addFormItem(new FormItem(constants.mtuNetwork(), mtu, 2, 1) {
            @Override
            public boolean getIsAvailable() {
                return getDetailModel().getExternalId() == null;
            }
        }, 3, 9);

        formBuilder.addFormItem(new FormItem(constants.externalIdProviderNetwork(), externalId, 4, 0) {
            @Override
            public boolean getIsAvailable() {
                return getDetailModel().getExternalId() != null;
            }
        }, 3, 9);
    }

    @Override
    protected void generateIds() {
        ViewIdHandler.idHandler.generateAndSetIds(this);
    }

    @Override
    public void setMainSelectedItem(NetworkView selectedItem) {
        driver.edit(getDetailModel());
        formBuilder.update(getDetailModel());
    }

}
