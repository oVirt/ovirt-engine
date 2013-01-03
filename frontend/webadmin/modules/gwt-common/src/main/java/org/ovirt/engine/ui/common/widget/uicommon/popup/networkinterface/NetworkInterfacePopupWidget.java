package org.ovirt.engine.ui.common.widget.uicommon.popup.networkinterface;

import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.CommonApplicationResources;
import org.ovirt.engine.ui.common.CommonApplicationTemplates;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.idhandler.WithElementId;
import org.ovirt.engine.ui.common.widget.Align;
import org.ovirt.engine.ui.common.widget.dialog.AdvancedParametersExpander;
import org.ovirt.engine.ui.common.widget.editor.EntityModelCheckBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.EntityModelRadioButtonEditor;
import org.ovirt.engine.ui.common.widget.editor.EntityModelTextBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.ListModelListBoxEditor;
import org.ovirt.engine.ui.common.widget.renderer.EnumRenderer;
import org.ovirt.engine.ui.common.widget.renderer.NullSafeRenderer;
import org.ovirt.engine.ui.common.widget.uicommon.popup.AbstractModelBoundPopupWidget;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmInterfaceModel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;

public class NetworkInterfacePopupWidget extends AbstractModelBoundPopupWidget<VmInterfaceModel> {

    interface Driver extends SimpleBeanEditorDriver<VmInterfaceModel, NetworkInterfacePopupWidget> {
        Driver driver = GWT.create(Driver.class);
    }

    interface ViewUiBinder extends UiBinder<FlowPanel, NetworkInterfacePopupWidget> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    interface ViewIdHandler extends ElementIdHandler<NetworkInterfacePopupWidget> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    protected interface Style extends CssResource {
        String cardStatusEditorContent();

        String cardStatusRadioContent();

        String linkStateEditorContent();

        String linkStateRadioContent();

        String checkBox();

        String portMirroringEditor();
    }

    @UiField
    protected Style style;

    @UiField
    @Path("name.entity")
    @WithElementId("name")
    EntityModelTextBoxEditor nameEditor;

    @UiField(provided = true)
    @Path("network.selectedItem")
    @WithElementId("network")
    ListModelListBoxEditor<Object> networkEditor;

    @UiField(provided = true)
    @Path("nicType.selectedItem")
    @WithElementId("nicType")
    ListModelListBoxEditor<Object> nicTypeEditor;

    @UiField
    @Path("portMirroring.entity")
    @WithElementId("portMirroring")
    protected EntityModelCheckBoxEditor portMirroringEditor;

    @UiField(provided = true)
    @Path("enableMac.entity")
    @WithElementId("enableManualMac")
    EntityModelCheckBoxEditor enableManualMacCheckbox;

    @UiField
    @Path("MAC.entity")
    @WithElementId("mac")
    EntityModelTextBoxEditor MACEditor;

    @UiField
    @Ignore
    Label macExample;

    @UiField
    protected HorizontalPanel cardStatusSelectionPanel;

    @UiField
    @Path(value = "plugged.entity")
    public ListModelListBoxEditor<Object> cardStatusEditor;

    @UiField(provided = true)
    @Path(value = "plugged_IsSelected.entity")
    public EntityModelRadioButtonEditor pluggedEditor;

    @UiField(provided = true)
    @Path(value = "unplugged_IsSelected.entity")
    public EntityModelRadioButtonEditor unpluggedEditor;

    @UiField
    protected HorizontalPanel linkStateSelectionPanel;

    @UiField
    @Path(value = "linked.entity")
    public ListModelListBoxEditor<Object> linkStateEditor;

    @UiField(provided = true)
    @Path(value = "linked_IsSelected.entity")
    public EntityModelRadioButtonEditor linkedEditor;

    @UiField(provided = true)
    @Path(value = "unlinked_IsSelected.entity")
    public EntityModelRadioButtonEditor unlinkedEditor;

    public static CommonApplicationTemplates templates = GWT.create(CommonApplicationTemplates.class);
    public static CommonApplicationResources resources = GWT.create(CommonApplicationResources.class);

    @UiField
    @Ignore
    public AdvancedParametersExpander expander;

    @UiField
    @Ignore
    public Panel expanderContent;

