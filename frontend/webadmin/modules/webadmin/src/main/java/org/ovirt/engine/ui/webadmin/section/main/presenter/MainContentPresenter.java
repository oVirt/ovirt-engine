package org.ovirt.engine.ui.webadmin.section.main.presenter;

import java.util.HashSet;
import java.util.Set;

import org.ovirt.engine.ui.webadmin.section.main.presenter.overlay.BookmarkPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.overlay.OverlayPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.overlay.OverlayPresenter.OverlayType;
import org.ovirt.engine.ui.webadmin.section.main.presenter.overlay.TagsPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.overlay.TasksPresenter;

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

public class MainContentPresenter extends Presenter<MainContentPresenter.ViewDef, MainContentPresenter.ProxyDef> {

    @ProxyCodeSplit
    public interface ProxyDef extends Proxy<MainContentPresenter> {
    }

    public interface ViewDef extends View {
    }

    @ContentSlot
    public static final Type<RevealContentHandler<?>> TYPE_SetMainTabPanelContent = new Type<>();

    @ContentSlot
    public static final Type<RevealContentHandler<?>> TYPE_SetSubTabPanelContent = new Type<>();

    @ContentSlot
    public static final Type<RevealContentHandler<?>> TYPE_SetOverlayContent = new Type<>();

    Set<OverlayPresenter> overlays = new HashSet<>();

    private Set<PresenterWidget<?>> mainTabPresenters;
    private Set<PresenterWidget<?>> subTabPresenters;

    private boolean clearing = false;

    @Inject
    public MainContentPresenter(EventBus eventBus, ViewDef view, ProxyDef proxy, TasksPresenter tasksPresenter,
            BookmarkPresenter bookmarkPresenter, TagsPresenter tagsPresenter) {
        super(eventBus, view, proxy, MainSectionPresenter.TYPE_SetMainContent);
        overlays.add(tasksPresenter);
        overlays.add(bookmarkPresenter);
        overlays.add(tagsPresenter);
    }

    @Override
    protected void onBind() {
        super.onBind();
        registerHandler(getEventBus().addHandler(UpdateMainContentLayoutEvent.getType(),
                new UpdateMainContentLayoutEvent.UpdateMainContentLayoutHandler() {

                    @Override
                    public void onUpdateMainContentLayout(UpdateMainContentLayoutEvent event) {
                        UpdateMainContentLayout.ContentDisplayType displayType = event.getContentDisplayType();
                        if (!clearing) {
                            clearing = true;
                            switch (displayType) {
                            case MAIN:
                                clearSlot(TYPE_SetSubTabPanelContent);
                                clearSlot(TYPE_SetOverlayContent);
                                break;
                            case SUB:
                                clearSlot(TYPE_SetMainTabPanelContent);
                                clearSlot(TYPE_SetOverlayContent);
                                break;
                            case OVERLAY:
                                Set<PresenterWidget<?>> currentMainPresenters = new HashSet<>();
                                if (mainTabPresenters != null) {
                                    currentMainPresenters.addAll(mainTabPresenters);
                                }
                                Set<PresenterWidget<?>> currentSubPresenters = new HashSet<>();
                                if (subTabPresenters != null) {
                                    currentSubPresenters.addAll(subTabPresenters);
                                }
                                mainTabPresenters = getChildren(LegacySlotConvertor.convert(TYPE_SetMainTabPanelContent));
                                subTabPresenters = getChildren(LegacySlotConvertor.convert(TYPE_SetSubTabPanelContent));
                                if (!(currentMainPresenters.isEmpty() && currentSubPresenters.isEmpty()) &&
                                        currentOverlayMatches(event.getOverlayType()) ) {
                                    restoreTabs(currentMainPresenters, currentSubPresenters);
                                } else {
                                    // If the overlay matches (it means we are switching overlay), switch to original
                                    // tab, then switch to the new overlay, so we keep the real tab.
                                    if (!currentOverlayMatches(event.getOverlayType())) {
                                        restoreTabs(currentMainPresenters, currentSubPresenters);
                                        mainTabPresenters = getChildren(LegacySlotConvertor.convert(TYPE_SetMainTabPanelContent));
                                        subTabPresenters = getChildren(LegacySlotConvertor.convert(TYPE_SetSubTabPanelContent));
                                    }
                                    clearSlot(TYPE_SetSubTabPanelContent);
                                    clearSlot(TYPE_SetMainTabPanelContent);
                                    setInSlot(TYPE_SetOverlayContent, getOverlayPresenter(event.getOverlayType()));
                                }
                                break;
                            case RESTORE:
                                restoreTabs(mainTabPresenters, subTabPresenters);
                                break;
                            }
                            clearing = false;
                        }
                    }

                    private void restoreTabs(Set<PresenterWidget<?>> mainTabPresenters, Set<PresenterWidget<?>> subTabPresenters) {
                        clearSlot(TYPE_SetOverlayContent);
                        if (!mainTabPresenters.isEmpty()) {
                            // There should be one element in presenters.
                            setInSlot(TYPE_SetMainTabPanelContent, mainTabPresenters.iterator().next());
                        } else if (!subTabPresenters.isEmpty()) {
                            setInSlot(TYPE_SetSubTabPanelContent, subTabPresenters.iterator().next());
                        }
                        mainTabPresenters.clear();
                        subTabPresenters.clear();
                    }
                }));
}

    private boolean currentOverlayMatches(OverlayType eventOverlay) {
        Set<PresenterWidget<?>> overlaySet = getChildren(LegacySlotConvertor.convert(TYPE_SetOverlayContent));
        if (eventOverlay != null) {
            for (PresenterWidget<?> overlay: overlaySet) {
                if (eventOverlay.equals(((OverlayPresenter)overlay).getOverlayType())) {
                    return true;
                }
            }
        }
        return false;
    }

    private Presenter<?, ?> getOverlayPresenter(OverlayType eventOverlay) {
        for (OverlayPresenter presenter: this.overlays) {
            if (eventOverlay.equals(presenter.getOverlayType())) {
                return (Presenter<?, ?>) presenter;
            }
        }
        throw new RuntimeException("Unable to locate specified overlay presenter: " + eventOverlay); // $NON-NLS-1$
    }
}
