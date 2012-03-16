package org.ovirt.engine.ui.userportal.section.main.presenter.tab.basic;

import java.util.Arrays;

import org.ovirt.engine.core.compat.Event;
import org.ovirt.engine.core.compat.EventArgs;
import org.ovirt.engine.core.compat.IEventListener;
import org.ovirt.engine.ui.common.widget.HasEditorDriver;
import org.ovirt.engine.ui.uicommonweb.models.userportal.UserPortalBasicListModel;
import org.ovirt.engine.ui.uicommonweb.models.userportal.UserPortalItemModel;
import org.ovirt.engine.ui.userportal.section.main.presenter.popup.console.ConsoleModelChangedEvent;
import org.ovirt.engine.ui.userportal.section.main.presenter.popup.console.ConsoleModelChangedEvent.ConsoleModelChangedHandler;
import org.ovirt.engine.ui.userportal.uicommon.model.UserPortalModelInitEvent;
import org.ovirt.engine.ui.userportal.uicommon.model.UserPortalModelInitEvent.UserPortalModelInitHandler;
import org.ovirt.engine.ui.userportal.uicommon.model.basic.UserPortalBasicListProvider;
import org.ovirt.engine.ui.userportal.utils.ConsoleManager;
import org.ovirt.engine.ui.userportal.widget.basic.ConsoleProtocol;
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

    private final ConsoleUtils consoleUtils;

    private ConsoleProtocol selectedProtocol;

    private UserPortalItemModel model;

    private UserPortalBasicListModel listModel;

    private final UserPortalBasicListProvider modelProvider;

    private final ConsoleManager consoleManager;

    public interface ViewDef extends View, HasEditorDriver<UserPortalItemModel>, HasMouseOutHandlers, HasMouseOverHandlers, HasClickHandlers, HasDoubleClickHandlers {

        void showDoubleClickBanner();

        void hideDoubleClickBanner();

        void setVmUpStyle();

        void setVmDownStyle();

        void setMouseOverStyle();

        void setSelected();

        void setNotSelected(boolean vmIsUp);

        void showErrorDialog(String message);
    }

    @Inject
    public MainTabBasicListItemPresenterWidget(EventBus eventBus,
            ViewDef view,
            ConsoleUtils consoleUtils,
            final UserPortalBasicListProvider modelProvider,
            ConsoleManager consoleManager) {
        super(eventBus, view);
        this.consoleUtils = consoleUtils;
        this.modelProvider = modelProvider;
        this.consoleManager = consoleManager;

        eventBus.addHandler(ConsoleModelChangedEvent.getType(), new ConsoleModelChangedHandler() {

            @Override
            public void onConsoleModelChanged(ConsoleModelChangedEvent event) {
                // update only when my model has changed
                if (sameEntity(model, event.getItemModel())) {
                    setupSelectedProtocol(model);
                }
            }

        });

        getEventBus().addHandler(UserPortalModelInitEvent.getType(), new UserPortalModelInitHandler() {

            @Override
            public void onUserPortalModelInit(UserPortalModelInitEvent event) {
                listenOnSelectedItemChanged(modelProvider);
            }
        });

        listenOnSelectedItemChanged(modelProvider);
    }

    private void listenOnSelectedItemChanged(UserPortalBasicListProvider modelProvider) {
        modelProvider.getModel().getSelectedItemChangedEvent().addListener(new IEventListener() {

            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                if (!sameEntity((UserPortalItemModel) listModel.getSelectedItem(), model)) {
                    getView().setNotSelected(model.IsVmUp());
                } else {
                    getView().setSelected();
                }
            }
        });

    }

    public void setModel(final UserPortalItemModel model) {
        this.model = model;
        this.listModel = modelProvider.getModel();
        setupSelectedProtocol(model);
        setupDefaultVmStyles();
        getView().edit(model);

        if (sameEntity((UserPortalItemModel) listModel.getSelectedItem(), model)) {
            setSelectedItem();
        }

    }

    protected void setupSelectedProtocol(final UserPortalItemModel model) {
        selectedProtocol = consoleUtils.determineDefaultProtocol(model);
    }

    protected boolean sameEntity(UserPortalItemModel prevModel, UserPortalItemModel newModel) {
        if (prevModel == null || newModel == null) {
            return false;
        }
        return modelProvider.getKey(prevModel).equals(modelProvider.getKey(newModel));
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
    public void onMouseOut(MouseOutEvent event) {
        getView().hideDoubleClickBanner();
        setupDefaultVmStyles();
    }

    private void setupDefaultVmStyles() {
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

    protected void setSelectedItem() {
        listModel.setSelectedItem(model);
        modelProvider.setSelectedItems(Arrays.asList(model));
        getView().setSelected();
    }

    private boolean canShowConsole() {
        return consoleUtils.canShowConsole(selectedProtocol, model);
    }

    boolean isSelected() {
        return sameEntity((UserPortalItemModel) listModel.getSelectedItem(), model);
    }

}
