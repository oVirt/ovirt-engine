package org.ovirt.engine.ui.userportal.section.main.presenter.tab.basic;

import java.util.Arrays;

import org.ovirt.engine.ui.common.idhandler.HasElementId;
import org.ovirt.engine.ui.common.widget.HasEditorDriver;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.userportal.ConsoleProtocol;
import org.ovirt.engine.ui.uicommonweb.models.userportal.UserPortalBasicListModel;
import org.ovirt.engine.ui.uicommonweb.models.userportal.UserPortalItemModel;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.IEventListener;
import org.ovirt.engine.ui.userportal.section.main.presenter.popup.console.ConsoleModelChangedEvent;
import org.ovirt.engine.ui.userportal.section.main.presenter.popup.console.ConsoleModelChangedEvent.ConsoleModelChangedHandler;
import org.ovirt.engine.ui.userportal.uicommon.model.UserPortalModelInitEvent;
import org.ovirt.engine.ui.userportal.uicommon.model.UserPortalModelInitEvent.UserPortalModelInitHandler;
import org.ovirt.engine.ui.userportal.uicommon.model.basic.UserPortalBasicListProvider;
import org.ovirt.engine.ui.userportal.utils.ConsoleManager;
import org.ovirt.engine.ui.userportal.widget.basic.ConsoleUtils;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.DoubleClickEvent;
import com.google.gwt.event.dom.client.DoubleClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.dom.client.HasDoubleClickHandlers;
import com.google.gwt.event.dom.client.HasMouseOutHandlers;
import com.google.gwt.event.dom.client.HasMouseOverHandlers;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;

public class MainTabBasicListItemPresenterWidget extends PresenterWidget<MainTabBasicListItemPresenterWidget.ViewDef> implements MouseOutHandler, MouseOverHandler, ClickHandler, DoubleClickHandler {

    public interface ViewDef extends View, HasEditorDriver<UserPortalItemModel>, HasMouseOutHandlers, HasMouseOverHandlers, HasClickHandlers, HasDoubleClickHandlers, HasElementId {

        void showDoubleClickBanner();

        void hideDoubleClickBanner();

        void setVmUpStyle();

        void setVmDownStyle();

        void setMouseOverStyle();

        void setSelected();

        void setNotSelected(boolean vmIsUp);

        void showErrorDialog(String message);

        HasClickHandlers addRunButton(UserPortalItemModel model, UICommand command);

        HasClickHandlers addShutdownButton(UserPortalItemModel model, UICommand command);

        HasClickHandlers addSuspendButton(UserPortalItemModel model, UICommand command);
    }

    private final ConsoleUtils consoleUtils;
    private final ConsoleManager consoleManager;
    private final UserPortalBasicListProvider listModelProvider;

    private ConsoleProtocol selectedProtocol;

    private UserPortalItemModel model;

    private UserPortalBasicListModel listModel;

    private IEventListener selectedItemChangeListener;

    @Inject
    public MainTabBasicListItemPresenterWidget(EventBus eventBus, ViewDef view,
            ConsoleUtils consoleUtils, ConsoleManager consoleManager,
            final UserPortalBasicListProvider listModelProvider) {
        super(eventBus, view);
        this.consoleUtils = consoleUtils;
        this.consoleManager = consoleManager;
        this.listModelProvider = listModelProvider;

        registerHandler(eventBus.addHandler(ConsoleModelChangedEvent.getType(), new ConsoleModelChangedHandler() {
            @Override
            public void onConsoleModelChanged(ConsoleModelChangedEvent event) {
                // update only when my model has changed
                if (sameEntity(model, event.getItemModel())) {
                    setupSelectedProtocol(model);
                }
            }

        }));

        registerHandler(getEventBus().addHandler(UserPortalModelInitEvent.getType(), new UserPortalModelInitHandler() {
            @Override
            public void onUserPortalModelInit(UserPortalModelInitEvent event) {
                updateListModel(listModelProvider.getModel());
            }
        }));

        updateListModel(listModelProvider.getModel());
    }

