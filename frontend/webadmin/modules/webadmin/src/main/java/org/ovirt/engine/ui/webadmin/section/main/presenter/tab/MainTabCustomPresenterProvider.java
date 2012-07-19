package org.ovirt.engine.ui.webadmin.section.main.presenter.tab;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;
import com.google.inject.Provider;

/**
 * Provider of {@link MainTabCustomPresenter} instances (non-singleton).
 */
public class MainTabCustomPresenterProvider implements Provider<MainTabCustomPresenter> {

    private MainTabCustomPresenter presenter;
    private MainTabCustomPresenter.ProxyDef proxy;

    private final Provider<MainTabCustomPresenter.ViewDef> viewProvider;
    private final EventBus eventBus;

    private String contentUrl;

    @Inject
    public MainTabCustomPresenterProvider(Provider<MainTabCustomPresenter.ViewDef> viewProvider, EventBus eventBus) {
        this.viewProvider = viewProvider;
        this.eventBus = eventBus;
    }

    @Override
    public MainTabCustomPresenter get() {
        assert proxy != null : "You must call setProxy first"; //$NON-NLS-1$

        if (presenter == null) {
            // Create and bind the presenter
            presenter = new MainTabCustomPresenter(eventBus, viewProvider.get(), proxy, contentUrl);
            presenter.bind();
        }

        return presenter;
    }

    public void setProxy(MainTabCustomPresenter.ProxyDef proxy) {
        this.proxy = proxy;
    }

    public void setContentUrl(String contentUrl) {
        this.contentUrl = contentUrl;
    }

}
