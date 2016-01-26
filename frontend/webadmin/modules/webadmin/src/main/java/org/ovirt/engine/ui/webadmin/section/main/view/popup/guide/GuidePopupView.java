package org.ovirt.engine.ui.webadmin.section.main.view.popup.guide;

import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.view.popup.AbstractModelBoundPopupView;
import org.ovirt.engine.ui.common.widget.UiCommandButton;
import org.ovirt.engine.ui.common.widget.dialog.SimpleDialogPanel;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.GuideModel;
import org.ovirt.engine.ui.uicommonweb.models.clusters.ClusterGuideModel;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.DataCenterGuideModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmGuideModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.IEventListener;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.guide.GuidePopupPresenterWidget;
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
    Label noteLabel;

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

    private final Driver driver = GWT.create(Driver.class);

    private static final ApplicationResources resources = AssetProvider.getResources();
    private static final ApplicationConstants constants = AssetProvider.getConstants();

    @Inject
    public GuidePopupView(EventBus eventBus) {
        super(eventBus);
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        ViewIdHandler.idHandler.generateAndSetIds(this);
        localize();
        driver.initialize(this);
    }

    void localize() {
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
        driver.edit(object);

        object.getPropertyChangedEvent().addListener(new IEventListener<PropertyChangedEventArgs>() {
            @Override
            public void eventRaised(Event<? extends PropertyChangedEventArgs> ev, Object sender, PropertyChangedEventArgs args) {
                GuideModel guideModel = (GuideModel) sender;
                String propertyName = args.propertyName;

                if (PropertyChangedEventArgs.PROGRESS.equals(propertyName)) {
                    if (guideModel.getProgress() == null) {

                        // Check whether there any available actions.
                        boolean hasAllowedActions = false;
                        for (Object item : Linq.concatUnsafe(guideModel.getCompulsoryActions(), guideModel.getOptionalActions())) {
                            UICommand command = (UICommand) item;
                            if (command.getIsExecutionAllowed()) {
                                hasAllowedActions = true;
                                break;
                            }
                        }

                        // Choose an appropriate message matching the entity type (DC, Cluster or VM).
                        String message = null;
                        if (guideModel instanceof DataCenterGuideModel) {
                            message = constants.guidePopupConfiguredDataCenterLabel();
                        } else if (guideModel instanceof ClusterGuideModel) {
                            message = constants.guidePopupConfiguredClusterLabel();
                        } else if (guideModel instanceof VmGuideModel) {
                            message = constants.guidePopupConfiguredVmLabel();
                        }

                        if (!hasAllowedActions) {
                            if (!guideModel.getNote().getIsAvailable()) {
                                infoLabel.setText(message);
                            }
                            else {
                                infoLabel.setText(configurationCompleted);
                                noteLabel.setText(guideModel.getNote().getEntity());
                            }
                            compulsorySection.setVisible(false);
                            optionalSection.setVisible(false);
                            // Rename dialog button.
                            guideModel.getCommands().get(0).setTitle(null);
                            guideModel.getCommands().get(0).setTitle(ConstantsManager.getInstance().getConstants().ok());
                        } else if (guideModel.getCompulsoryActions().isEmpty()) {
                            infoLabel.setText(configurationCompleted);
                            optionalSection.setVisible(true);
                            compulsorySection.setVisible(false);
                        } else if (guideModel.getOptionalActions().isEmpty()) {
                            updateCreatedLabel(guideModel);
                            optionalSection.setVisible(false);
                            compulsorySection.setVisible(true);
                            compulsoryActionsLabel.setVisible(true);
                        } else {
                            infoLabel.setText(unconfigured);
                            optionalSection.setVisible(true);
                            compulsorySection.setVisible(true);
                            compulsoryActionsLabel.setVisible(false);
                        }
                    }

                    updateActionsPanels(guideModel);
                } else if ("Window".equals(propertyName)) { //$NON-NLS-1$
                    if (guideModel.getLastExecutedCommand().getName().equals("Cancel")) { //$NON-NLS-1$
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
        guideButton.getElement().setId("UiCommandButton_guideButton_" + command.getTitle()); //$NON-NLS-1$
        guideButton.setCustomContentStyle(style.actionButtonContent());
        guideButton.addStyleName(style.actionButton());
        guideButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                command.execute();
            }
        });

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
        return driver.flush();
    }

    interface Style extends CssResource {
        String actionButtonContent();

        String actionButton();
    }

}