    void addSelectedItemChangeHandler() {
        selectedItemChangeListener = new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                if (!sameEntity(listModel.getSelectedItem(), model)) {
                    getView().setNotSelected(model.IsVmUp());
                } else {
                    getView().setSelected();
                }
            }
        };
        listModel.getSelectedItemChangedEvent().addListener(selectedItemChangeListener);
    }

    void removeSelectedItemChangeHandler() {
        if (listModel != null && selectedItemChangeListener != null) {
            listModel.getSelectedItemChangedEvent().removeListener(selectedItemChangeListener);
            selectedItemChangeListener = null;
        }
    }

    void updateListModel(UserPortalBasicListModel newListModel) {
        // Remove SelectedItemChangedEvent listener from current list model
        removeSelectedItemChangeHandler();

        // Update current list model
        this.listModel = newListModel;

        // Add electedItemChangedEvent listener to new list model
        addSelectedItemChangeHandler();
    }

    public void setModel(UserPortalItemModel model) {
        this.model = model;

        setupSelectedProtocol(model);
        setupDefaultVmStyles();

        final UICommand runCommand = model.getIsPool() ? model.getTakeVmCommand() : model.getRunCommand();
        registerHandler(getView().addRunButton(model, runCommand).addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                executeCommand(runCommand);
            }
        }));

        final UICommand shutdownCommand = model.getShutdownCommand();
        registerHandler(getView().addShutdownButton(model, shutdownCommand).addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                executeCommand(shutdownCommand);
            }
        }));

        final UICommand suspendCommand = model.getPauseCommand();
        registerHandler(getView().addSuspendButton(model, suspendCommand).addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                executeCommand(suspendCommand);
            }
        }));

        getView().edit(model);

        if (sameEntity(listModel.getSelectedItem(), model)) {
            setSelectedItem();
        }
    }

    void executeCommand(UICommand command) {
        if (command != null) {
            command.Execute();
        }
    }

    protected void setupSelectedProtocol(UserPortalItemModel model) {
        selectedProtocol = consoleUtils.determineConnectionProtocol(model);
    }

    boolean sameEntity(UserPortalItemModel prevModel, UserPortalItemModel newModel) {
        if (prevModel == null || newModel == null) {
            return false;
        }
        return listModelProvider.getKey(prevModel).equals(listModelProvider.getKey(newModel));
    }

    @Override
    protected void onBind() {
        super.onBind();
        registerHandler(getView().addMouseOutHandler(this));
        registerHandler(getView().addMouseOverHandler(this));
        registerHandler(getView().addClickHandler(this));
        registerHandler(getView().addDoubleClickHandler(this));
    }

    @Override
    protected void onUnbind() {
        super.onUnbind();
        removeSelectedItemChangeHandler();
    }

    @Override
    public void onMouseOut(MouseOutEvent event) {
        getView().hideDoubleClickBanner();
        setupDefaultVmStyles();
    }

    void setupDefaultVmStyles() {
        if (!isSelected()) {
            if (model.IsVmUp()) {
                getView().setVmUpStyle();
            } else {
                getView().setVmDownStyle();
            }
        }
    }

    @Override
    public void onMouseOver(MouseOverEvent event) {
        if (!isSelected()) {
            getView().setMouseOverStyle();
        }
        if (canShowConsole()) {
            getView().showDoubleClickBanner();
        }
    }

    @Override
    public void onDoubleClick(DoubleClickEvent event) {
        String res = consoleManager.connectToConsole(selectedProtocol, model);
        if (res != null) {
            getView().showErrorDialog(res);
        }
    }

    @Override
    public void onClick(ClickEvent event) {
        if (!isSelected()) {
            setSelectedItem();
        }
    }

    void setSelectedItem() {
        listModel.setSelectedItem(model);
        listModelProvider.setSelectedItems(Arrays.asList(model));
        getView().setSelected();
    }

    boolean canShowConsole() {
        return consoleUtils.canShowConsole(selectedProtocol, model);
    }

    boolean isSelected() {
        return sameEntity(listModel.getSelectedItem(), model);
    }

}
