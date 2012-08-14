package org.ovirt.engine.ui.webadmin.section.main.presenter;

import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.queries.ConfigurationValues;
import org.ovirt.engine.core.common.queries.GetConfigurationValueParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.ui.common.widget.dialog.PopupNativeKeyPressHandler;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;

import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.PopupView;
import com.gwtplatform.mvp.client.PresenterWidget;

/**
 * About popup presenter.
 *
 * @author Asaf Shakarchi
 */
public class AboutPopupPresenterWidget extends PresenterWidget<AboutPopupPresenterWidget.ViewDef> {

    public interface ViewDef extends PopupView {

        void setVersion(String version);

        HasClickHandlers getCloseButton();

        void setPopupKeyPressHandler(PopupNativeKeyPressHandler handler);
    }

    @Inject
    public AboutPopupPresenterWidget(EventBus eventBus, ViewDef view) {
        super(eventBus, view);
    }

    @Override
    protected void onBind() {
        super.onBind();

        registerHandler(getView().getCloseButton().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                getView().hide();
            }
        }));

        getView().setPopupKeyPressHandler(new PopupNativeKeyPressHandler() {
            @Override
            public void onKeyPress(NativeEvent event) {
                if (KeyCodes.KEY_ESCAPE == event.getKeyCode()) {
                    getView().hide();
                }
            }
        });

    }

    @Override
    protected void onReveal() {
        super.onReveal();

        AsyncQuery aQuery = new AsyncQuery();

        Frontend.RunPublicQuery(VdcQueryType.GetConfigurationValue,
                new GetConfigurationValueParameters(ConfigurationValues.VdcVersion, Config.DefaultConfigurationVersion),
                aQuery);

        AsyncQuery _asyncQuery = new AsyncQuery();
        _asyncQuery.setModel(this);
        _asyncQuery.asyncCallback = new INewAsyncCallback() {
            @Override
            public void OnSuccess(Object model, Object result) {
                String version = (String) result;

                getView().setVersion(version);
            }
        };

        AsyncDataProvider.GetRpmVersionViaPublic(_asyncQuery);
    }

}