    public NetworkInterfacePopupWidget(EventBus eventBus, CommonApplicationConstants constants) {
        initManualWidgets();
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        expander.initWithContent(expanderContent.getElement());
        localize(constants);
        applyStyles();
        ViewIdHandler.idHandler.generateAndSetIds(this);
        Driver.driver.initialize(this);
    }

    private void localize(CommonApplicationConstants constants) {
        nameEditor.setLabel(constants.nameNetworkIntefacePopup());
        networkEditor.setLabel(constants.networkNetworkIntefacePopup());
        nicTypeEditor.setLabel(constants.typeNetworkIntefacePopup());
        enableManualMacCheckbox.setLabel(constants.specipyCustMacNetworkIntefacePopup());
        portMirroringEditor.setLabel(constants.portMirroringNetworkIntefacePopup());

        cardStatusEditor.setLabel(constants.cardStatusNetworkInteface());
        pluggedEditor.asRadioButton()
                .setHTML(templates.imageTextCardStatus(SafeHtmlUtils.fromTrustedString(AbstractImagePrototype.create(resources.pluggedNetworkImage())
                        .getHTML()),
                        constants.pluggedNetworkInteface()));
        unpluggedEditor.asRadioButton()
                .setHTML(templates.imageTextCardStatus(SafeHtmlUtils.fromTrustedString(AbstractImagePrototype.create(resources.unpluggedNetworkImage())
                        .getHTML()),
                        constants.unpluggedNetworkInteface()));

        linkStateEditor.setLabel(constants.linkStateNetworkInteface());
        linkedEditor.asRadioButton()
                .setHTML(templates.imageTextCardStatus(SafeHtmlUtils.fromTrustedString(AbstractImagePrototype.create(resources.linkedNetworkImage())
                        .getHTML()),
                        constants.linkedNetworkInteface()));
        unlinkedEditor.asRadioButton()
                .setHTML(templates.imageTextCardStatus(SafeHtmlUtils.fromTrustedString(AbstractImagePrototype.create(resources.unlinkedNetworkImage())
                        .getHTML()),
                        constants.unlinkedNetworkInteface()));
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private void initManualWidgets() {
        networkEditor = new ListModelListBoxEditor<Object>(new NullSafeRenderer<Object>() {
            @Override
            public String renderNullSafe(Object object) {
                return ((Network) object).getName();
            }
        });

        nicTypeEditor = new ListModelListBoxEditor<Object>(new EnumRenderer());

        pluggedEditor = new EntityModelRadioButtonEditor("cardStatus"); //$NON-NLS-1$
        unpluggedEditor = new EntityModelRadioButtonEditor("cardStatus"); //$NON-NLS-1$

        linkedEditor = new EntityModelRadioButtonEditor("linkState"); //$NON-NLS-1$
        unlinkedEditor = new EntityModelRadioButtonEditor("linkState"); //$NON-NLS-1$

        enableManualMacCheckbox = new EntityModelCheckBoxEditor(Align.RIGHT);
    }

    @Override
    public void focusInput() {
        nameEditor.setFocus(true);
    }

    @Override
    public void edit(final VmInterfaceModel iface) {
        Driver.driver.edit(iface);

        hideMacWhenNotEnabled(iface);
    }

    private void hideMacWhenNotEnabled(VmInterfaceModel iface) {
        if (!iface.getMAC().getIsAvailable()) {
            enableManualMacCheckbox.setVisible(false);
            MACEditor.setVisible(false);
            macExample.setVisible(false);
        }
    }

    @Override
    public VmInterfaceModel flush() {
        return Driver.driver.flush();
    }

    private void applyStyles() {
        cardStatusEditor.addContentWidgetStyleName(style.cardStatusEditorContent());
        pluggedEditor.addContentWidgetStyleName(style.cardStatusRadioContent());
        unpluggedEditor.addContentWidgetStyleName(style.cardStatusRadioContent());

        linkStateEditor.addContentWidgetStyleName(style.linkStateEditorContent());
        linkedEditor.addContentWidgetStyleName(style.linkStateRadioContent());
        unlinkedEditor.addContentWidgetStyleName(style.linkStateRadioContent());

        enableManualMacCheckbox.addContentWidgetStyleName(style.checkBox());

        portMirroringEditor.addContentWidgetStyleName(style.portMirroringEditor());
    }
}
