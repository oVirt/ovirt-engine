package org.ovirt.engine.ui.userportal.section.main.view.tab.basic;

import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.idhandler.WithElementId;
import org.ovirt.engine.ui.common.view.AbstractView;
import org.ovirt.engine.ui.common.widget.tooltip.WidgetTooltip;
import org.ovirt.engine.ui.uicommonweb.DynamicMessages;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.userportal.UserPortalBasicListModel;
import org.ovirt.engine.ui.userportal.ApplicationTemplates;
import org.ovirt.engine.ui.userportal.gin.AssetProvider;
import org.ovirt.engine.ui.userportal.section.main.presenter.tab.basic.MainTabBasicDetailsPresenterWidget;
import org.ovirt.engine.ui.userportal.widget.ToStringEntityModelLabel;
import org.ovirt.engine.ui.userportal.widget.basic.DisksImageWidget;
import org.ovirt.engine.ui.userportal.widget.basic.IconImage;
import org.ovirt.engine.ui.userportal.widget.basic.VmTypeImage;
import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.text.shared.AbstractRenderer;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ValueLabel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class MainTabBasicDetailsView extends AbstractView implements MainTabBasicDetailsPresenterWidget.ViewDef {

    interface ViewUiBinder extends UiBinder<Widget, MainTabBasicDetailsView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    interface Driver extends SimpleBeanEditorDriver<UserPortalBasicListModel, MainTabBasicDetailsView> {
    }

    interface ViewIdHandler extends ElementIdHandler<MainTabBasicDetailsView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    public interface Style extends CssResource {
        String protocolWarning();
        String protocol();
        String basicInfoDetailsLinkDisabled();
        String basicInfoDetailsLink();
    }

    private static final ApplicationTemplates templates = AssetProvider.getTemplates();

    @UiField
    @Path("selectedItem.smallIconId")
    IconImage smallIconImage;

    @UiField
    @Path("selectedItem")
    VmTypeImage vmImage;

    @UiField
    @Path("selectedItem.name")
    @WithElementId("name")
    Label vmName;

    @UiField
    @Path("selectedItem.description")
    @WithElementId("description")
    Label vmDescription;

    @UiField(provided = true)
    @Path("selectedItem.osId")
    @WithElementId("os")
    ValueLabel<Integer> operatingSystem;

    @UiField
    @Path("selectedItemDefinedMemory.entity")
    @WithElementId
    ToStringEntityModelLabel memory;

    @UiField
    @Path("selectedItemNumOfCpuCores.entity")
    @WithElementId
    ToStringEntityModelLabel numberOfCores;

    @UiField
    WidgetTooltip numberOfCoresTooltip;

    @UiField
    @Ignore
    @WithElementId
    DisksImageWidget disks;

    @UiField
    @Ignore
    @WithElementId("protocol")
    Label protocolMessage;

    @UiField
    @Ignore
    @WithElementId
    Anchor consoleConnectAnchor;

    @UiField
    @Ignore
    @WithElementId
    Anchor editProtocolLink;

    @UiField(provided = true)
    @Ignore
    @WithElementId
    Anchor consoleClientResourcesUrl;

    @UiField
    Style style;

    private final Driver driver = GWT.create(Driver.class);

    @Inject
    public MainTabBasicDetailsView(final DynamicMessages dynamicMessages) {
        operatingSystem = new ValueLabel<>(new AbstractRenderer<Integer>() {

            @Override
            public String render(Integer object) {
                return AsyncDataProvider.getInstance().getOsName(object);
            }
        });
        consoleClientResourcesUrl = new Anchor(dynamicMessages.clientResources());
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        driver.initialize(this);
        ViewIdHandler.idHandler.generateAndSetIds(this);

        initToolTips();
    }

    private void initToolTips() {
        numberOfCoresTooltip.setHtml(templates.numOfCpuCoresTooltip());
    }

    @Override
    public void edit(UserPortalBasicListModel model) {
        driver.edit(model);
    }

    @Override
    public UserPortalBasicListModel flush() {
        return driver.flush();
    }

    @Override
    public void editDistItems(Iterable<DiskImage> diskImages) {
        disks.setValue(diskImages);
    }

    @Override
    public void setConsoleProtocolMessage(String message) {
        protocolMessage.setText(message);
        protocolMessage.setStyleName(style.protocolWarning());
    }

    @Override
    public void setConsoleConnectLinkEnabled(boolean enabled) {
        if (enabled) {
            consoleConnectAnchor.setStyleName(style.basicInfoDetailsLink());
        } else {
            consoleConnectAnchor.setStyleName(style.basicInfoDetailsLinkDisabled());
        }
    }

    @Override
    public void setEditConsoleEnabled(boolean enabled) {
        if (enabled) {
            editProtocolLink.setStyleName(style.basicInfoDetailsLink());
        } else {
            editProtocolLink.setStyleName(style.basicInfoDetailsLinkDisabled());
        }
    }

    @Override
    public void displayVmOsImages(boolean dispaly) {
        smallIconImage.setVisible(dispaly);
        vmImage.setVisible(dispaly);
    }

    @Override
    public HasClickHandlers getEditButton() {
        return editProtocolLink;
    }

    @Override
    public HasClickHandlers getConsoleClientResourcesAnchor() {
        return consoleClientResourcesUrl;
    }

    public HasClickHandlers getConsoleConnectAnchor() {
        return consoleConnectAnchor;
    }

    @Override
    public void clear() {
        vmName.setText(null);
        vmDescription.setText(null);
        protocolMessage.setText(null);
        memory.setValue(null);
        numberOfCores.setValue(null);
        operatingSystem.getElement().setInnerHTML(""); //$NON-NLS-1$
        disks.clear();
        displayVmOsImages(false);
    }
}
