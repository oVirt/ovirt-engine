package org.ovirt.engine.ui.webadmin.widget.provider;

import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.ui.common.editor.UiCommonEditorDriver;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.idhandler.WithElementId;
import org.ovirt.engine.ui.common.widget.editor.ListModelListBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.generic.StringEntityModelTextBoxEditor;
import org.ovirt.engine.ui.common.widget.uicommon.popup.AbstractModelBoundPopupWidget;
import org.ovirt.engine.ui.uicommonweb.models.providers.KVMPropertiesModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;

import com.google.gwt.core.client.GWT;
import com.google.gwt.text.shared.AbstractRenderer;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.inject.Inject;

public class KVMPropertiesWidget extends AbstractModelBoundPopupWidget<KVMPropertiesModel> {

    interface Driver extends UiCommonEditorDriver<KVMPropertiesModel, KVMPropertiesWidget> {
    }

    private final Driver driver = GWT.create(Driver.class);

    interface ViewUiBinder extends UiBinder<FlowPanel, KVMPropertiesWidget> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    interface ViewIdHandler extends ElementIdHandler<KVMPropertiesWidget> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    @UiField
    @Path("url.entity")
    @WithElementId("url")
    StringEntityModelTextBoxEditor urlEditor;

    @UiField(provided = true)
    @Path("proxyHost.selectedItem")
    @WithElementId("proxyHost")
    ListModelListBoxEditor<VDS> proxyHostEditor;

    @Inject
    public KVMPropertiesWidget() {
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
        urlEditor.setLabel(constants.kvmUri());
        proxyHostEditor.setLabel(constants.proxyHost());
    }

    @Override
    public void edit(KVMPropertiesModel object) {
        driver.edit(object);
    }

    @Override
    public KVMPropertiesModel flush() {
        return driver.flush();
    }

    @Override
    public void cleanup() {
        driver.cleanup();
    }

    @Override
    public int setTabIndexes(int nextTabIndex) {
        urlEditor.setTabIndex(nextTabIndex++);
        proxyHostEditor.setTabIndex(nextTabIndex++);
        return nextTabIndex;
    }
}
