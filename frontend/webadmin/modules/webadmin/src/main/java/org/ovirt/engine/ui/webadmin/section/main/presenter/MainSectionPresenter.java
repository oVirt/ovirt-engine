package org.ovirt.engine.ui.webadmin.section.main.presenter;

import com.google.gwt.dom.client.Document;
import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.presenter.slots.NestedSlot;
import com.gwtplatform.mvp.client.presenter.slots.PermanentSlot;
import com.gwtplatform.mvp.client.proxy.Proxy;

public class MainSectionPresenter extends Presenter<MainSectionPresenter.ViewDef, MainSectionPresenter.ProxyDef> {

    @ProxyCodeSplit
    public interface ProxyDef extends Proxy<MainSectionPresenter> {
    }

    public interface ViewDef extends View {
    }

    public static final PermanentSlot<HeaderPresenterWidget> TYPE_SetHeader = new PermanentSlot<>();

    public static final PermanentSlot<MenuPresenterWidget> TYPE_SetMenu = new PermanentSlot<>();

    public static final NestedSlot TYPE_SetMainContent = new NestedSlot();

    private final HeaderPresenterWidget header;
    private final MenuPresenterWidget menu;

    @Inject
    public MainSectionPresenter(EventBus eventBus, ViewDef view, ProxyDef proxy,
            HeaderPresenterWidget header, MenuPresenterWidget menu) {
        super(eventBus, view, proxy, RevealType.Root);
        this.header = header;
        this.menu = menu;
    }

    @Override
    protected void onReveal() {
        super.onReveal();

        setInSlot(TYPE_SetHeader, header);
        setInSlot(TYPE_SetMenu, menu);

        // Remove the loading page placeholder
        removeHostPagePlaceholder();
    }

    protected void removeHostPagePlaceholder() {
        Document.get().getElementById("host-page-placeholder").removeFromParent(); //$NON-NLS-1$
    }

}
