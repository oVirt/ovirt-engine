package org.ovirt.engine.ui.webadmin.widget.provider;

import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.ui.common.editor.UiCommonEditorDriver;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.idhandler.WithElementId;
import org.ovirt.engine.ui.common.widget.editor.ListModelListBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.generic.EntityModelCheckBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.generic.StringEntityModelTextBoxEditor;
import org.ovirt.engine.ui.common.widget.uicommon.popup.AbstractModelBoundPopupWidget;
import org.ovirt.engine.ui.uicommonweb.models.providers.VmwarePropertiesModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;

import com.google.gwt.core.client.GWT;
import com.google.gwt.text.shared.AbstractRenderer;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.inject.Inject;

public class VmwarePropertiesWidget extends AbstractModelBoundPopupWidget<VmwarePropertiesModel> {

    interface Driver extends UiCommonEditorDriver<VmwarePropertiesModel, VmwarePropertiesWidget> {
    }

    private final Driver driver = GWT.create(Driver.class);

    interface ViewUiBinder extends UiBinder<FlowPanel, VmwarePropertiesWidget> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    interface ViewIdHandler extends ElementIdHandler<VmwarePropertiesWidget> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    @UiField
    @Path("vCenter.entity")
    @WithElementId("vCenter")
    StringEntityModelTextBoxEditor vCenterEditor;

    @UiField
    @Path("esx.entity")
    @WithElementId("esx")
    StringEntityModelTextBoxEditor esxEditor;

    @UiField
    @Path("vmwareDatacenter.entity")
    @WithElementId("vmwareDatacenter")
    StringEntityModelTextBoxEditor vmwareDatacenterEditor;

    @UiField
    @Path("vmwareCluster.entity")
    @WithElementId("vmwareCluster")
    StringEntityModelTextBoxEditor vmwareClusterEditor;

    @UiField(provided = true)
    @Path("proxyHost.selectedItem")
    @WithElementId("proxyHost")
    ListModelListBoxEditor<VDS> proxyHostEditor;

    @UiField
    @Path("verifySSL.entity")
    @WithElementId
    EntityModelCheckBoxEditor verifySSLEditor;

    @Inject
    public VmwarePropertiesWidget() {
        proxyHostEditor = new ListModelListBoxEditor<>(new AbstractRenderer<VDS>() {
            @Override
            public String render(VDS object) {
                return object != null ? object.getName() :
                    ConstantsManager.getInstance().getConstants().anyHostInDataCenter();
            }
        });
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        ViewIdHandler.idHandler.generateAndSetIds(this);

        localize();
        driver.initialize(this);
    }

    void localize() {
        vCenterEditor.setLabel(constants.vCenter());
        esxEditor.setLabel(constants.esxi());
        vmwareDatacenterEditor.setLabel(constants.vmwareDataCenter());
        vmwareClusterEditor.setLabel(constants.vmwareCluster());
        proxyHostEditor.setLabel(constants.proxyHost());
        verifySSLEditor.setLabel(constants.vmwareVerifyServerSslCert());
    }

    @Override
    public void edit(VmwarePropertiesModel object) {
        driver.edit(object);
    }

    @Override
    public VmwarePropertiesModel flush() {
        return driver.flush();
    }

    @Override
    public void cleanup() {
        driver.cleanup();
    }

    @Override
    public int setTabIndexes(int nextTabIndex) {
        vCenterEditor.setTabIndex(nextTabIndex++);
        esxEditor.setTabIndex(nextTabIndex++);
        vmwareDatacenterEditor.setTabIndex(nextTabIndex++);
        vmwareClusterEditor.setTabIndexes(nextTabIndex++);
        verifySSLEditor.setTabIndex(nextTabIndex++);
        proxyHostEditor.setTabIndex(nextTabIndex++);
        return nextTabIndex;
    }
}
