package org.ovirt.engine.ui.webadmin.section.main.view.popup.provider;

import org.ovirt.engine.core.common.businessentities.network.ExternalSubnet.IpVersion;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.idhandler.WithElementId;
import org.ovirt.engine.ui.common.view.popup.AbstractModelBoundPopupView;
import org.ovirt.engine.ui.common.widget.dialog.SimpleDialogPanel;
import org.ovirt.engine.ui.common.widget.editor.ListModelListBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.generic.StringEntityModelLabelEditor;
import org.ovirt.engine.ui.common.widget.editor.generic.StringEntityModelTextBoxEditor;
import org.ovirt.engine.ui.common.widget.renderer.EnumRenderer;
import org.ovirt.engine.ui.uicommonweb.models.providers.NewExternalSubnetModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.provider.ExternalSubnetPopupPresenterWidget;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.inject.Inject;

public class ExternalSubnetPopupView extends AbstractModelBoundPopupView<NewExternalSubnetModel> implements ExternalSubnetPopupPresenterWidget.ViewDef {

    interface Driver extends SimpleBeanEditorDriver<NewExternalSubnetModel, ExternalSubnetPopupView> {
    }

    interface ViewUiBinder extends UiBinder<SimpleDialogPanel, ExternalSubnetPopupView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    interface ViewIdHandler extends ElementIdHandler<ExternalSubnetPopupView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    @UiField
    @Path("name.entity")
    @WithElementId("name")
    StringEntityModelTextBoxEditor nameEditor;

    @UiField
    @Path("cidr.entity")
    @WithElementId("cidr")
    StringEntityModelTextBoxEditor cidrEditor;

    @UiField(provided = true)
    @Path("ipVersion.selectedItem")
    @WithElementId("ipVersion")
    ListModelListBoxEditor<IpVersion> ipVersionEditor;

    @UiField
    @Ignore
    StringEntityModelLabelEditor networkEditor;

    private final Driver driver = GWT.create(Driver.class);

    @Inject
    public ExternalSubnetPopupView(EventBus eventBus, ApplicationResources resources, ApplicationConstants constants) {
        super(eventBus, resources);
        ipVersionEditor = new ListModelListBoxEditor<IpVersion>(new EnumRenderer<IpVersion>());
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        localize(constants);
        ViewIdHandler.idHandler.generateAndSetIds(this);
        driver.initialize(this);
    }

    private void localize(ApplicationConstants constants) {
        networkEditor.setLabel(constants.networkExternalSubnet());
        nameEditor.setLabel(constants.nameExternalSubnet());
        cidrEditor.setLabel(constants.cidrExternalSubnet());
        ipVersionEditor.setLabel(constants.ipVersionExternalSubnet());
    }

    @Override
    public void focusInput() {
        nameEditor.setFocus(true);
    }

    @Override
    public void edit(final NewExternalSubnetModel subnet) {
        driver.edit(subnet);
        networkEditor.asValueBox().setValue(subnet.getNetwork().getEntity().getName());
    }

    @Override
    public NewExternalSubnetModel flush() {
        return driver.flush();
    }
}
