package org.ovirt.engine.ui.webadmin.section.main.presenter;

import java.util.Set;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.GwtEvent.Type;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.ContentSlot;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.presenter.slots.LegacySlotConvertor;
import com.gwtplatform.mvp.client.proxy.Proxy;
import com.gwtplatform.mvp.client.proxy.RevealContentHandler;

public class MainContentPresenter extends Presenter<MainContentPresenter.ViewDef, MainContentPresenter.ProxyDef>
    implements RevealOverlayContentEvent.RevealOverlayContentHandler {

    @ProxyCodeSplit
    public interface ProxyDef extends Proxy<MainContentPresenter> {
    }

    public interface ViewDef extends View {
    }

    @ContentSlot
    public static final Type<RevealContentHandler<?>> TYPE_SetContent = new Type<>();

    @ContentSlot
    public static final Type<RevealContentHandler<?>> TYPE_SetOverlay = new Type<>();

    @Inject
    public MainContentPresenter(EventBus eventBus, ViewDef view, ProxyDef proxy) {
        super(eventBus, view, proxy, MainSectionPresenter.TYPE_SetMainContent);
    }

    @Override
    public void onBind() {
        super.onBind();
        addRegisteredHandler(RevealOverlayContentEvent.getType(), this);
    }

    @Override
    public void setInSlot(Object slot, PresenterWidget<?> content) {
        super.setInSlot(slot, content);
        if (slot == TYPE_SetContent) {
            onRevealOverlayContent(new RevealOverlayContentEvent(null));
        }
    }

    @Override
    public void onRevealOverlayContent(RevealOverlayContentEvent event) {
        Set<PresenterWidget<?>> children = getChildren(LegacySlotConvertor.convert(TYPE_SetContent));
        if (event.getContent() != null) {
            event.getContent().setCurrentPlaceWidget(children.iterator().next());
        }
        setInSlot(TYPE_SetOverlay, event.getContent());
    }
}
