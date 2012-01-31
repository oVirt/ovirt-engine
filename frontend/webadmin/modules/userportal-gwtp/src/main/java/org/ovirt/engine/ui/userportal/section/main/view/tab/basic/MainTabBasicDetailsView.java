package org.ovirt.engine.ui.userportal.section.main.view.tab.basic;

import org.ovirt.engine.ui.common.view.AbstractView;
import org.ovirt.engine.ui.userportal.ApplicationResources;
import org.ovirt.engine.ui.userportal.section.main.presenter.tab.basic.MainTabBasicDetailsPresenterWidget;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class MainTabBasicDetailsView extends AbstractView implements MainTabBasicDetailsPresenterWidget.ViewDef {

    @UiField
    Image osImage;

    @UiField
    Image vmImage;

    @UiField
    Label vmName;

    @UiField
    Label vmDescription;

    @UiField
    Label operatingSystem;

    @UiField
    Label memory;

    @UiField
    Label numberOfCores;

    interface ViewUiBinder extends UiBinder<Widget, MainTabBasicDetailsView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    @Inject
    public MainTabBasicDetailsView(ApplicationResources resources) {
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));

        osImage.setUrl(resources.unassignedSmallImage().getURL());
        vmImage.setUrl(resources.serverVmIcon().getURL());
        vmName.setText("name");
        vmDescription.setText("decsription");
        operatingSystem.setText("Unassigned");
        memory.setText("512MB");
        numberOfCores.setText("1 (1 Socket(s), 1 Core(s) per Socket)");

    }

}
