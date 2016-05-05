package org.ovirt.engine.ui.webadmin.section.main.view.popup.profile;

import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkFilter;
import org.ovirt.engine.core.common.businessentities.network.NetworkQoS;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.idhandler.WithElementId;
import org.ovirt.engine.ui.common.view.popup.AbstractModelBoundPopupView;
import org.ovirt.engine.ui.common.widget.Align;
import org.ovirt.engine.ui.common.widget.dialog.SimpleDialogPanel;
import org.ovirt.engine.ui.common.widget.editor.ListModelListBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.generic.EntityModelCheckBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.generic.StringEntityModelTextBoxEditor;
import org.ovirt.engine.ui.common.widget.form.key_value.KeyValueWidget;
import org.ovirt.engine.ui.common.widget.renderer.NameRenderer;
import org.ovirt.engine.ui.common.widget.renderer.NetworkFilterRenderer;
import org.ovirt.engine.ui.uicommonweb.models.profiles.VnicProfileModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.key_value.KeyValueModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.profile.VnicProfilePopupPresenterWidget;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.inject.Inject;

public class VnicProfilePopupView extends AbstractModelBoundPopupView<VnicProfileModel> implements VnicProfilePopupPresenterWidget.ViewDef {

    interface Driver extends SimpleBeanEditorDriver<VnicProfileModel, VnicProfilePopupView> {
    }

    interface ViewUiBinder extends UiBinder<SimpleDialogPanel, VnicProfilePopupView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    interface ViewIdHandler extends ElementIdHandler<VnicProfilePopupView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    protected interface Style extends CssResource {
        String checkboxEditor();

        String checkBox();

        String publicUseEditor();
    }

    @UiField
    protected Style style;

    @UiField
    @Path("name.entity")
    @WithElementId("name")
    StringEntityModelTextBoxEditor nameEditor;

    @UiField
    @Path("description.entity")
    @WithElementId("description")
    StringEntityModelTextBoxEditor descriptionEditor;

    @UiField(provided = true)
    @Path(value = "networkQoS.selectedItem")
    @WithElementId("networkQoS")
    public ListModelListBoxEditor<NetworkQoS> networkQoSEditor;

    @UiField(provided = true)
    @Path(value = "networkFilter.selectedItem")
    @WithElementId("networkFilter")
    public ListModelListBoxEditor<NetworkFilter> networkFilterEditor;

    @UiField
    @Path("passthrough.entity")
    @WithElementId("passthrough")
    protected EntityModelCheckBoxEditor passthroughEditor;

    @UiField
    @Path("portMirroring.entity")
    @WithElementId("portMirroring")
    protected EntityModelCheckBoxEditor portMirroringEditor;

    @UiField(provided = true)
    @Ignore
    public KeyValueWidget<KeyValueModel> customPropertiesSheetEditor;

    @UiField(provided = true)
    @Path(value = "publicUse.entity")
    public final EntityModelCheckBoxEditor publicUseEditor;

    @UiField(provided = true)
    @Path("network.selectedItem")
    ListModelListBoxEditor<Network> networkEditor;

    private final Driver driver = GWT.create(Driver.class);

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    @Inject
    public VnicProfilePopupView(EventBus eventBus) {
        super(eventBus);
        publicUseEditor = new EntityModelCheckBoxEditor(Align.RIGHT);
        customPropertiesSheetEditor = new KeyValueWidget<>("380px"); //$NON-NLS-1$
        networkEditor = new ListModelListBoxEditor<>(new NameRenderer<Network>());
        networkQoSEditor = new ListModelListBoxEditor<>(new NameRenderer<NetworkQoS>());
        networkFilterEditor = new ListModelListBoxEditor<>(new NetworkFilterRenderer(constants));
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        localize();
        applyStyles();
        ViewIdHandler.idHandler.generateAndSetIds(this);
        driver.initialize(this);
    }

    private void localize() {
        networkEditor.setLabel(constants.networkVnicProfile());
        nameEditor.setLabel(constants.nameVnicProfile());
        descriptionEditor.setLabel(constants.descriptionVnicProfile());
        passthroughEditor.setLabel(constants.passthorughVnicProfile());
        portMirroringEditor.setLabel(constants.portMirroringVnicProfile());
        publicUseEditor.setLabel(constants.publicUseVnicProfile());
        networkQoSEditor.setLabel(constants.profileQoSInstanceTypeLabel());
        networkFilterEditor.setLabel(constants.profileNetworkFilterLabel());
    }

    @Override
    public void focusInput() {
        nameEditor.setFocus(true);
    }

    @Override
    public void edit(final VnicProfileModel profile) {
        driver.edit(profile);
        customPropertiesSheetEditor.edit(profile.getCustomPropertySheet());
    }

    @Override
    public VnicProfileModel flush() {
        return driver.flush();
    }

    private void applyStyles() {
        portMirroringEditor.addContentWidgetContainerStyleName(style.checkboxEditor());
        passthroughEditor.addContentWidgetContainerStyleName(style.checkboxEditor());
        publicUseEditor.addContentWidgetContainerStyleName(style.publicUseEditor());
        publicUseEditor.asCheckBox().addStyleName(style.checkBox());
    }
}
