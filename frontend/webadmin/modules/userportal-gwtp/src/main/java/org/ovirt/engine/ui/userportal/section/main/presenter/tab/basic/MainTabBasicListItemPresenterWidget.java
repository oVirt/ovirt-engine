package org.ovirt.engine.ui.userportal.section.main.presenter.tab.basic;

import java.util.Arrays;

import org.ovirt.engine.core.compat.Event;
import org.ovirt.engine.core.compat.EventArgs;
import org.ovirt.engine.core.compat.IEventListener;
import org.ovirt.engine.ui.common.widget.HasEditorDriver;
import org.ovirt.engine.ui.uicommonweb.models.userportal.UserPortalBasicListModel;
import org.ovirt.engine.ui.uicommonweb.models.userportal.UserPortalItemModel;
import org.ovirt.engine.ui.userportal.section.main.view.tab.basic.MainTabBasicListItemMessages;
import org.ovirt.engine.ui.userportal.section.main.view.tab.basic.widget.ConsoleProtocol;
import org.ovirt.engine.ui.userportal.section.main.view.tab.basic.widget.ConsoleUtils;
import org.ovirt.engine.ui.userportal.uicommon.model.UserPortalBasicListProvider;

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

    private final MainTabBasicListItemMessages messages;

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
            UserPortalBasicListProvider modelProvider,
            MainTabBasicListItemMessages messages) {
        super(eventBus, view);
        this.consoleUtils = consoleUtils;
        this.modelProvider = modelProvider;
        this.messages = messages;
    }

    public void setModel(final UserPortalItemModel model) {
        this.model = model;
        this.listModel = modelProvider.getModel();
        selectedProtocol = consoleUtils.determineDefaultProtocol(model);
        setupDefaultVmStyles();
        getView().edit(model);

        listModel.getSelectedItemChangedEvent().addListener(new IEventListener() {

            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                if (listModel.getSelectedItem() != model) {
                    getView().setNotSelected(model.IsVmUp());
                }
            }
        });

        if (sameEntity((UserPortalItemModel) listModel.getSelectedItem(), model)) {
            getView().setSelected();
        }

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

    protected void setupDefaultVmStyles() {
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
        if (!canShowConsole()) {
            return;
        }

        if (selectedProtocol == ConsoleProtocol.SPICE) {
            showSpiceConsole();
        } else if (selectedProtocol == ConsoleProtocol.RDP) {
            showRpdConsole();
        }

    }

    private void showRpdConsole() {
        if (consoleUtils.canOpenRDPConsole(model)) {
            model.getAdditionalConsole().getConnectCommand().Execute();
        } else if (!consoleUtils.isRDPAvailable()) {
            showError("RDP");
        }
    }

    private void showSpiceConsole() {
        if (consoleUtils.canOpenSpiceConsole(model)) {
            model.getDefaultConsole().getConnectCommand().Execute();
        } else if (!consoleUtils.isSpiceAvailable()) {
            showError("SPICE");
        }
    }

    private void showError(String protocol) {
        getView().showErrorDialog(messages.errorConnectingToConsole(model.getName(), protocol));
    }

    @Override
    public void onClick(ClickEvent event) {
        if (!isSelected()) {
            listModel.setSelectedItem(model);
            modelProvider.setSelectedItems(Arrays.asList(model));
            getView().setSelected();
        }
    }

    private boolean canShowConsole() {
        if (selectedProtocol == null) {
            return false;
        }

        boolean isSpiceAvailable =
                selectedProtocol.equals(ConsoleProtocol.SPICE) && consoleUtils.canOpenSpiceConsole(model);
        boolean isRdpAvailable =
                (selectedProtocol.equals(ConsoleProtocol.RDP) && consoleUtils.canOpenRDPConsole(model));

        return isSpiceAvailable || isRdpAvailable;
    }

    private boolean isSelected() {
        return sameEntity((UserPortalItemModel) listModel.getSelectedItem(), model);
    }

}
