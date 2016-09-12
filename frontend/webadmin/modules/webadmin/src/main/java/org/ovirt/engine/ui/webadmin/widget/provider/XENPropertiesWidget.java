package org.ovirt.engine.ui.webadmin.widget.provider;

import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.ui.common.editor.UiCommonEditorDriver;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.idhandler.WithElementId;
import org.ovirt.engine.ui.common.widget.editor.ListModelListBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.generic.StringEntityModelTextBoxEditor;
import org.ovirt.engine.ui.common.widget.uicommon.popup.AbstractModelBoundPopupWidget;
import org.ovirt.engine.ui.uicommonweb.models.providers.XENPropertiesModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;

import com.google.gwt.core.client.GWT;
import com.google.gwt.text.shared.AbstractRenderer;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.inject.Inject;

public class XENPropertiesWidget extends AbstractModelBoundPopupWidget<XENPropertiesModel> {

    interface Driver extends UiCommonEditorDriver<XENPropertiesModel, XENPropertiesWidget> {
    }

    private final Driver driver = GWT.create(Driver.class);

    interface ViewUiBinder extends UiBinder<FlowPanel, XENPropertiesWidget> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    interface ViewIdHandler extends ElementIdHandler<XENPropertiesWidget> {
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
    public XENPropertiesWidget() {
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
        urlEditor.setLabel(constants.xenUri());
        proxyHostEditor.setLabel(constants.proxyHost());
    }

    @Override
    public void edit(XENPropertiesModel object) {
        driver.edit(object);
    }

    @Override
    public XENPropertiesModel flush() {
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
