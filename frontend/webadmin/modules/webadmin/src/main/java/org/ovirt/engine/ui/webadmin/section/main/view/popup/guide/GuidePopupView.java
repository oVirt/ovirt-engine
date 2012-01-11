package org.ovirt.engine.ui.webadmin.section.main.view.popup.guide;

import org.ovirt.engine.core.compat.Event;
import org.ovirt.engine.core.compat.EventArgs;
import org.ovirt.engine.core.compat.IEventListener;
import org.ovirt.engine.core.compat.PropertyChangedEventArgs;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.GuideModel;
import org.ovirt.engine.ui.uicommonweb.models.clusters.ClusterGuideModel;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.DataCenterGuideModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmGuideModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.guide.GuidePopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.AbstractModelBoundPopupView;
import org.ovirt.engine.ui.webadmin.uicommon.model.GuideModelProvider;
import org.ovirt.engine.ui.webadmin.widget.UiCommandButton;
import org.ovirt.engine.ui.webadmin.widget.dialog.SimpleDialogPanel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.inject.Inject;

public class GuidePopupView extends AbstractModelBoundPopupView<GuideModel> implements GuidePopupPresenterWidget.ViewDef {

    interface Driver extends SimpleBeanEditorDriver<GuideModel, GuidePopupView> {
        Driver driver = GWT.create(Driver.class);
    }

    interface ViewUiBinder extends UiBinder<SimpleDialogPanel, GuidePopupView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    interface ViewIdHandler extends ElementIdHandler<GuidePopupView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    String datacenterCreated;
    String clusterCreated;
    String vmCreated;
    String unconfigured;
    String configurationCompleted;

    @UiField
    @Ignore
    Label infoLabel;

    @UiField
    @Ignore
    Label optionalActionsLabel;

    @UiField
    @Ignore
    Label compulsoryActionsLabel;

    @UiField
    VerticalPanel compulsoryActionsPanel;

    @UiField
    VerticalPanel optionalActionsPanel;

    @UiField
    VerticalPanel compulsorySection;

    @UiField
    VerticalPanel optionalSection;

    @UiField
    Style style;

    private final GuideModelProvider guideModelProvider;

    private final ApplicationResources resources;

    @Inject
    public GuidePopupView(EventBus eventBus,
            ApplicationResources resources,
            ApplicationConstants constants,
            GuideModelProvider guideModelProvider) {
        super(eventBus, resources);
        this.guideModelProvider = guideModelProvider;
        this.resources = resources;
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        ViewIdHandler.idHandler.generateAndSetIds(this);
        localize(constants);
        Driver.driver.initialize(this);
    }

    void localize(ApplicationConstants constants) {
        datacenterCreated = constants.guidePopupDataCenterCreatedLabel();
        clusterCreated = constants.guidePopupClusterCreatedLabel();
        vmCreated = constants.guidePopupVMCreatedLabel();
        unconfigured = constants.guidePopupUnconfiguredLabel();
        configurationCompleted = constants.guidePopupConfigurationCompletedLabel();
        compulsoryActionsLabel.setText(constants.guidePopupRequiredActionsLabel());
        optionalActionsLabel.setText(constants.guidePopupOptionalActionsLabel());
    }

    @Override
    public void edit(GuideModel object) {
        Driver.driver.edit(object);

        guideModelProvider.setModel(object);

        object.getPropertyChangedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                GuideModel guideModel = (GuideModel) sender;
                String propertyName = ((PropertyChangedEventArgs) args).PropertyName;

                if ("Progress".equals(propertyName)) {
                    if (guideModel.getProgress() == null) {
                        if (guideModel.getCompulsoryActions().isEmpty()) {
                            infoLabel.setText(configurationCompleted);
                            optionalSection.setVisible(true);
                            compulsorySection.setVisible(false);
                        }
                        else if (guideModel.getOptionalActions().isEmpty()) {
                            updateCreatedLabel(guideModel);
                            optionalSection.setVisible(false);
                            compulsorySection.setVisible(true);
                            compulsoryActionsLabel.setVisible(true);
                        }
                        else {
                            infoLabel.setText(unconfigured);
                            optionalSection.setVisible(true);
                            compulsorySection.setVisible(true);
                            compulsoryActionsLabel.setVisible(false);
                        }
                    }

                    updateActionsPanels(guideModel);
                }
                else if ("Window".equals(propertyName)) {
                    if (guideModel.getLastExecutedCommand().getName().equals("Cancel")) {
                        redrawActionsPanels();
                    }
                }
            }
        });
    }

    private void updateActionsPanels(GuideModel object) {
        compulsoryActionsPanel.clear();
        optionalActionsPanel.clear();

        for (final UICommand command : object.getCompulsoryActions()) {
            addButton(command, compulsoryActionsPanel, resources.wrenchImage());
        }

        for (final UICommand command : object.getOptionalActions()) {
            addButton(command, optionalActionsPanel, resources.plusButtonImage());
        }
    }

    private void redrawActionsPanels() {
        compulsorySection.remove(compulsoryActionsPanel);
        compulsorySection.add(compulsoryActionsPanel);

        optionalSection.remove(optionalActionsPanel);
        optionalSection.add(optionalActionsPanel);
    }

    private void addButton(final UICommand command, VerticalPanel buttonsPanel, ImageResource buttonImage) {
        UiCommandButton guideButton = new UiCommandButton(command.getTitle(), buttonImage);
        guideButton.setCommand(command);
        guideButton.getElement().setId("UiCommandButton_guideButton_" + command.getTitle());
        guideButton.setCustomContentStyle(style.actionButtonContent());
        guideButton.addStyleName(style.actionButton());
        guideButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                command.Execute();
            }
        });

        if (!command.getExecuteProhibitionReasons().isEmpty()) {
            guideButton.setTitle(command.getExecuteProhibitionReasons().get(0));
        }

        VerticalPanel buttonContainer = new VerticalPanel();
        buttonContainer.add(guideButton);
        buttonsPanel.add(buttonContainer);
    }

    private void updateCreatedLabel(GuideModel object) {
        if (object instanceof DataCenterGuideModel) {
            infoLabel.setText(datacenterCreated);
        }
        else if (object instanceof ClusterGuideModel) {
            infoLabel.setText(clusterCreated);
        }
        else if (object instanceof VmGuideModel) {
            infoLabel.setText(vmCreated);
        }
    }

    @Override
    public GuideModel flush() {
        return Driver.driver.flush();
    }

    interface Style extends CssResource {
        String actionButtonContent();

        String actionButton();
    }
}
