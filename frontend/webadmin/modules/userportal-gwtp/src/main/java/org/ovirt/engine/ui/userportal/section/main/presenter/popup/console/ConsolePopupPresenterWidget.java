package org.ovirt.engine.ui.userportal.section.main.presenter.popup.console;

import org.ovirt.engine.ui.common.presenter.AbstractModelBoundPopupPresenterWidget;
import org.ovirt.engine.ui.uicommonweb.models.userportal.IUserPortalListModel;
import org.ovirt.engine.ui.uicommonweb.models.userportal.UserPortalItemModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.SpiceConsoleModel;
import org.ovirt.engine.ui.userportal.ApplicationConstants;
import org.ovirt.engine.ui.userportal.widget.basic.ConsoleUtils;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;
import com.gwtplatform.dispatch.annotation.GenEvent;

public class ConsolePopupPresenterWidget extends AbstractModelBoundPopupPresenterWidget<IUserPortalListModel, ConsolePopupPresenterWidget.ViewDef> {

    private final ConsoleUtils consoleUtils;
    private final ApplicationConstants constants;

    @GenEvent
    class ConsoleModelChanged {
        UserPortalItemModel itemModel;
    }

    public interface ViewDef extends AbstractModelBoundPopupPresenterWidget.ViewDef<IUserPortalListModel> {

        void setSpiceAvailable(boolean visible);

        void setRdpAvailable(boolean visible);

        HasValueChangeHandlers<Boolean> getSpiceRadioButton();

        HasValueChangeHandlers<Boolean> getRdpRadioButton();

        void rdpSelected(boolean selected);

        void spiceSelected(boolean selected);

        void selectSpice(boolean selected);

        void selectRdp(boolean selected);

        void setAdditionalConsoleAvailable(boolean hasAdditionalConsole);

        void setSpiceConsoleAvailable(boolean b);

    }

    @Inject
    public ConsolePopupPresenterWidget(EventBus eventBus, ViewDef view,
            ConsoleUtils consoleUtils, ApplicationConstants constants) {
        super(eventBus, view);
        this.consoleUtils = consoleUtils;
        this.constants = constants;
    }

    @Override
    public void init(final IUserPortalListModel model) {

        // it is needed to define this buttons by hand as the model does not contain the specific commands
        // TODO implement the enter/escape binding
        getView().removeButtons();

        getView().addFooterButton(constants.cancel(), "IDs are currently ignored").addClickHandler(new ClickHandler() { //$NON-NLS-1$

            @Override
            public void onClick(ClickEvent event) {
                hideAndUnbind();
            }
        });

        getView().addFooterButton(constants.ok(), "IDs are currently ignored").addClickHandler(new ClickHandler() { //$NON-NLS-1$

            @Override
            public void onClick(ClickEvent event) {
                ConsoleModelChangedEvent.fire(getEventBus(), (UserPortalItemModel) model.getSelectedItem());
                getView().flush();
                hideAndUnbind();
            }
        });

        getView().setTitle(constants.consoleOptions());

        initView(model);

        super.init(model);
    }

    private void initView(IUserPortalListModel model) {

        listenOnRadioButtons();

        UserPortalItemModel currentItem = (UserPortalItemModel) model.getSelectedItem();

        boolean spiceAvailable =
                currentItem.getDefaultConsole() instanceof SpiceConsoleModel && consoleUtils.isSpiceAvailable();
        boolean rdpAvailable = currentItem.getHasAdditionalConsole() && consoleUtils.isRDPAvailable();

        getView().setSpiceAvailable(spiceAvailable);
        getView().setRdpAvailable(rdpAvailable);

        if (spiceAvailable && rdpAvailable) {
            getView().selectSpice(true);
            getView().selectRdp(false);
            getView().spiceSelected(true);
        } else {
            getView().selectSpice(spiceAvailable);
            getView().selectRdp(rdpAvailable);
            getView().rdpSelected(spiceAvailable);
            getView().spiceSelected(rdpAvailable);
        }

        getView().setAdditionalConsoleAvailable(currentItem.getHasAdditionalConsole());
        getView().setSpiceConsoleAvailable(currentItem.getDefaultConsole() instanceof SpiceConsoleModel);

    }

    protected void listenOnRadioButtons() {
        registerHandler(getView().getRdpRadioButton().addValueChangeHandler(new ValueChangeHandler<Boolean>() {

            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                getView().rdpSelected(event.getValue());
                getView().spiceSelected(!event.getValue());
            }
        }));

        registerHandler(getView().getSpiceRadioButton().addValueChangeHandler(new ValueChangeHandler<Boolean>() {

            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                getView().spiceSelected(event.getValue());
                getView().rdpSelected(!event.getValue());
            }
        }));
    }

}
