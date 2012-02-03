package org.ovirt.engine.ui.userportal.section.main.view.tab.basic;

import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.VmOsType;
import org.ovirt.engine.ui.common.view.AbstractView;
import org.ovirt.engine.ui.uicommonweb.models.userportal.UserPortalBasicListModel;
import org.ovirt.engine.ui.userportal.ApplicationResources;
import org.ovirt.engine.ui.userportal.section.main.presenter.tab.basic.MainTabBasicDetailsPresenterWidget;
import org.ovirt.engine.ui.userportal.section.main.view.tab.basic.widget.DisksImageWidget;
import org.ovirt.engine.ui.userportal.section.main.view.tab.basic.widget.OsTypeImage;
import org.ovirt.engine.ui.userportal.section.main.view.tab.basic.widget.UserPortalEntityModelLabel;
import org.ovirt.engine.ui.userportal.section.main.view.tab.basic.widget.VmTypeImage;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.text.shared.AbstractRenderer;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ValueLabel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class MainTabBasicDetailsView extends AbstractView implements MainTabBasicDetailsPresenterWidget.ViewDef {

    @UiField
    @Path("SelectedItem.OsType")
    OsTypeImage osImage;

    @UiField
    @Path("SelectedItem")
    VmTypeImage vmImage;

    @UiField
    @Path("SelectedItem.Name")
    Label vmName;

    @UiField
    @Path("SelectedItem.Description")
    Label vmDescription;

    @UiField(provided = true)
    @Path("SelectedItem.OsType")
    ValueLabel<VmOsType> operatingSystem;

    @UiField
    @Path("SelectedItemDefinedMemory.Entity")
    UserPortalEntityModelLabel memory;

    @UiField
    @Path("SelectedItemNumOfCpuCores.Entity")
    UserPortalEntityModelLabel numberOfCores;

    @UiField
    @Ignore
    DisksImageWidget disks;

    @UiField
    @Ignore
    Label protocolMessage;

    @UiField
    @Ignore
    HTML editProtocol;

    @UiField
    Style style;

    public interface Style extends CssResource {
        String protocolWarning();

        String protocol();
    }

    interface ViewUiBinder extends UiBinder<Widget, MainTabBasicDetailsView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    interface Driver extends SimpleBeanEditorDriver<UserPortalBasicListModel, MainTabBasicDetailsView> {
        Driver driver = GWT.create(Driver.class);
    }

    @Inject
    public MainTabBasicDetailsView(ApplicationResources resources) {
        operatingSystem = new ValueLabel<VmOsType>(new AbstractRenderer<VmOsType>() {
            @Override
            public String render(VmOsType object) {
                return object.name();
            }
        });

        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));

        Driver.driver.initialize(this);

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
    public void setEditEnabled(boolean enabled) {
        editProtocol.setText(" (Edit)");
        // TODO
    }

}
