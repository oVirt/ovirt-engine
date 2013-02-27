package org.ovirt.engine.ui.userportal.section.main.view.tab.basic;

import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.VmOsType;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.idhandler.WithElementId;
import org.ovirt.engine.ui.common.view.AbstractView;
import org.ovirt.engine.ui.common.widget.label.EnumLabel;
import org.ovirt.engine.ui.uicommonweb.models.userportal.UserPortalBasicListModel;
import org.ovirt.engine.ui.userportal.ApplicationResources;
import org.ovirt.engine.ui.userportal.section.main.presenter.tab.basic.MainTabBasicDetailsPresenterWidget;
import org.ovirt.engine.ui.userportal.widget.ToStringEntityModelLabel;
import org.ovirt.engine.ui.userportal.widget.basic.DisksImageWidget;
import org.ovirt.engine.ui.userportal.widget.basic.OsTypeImage;
import org.ovirt.engine.ui.userportal.widget.basic.VmTypeImage;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class MainTabBasicDetailsView extends AbstractView implements MainTabBasicDetailsPresenterWidget.ViewDef {

    interface ViewUiBinder extends UiBinder<Widget, MainTabBasicDetailsView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    interface Driver extends SimpleBeanEditorDriver<UserPortalBasicListModel, MainTabBasicDetailsView> {
        Driver driver = GWT.create(Driver.class);
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

    @UiField
    @Path("SelectedItem.OsType")
    OsTypeImage osImage;

    @UiField
    @Path("SelectedItem")
    VmTypeImage vmImage;

    @UiField
    @Path("SelectedItem.Name")
    @WithElementId("name")
    Label vmName;

    @UiField
    @Path("SelectedItem.Description")
    @WithElementId("description")
    Label vmDescription;

    @UiField
    @Path("SelectedItem.OsType")
    @WithElementId("os")
    EnumLabel<VmOsType> operatingSystem;

    @UiField
    @Path("SelectedItemDefinedMemory.Entity")
    @WithElementId
    ToStringEntityModelLabel memory;

    @UiField
    @Path("SelectedItemNumOfCpuCores.Entity")
    @WithElementId
    ToStringEntityModelLabel numberOfCores;

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
    Anchor editProtocolLink;

    @UiField
    Style style;

    @Inject
    public MainTabBasicDetailsView(ApplicationResources resources) {
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        Driver.driver.initialize(this);
        ViewIdHandler.idHandler.generateAndSetIds(this);
    }

    @Override
    public void edit(UserPortalBasicListModel model) {
        Driver.driver.edit(model);
    }

    @Override
    public UserPortalBasicListModel flush() {
        return Driver.driver.flush();
    }

    @Override
    public void editDistItems(Iterable<DiskImage> diskImages) {
        disks.setValue(diskImages);
    }

    @Override
    public void setConsoleWarningMessage(String message) {
        protocolMessage.setText(message);
        protocolMessage.setStyleName(style.protocolWarning());
    }

    @Override
    public void setConsoleProtocol(String protocolName) {
        protocolMessage.setText(protocolName);
        protocolMessage.setStyleName(style.protocolWarning());
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
        osImage.setVisible(dispaly);
        vmImage.setVisible(dispaly);
    }

    @Override
    public HasClickHandlers getEditButton() {
        return editProtocolLink;
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
