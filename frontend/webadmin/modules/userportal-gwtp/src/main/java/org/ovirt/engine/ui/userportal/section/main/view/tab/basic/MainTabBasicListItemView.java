package org.ovirt.engine.ui.userportal.section.main.view.tab.basic;

import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.ui.common.system.ErrorPopupManager;
import org.ovirt.engine.ui.common.view.AbstractView;
import org.ovirt.engine.ui.common.widget.action.ActionButton;
import org.ovirt.engine.ui.common.widget.action.ActionButtonDefinition;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.userportal.UserPortalItemModel;
import org.ovirt.engine.ui.userportal.ApplicationResources;
import org.ovirt.engine.ui.userportal.section.main.presenter.tab.basic.MainTabBasicListItemPresenterWidget;
import org.ovirt.engine.ui.userportal.section.main.view.tab.basic.widget.MainTabBasicListItemActionButton;
import org.ovirt.engine.ui.userportal.section.main.view.tab.basic.widget.MainTabBasicListItemMessagesTranslator;
import org.ovirt.engine.ui.userportal.section.main.view.tab.basic.widget.OsTypeImage;
import org.ovirt.engine.ui.userportal.section.main.view.tab.basic.widget.VmPausedImage;
import org.ovirt.engine.ui.userportal.section.main.view.tab.basic.widget.VmUpMaskImage;
import org.ovirt.engine.ui.userportal.widget.action.UserPortalImageButtonDefinition;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.DoubleClickEvent;
import com.google.gwt.event.dom.client.DoubleClickHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.event.logical.shared.InitializeEvent;
import com.google.gwt.event.logical.shared.InitializeHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.text.shared.AbstractRenderer;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.ValueLabel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class MainTabBasicListItemView extends AbstractView implements MainTabBasicListItemPresenterWidget.ViewDef {

    @UiField
    @Path("osType")
    OsTypeImage osTypeImage;

    @UiField
    @Path("IsVmUp")
    VmUpMaskImage vmUpImage;

    @UiField
    @Path("Status")
    VmPausedImage vmPausedImage;

    @UiField(provided = true)
    @Path("Status")
    ValueLabel<VMStatus> vmStatus;

    @UiField
    @Path("name")
    Label vmName;

    @UiField
    @Ignore
    FlowPanel buttonsPanel;

    @UiField
    @Ignore
    SimplePanel consoleBaner;

    @UiField
    @Ignore
    LayoutPanel mainContainer;

    @UiField
    Style style;

    private final ApplicationResources applicationResources;

    private final ErrorPopupManager errorPopupManager;

    private final MainTabBasicListItemMessages messages;

    interface Driver extends SimpleBeanEditorDriver<UserPortalItemModel, MainTabBasicListItemView> {
        Driver driver = GWT.create(Driver.class);
    }

    public interface Style extends CssResource {

        String itemOverStyle();

        String itemNotRunningStyle();

        String itemRunningStyle();

        String machineStatusSelectedStyle();

        String machineStatusStyle();

        String itemSelectedStyle();

        String handCursor();

        String defaultCursor();
    }

    interface ViewUiBinder extends UiBinder<Widget, MainTabBasicListItemView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    @Inject
    public MainTabBasicListItemView(
            final ApplicationResources applicationResources,
            final MainTabBasicListItemMessagesTranslator translator,
            final MainTabBasicListItemMessages messages,
            final ErrorPopupManager errorPopupManager) {

        this.applicationResources = applicationResources;
        this.messages = messages;
        this.errorPopupManager = errorPopupManager;

        vmStatus = new ValueLabel<VMStatus>(new AbstractRenderer<VMStatus>() {
            @Override
            public String render(VMStatus object) {
                return translator.translate(object.name());
            }
        });

        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        consoleBaner.setVisible(false);

        Driver.driver.initialize(this);
    }

    protected void initButtons(ApplicationResources applicationResources, final UserPortalItemModel model) {
        addButton(new UserPortalImageButtonDefinition<UserPortalItemModel>(model.getIsPool() ? messages.takeVm() : messages.runVm(),
                applicationResources.playIcon(),
                applicationResources.playDisabledIcon()) {

            @Override
            protected UICommand resolveCommand() {
                return model.getIsPool() ? model.getTakeVmCommand() : model.getRunCommand();
            }

        });

        addButton(new UserPortalImageButtonDefinition<UserPortalItemModel>(messages.shutdownVm(),
                applicationResources.stopIcon(),
                applicationResources.stopDisabledIcon()) {

            @Override
            protected UICommand resolveCommand() {
                return model.getShutdownCommand();
            }

        });

        addButton(new UserPortalImageButtonDefinition<UserPortalItemModel>(messages.suspendVm(),
                applicationResources.pauseIcon(),
                applicationResources.pauseDisabledIcon()) {

            @Override
            protected UICommand resolveCommand() {
                return model.getPauseCommand();
            }

        });
    }

    private void addButton(final UserPortalImageButtonDefinition<UserPortalItemModel> buttonDefinition) {

        final MainTabBasicListItemActionButton button = new MainTabBasicListItemActionButton();
        button.initialize(buttonDefinition);

        button.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                buttonDefinition.onClick(null);
            }
        });

        buttonsPanel.add(button);

        buttonDefinition.addInitializeHandler(new InitializeHandler() {

            @Override
            public void onInitialize(InitializeEvent event) {
                updateActionButton(button, buttonDefinition);
            }
        });

        updateActionButton(button, buttonDefinition);
    }

    /**
     * Ensures that the specified action button is visible or hidden and enabled or disabled as it should.
     */
    private void updateActionButton(ActionButton button, ActionButtonDefinition<UserPortalItemModel> buttonDef) {
        button.setEnabled(buttonDef.isEnabled(null));
    }

    @Override
    public void edit(UserPortalItemModel model) {
        initButtons(applicationResources, model);
        Driver.driver.edit(model);
    }

    @Override
    public UserPortalItemModel flush() {
        return Driver.driver.flush();
    }

    @Override
    public void fireEvent(GwtEvent<?> event) {
        // no-op, the handlers are on the widget
    }

    @Override
    public HandlerRegistration addMouseOverHandler(MouseOverHandler handler) {
        return asWidget().addDomHandler(handler, MouseOverEvent.getType());
    }

    @Override
    public HandlerRegistration addMouseOutHandler(MouseOutHandler handler) {
        return asWidget().addDomHandler(handler, MouseOutEvent.getType());
    }

    @Override
    public HandlerRegistration addClickHandler(ClickHandler handler) {
        return asWidget().addDomHandler(handler, ClickEvent.getType());
    }

    @Override
    public void showDoubleClickBanner() {
        consoleBaner.setVisible(true);
        mainContainer.addStyleName(style.handCursor());
    }

    @Override
    public void hideDoubleClickBanner() {
        consoleBaner.setVisible(false);
        mainContainer.addStyleName(style.defaultCursor());
    }

    @Override
    public void setVmUpStyle() {
        mainContainer.setStyleName(style.itemRunningStyle());
    }

    @Override
    public void setVmDownStyle() {
        mainContainer.setStyleName(style.itemNotRunningStyle());
    }

    @Override
    public void setMouseOverStyle() {
        mainContainer.setStyleName(style.itemOverStyle());
    }

    @Override
    public void setSelected() {
        vmStatus.setStyleName(style.machineStatusSelectedStyle());
        mainContainer.setStyleName(style.itemSelectedStyle());
    }

    @Override
    public void setNotSelected(boolean vmIsUp) {
        vmStatus.setStyleName(style.machineStatusStyle());
        if (vmIsUp) {
            mainContainer.setStyleName(style.itemRunningStyle());
        } else {
            mainContainer.setStyleName(style.itemNotRunningStyle());
        }
    }

    @Override
    public HandlerRegistration addDoubleClickHandler(DoubleClickHandler handler) {
        return mainContainer.addDomHandler(handler, DoubleClickEvent.getType());
    }

    @Override
    public void showErrorDialog(String message) {
        errorPopupManager.show(message);
    }

}
