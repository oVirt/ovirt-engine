package org.ovirt.engine.ui.common.gin;

import com.gwtplatform.common.client.CommonGinModule;
import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.CommonApplicationMessages;
import org.ovirt.engine.ui.common.CommonApplicationResources;
import org.ovirt.engine.ui.common.CommonApplicationTemplates;
import org.ovirt.engine.ui.common.auth.CurrentUser;
import org.ovirt.engine.ui.common.auth.LoggedInGatekeeper;
import org.ovirt.engine.ui.common.system.ApplicationFocusManager;
import org.ovirt.engine.ui.common.system.AsyncCallFailureHandler;
import org.ovirt.engine.ui.common.system.ClientStorage;
import org.ovirt.engine.ui.common.system.ClientStorageImpl;
import org.ovirt.engine.ui.common.system.ErrorPopupManagerImpl;
import org.ovirt.engine.ui.common.system.LockInteractionManager;
import org.ovirt.engine.ui.common.uicommon.ClientAgentType;
import org.ovirt.engine.ui.common.utils.DynamicMessages;
import org.ovirt.engine.ui.common.utils.HttpUtils;
import org.ovirt.engine.ui.frontend.AppErrors;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.VdsmErrors;
import org.ovirt.engine.ui.frontend.communication.CommunicationProvider;
import org.ovirt.engine.ui.frontend.communication.GWTRPCCommunicationProvider;
import org.ovirt.engine.ui.frontend.communication.OperationProcessor;
import org.ovirt.engine.ui.frontend.communication.SSOTokenChangeEvent;
import org.ovirt.engine.ui.frontend.communication.VdcOperationManager;
import org.ovirt.engine.ui.frontend.communication.XsrfRpcRequestBuilder;
import org.ovirt.engine.ui.frontend.gwtservices.GenericApiGWTService;
import org.ovirt.engine.ui.frontend.gwtservices.GenericApiGWTServiceAsync;
import org.ovirt.engine.ui.uicommonweb.ErrorPopupManager;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.Response;
import com.google.gwt.inject.client.AbstractGinModule;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.rpc.XsrfTokenService;
import com.google.gwt.user.client.rpc.XsrfTokenServiceAsync;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.gwtplatform.mvp.client.RootPresenter;
import com.gwtplatform.mvp.shared.proxy.ParameterTokenFormatter;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.shared.proxy.TokenFormatter;

/**
 * GIN module containing common infrastructure and configuration bindings.
 */
public abstract class BaseSystemModule extends AbstractGinModule {

    protected void bindCommonInfrastructure(Class<? extends PlaceManager> placeManager) {
        install(new CommonGinModule());

        bindEventBus();
        bindFrontendInfrastructure();
        bind(TokenFormatter.class).to(ParameterTokenFormatter.class).in(Singleton.class);
        bind(RootPresenter.class).asEagerSingleton();
        bindTypeAndImplAsSingleton(PlaceManager.class, placeManager);
        bind(CurrentUser.class).in(Singleton.class);
        bind(LoggedInGatekeeper.class).in(Singleton.class);
        bind(ErrorPopupManager.class).to(ErrorPopupManagerImpl.class).in(Singleton.class);
        bind(AsyncCallFailureHandler.class).asEagerSingleton();
        bind(ClientAgentType.class).in(Singleton.class);
        bind(ClientStorage.class).to(ClientStorageImpl.class).in(Singleton.class);
        bind(ApplicationFocusManager.class).asEagerSingleton();
        bind(LockInteractionManager.class).asEagerSingleton();
        requestStaticInjection(AssetProvider.class);
    }

    private void bindEventBus() {
        // Bind actual (non-legacy) EventBus to its legacy interface
        bind(com.google.web.bindery.event.shared.EventBus.class).to(com.google.gwt.event.shared.EventBus.class);
        // Bind legacy EventBus interface to SimpleEventBus implementation
        bind(com.google.gwt.event.shared.EventBus.class).to(SimpleEventBus.class).in(Singleton.class);
    }

    private void bindFrontendInfrastructure() {
        bind(Frontend.class).in(Singleton.class);
        requestStaticInjection(Frontend.InstanceHolder.class);
        bind(VdcOperationManager.class).in(Singleton.class);
        bind(OperationProcessor.class).in(Singleton.class);
        bind(CommunicationProvider.class).to(GWTRPCCommunicationProvider.class).in(Singleton.class);
    }

    protected void bindResourceConfiguration(
            Class<? extends CommonApplicationConstants> constants,
            Class<? extends CommonApplicationMessages> messages,
            Class<? extends CommonApplicationResources> resources,
            Class<? extends CommonApplicationTemplates> templates,
            Class<? extends DynamicMessages> dynamicMessages) {
        bindTypeAndImplAsSingleton(CommonApplicationConstants.class, constants);
        bindTypeAndImplAsSingleton(CommonApplicationMessages.class, messages);
        bindTypeAndImplAsSingleton(CommonApplicationResources.class, resources);
        bindTypeAndImplAsSingleton(CommonApplicationTemplates.class, templates);
        bindTypeAndImplAsSingleton(DynamicMessages.class, dynamicMessages);
        bind(AppErrors.class).in(Singleton.class);
        bind(VdsmErrors.class).in(Singleton.class);
    }

    /**
     * Binds {@code type} to its {@code impl} so that injecting any of these yields the singleton {@code impl} instance.
     */
    protected <T> void bindTypeAndImplAsSingleton(Class<T> type, Class<? extends T> impl) {
        bind(type).to(impl);
        bind(impl).in(Singleton.class);
    }

    @Provides
    @Singleton
    public GenericApiGWTServiceAsync getGenericApiGWTService(final XsrfRpcRequestBuilder requestBuilder) {
        // no need to use GenericApiGWTServiceAsync.Util as this is GIN-managed singleton anyway
        GenericApiGWTServiceAsync service = GWT.create(GenericApiGWTService.class);
        // cast to ServiceDefTarget and set RPC request builder as needed
        ((ServiceDefTarget) service).setRpcRequestBuilder(requestBuilder);
        return service;
    }

    @Provides
    @Singleton
    public XsrfRpcRequestBuilder getXsrfRequestBuilder(final EventBus eventBus) {
        return new XsrfRpcRequestBuilder() {
            @Override
            protected void doSetCallback(RequestBuilder rb, final RequestCallback callback) {
                rb.setCallback(new RequestCallback() {

                    @Override
                    public void onResponseReceived(Request request, Response response) {
                        String tokenValue = HttpUtils.getHeader(response, "OVIRT-SSO-TOKEN"); //$NON-NLS-1$
                        if (tokenValue != null) {
                            //Login result received.
                            SSOTokenChangeEvent.fire(eventBus, tokenValue);
                        }
                        callback.onResponseReceived(request, response);
                    }

                    @Override
                    public void onError(Request request, Throwable exception) {
                        callback.onError(request, exception);
                    }
                });
            }
        };
    }

    @Provides
    @Singleton
    public XsrfTokenServiceAsync getXsrfTokenService() {
        XsrfTokenServiceAsync service = GWT.create(XsrfTokenService.class);
        ((ServiceDefTarget) service).setServiceEntryPoint(GWT.getModuleBaseURL() + XsrfRpcRequestBuilder.XSRF_PATH);
        return service;
    }
}
