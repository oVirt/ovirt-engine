package org.ovirt.engine.ui.webadmin.section.main.presenter.overlay;

import java.util.Collection;

import org.ovirt.engine.ui.uicommonweb.models.tags.TagModel;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.IEventListener;
import org.ovirt.engine.ui.webadmin.section.main.presenter.AbstractOverlayPresenter;
import org.ovirt.engine.ui.webadmin.uicommon.model.TagModelProvider;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.proxy.Proxy;

public class TagsPresenter extends AbstractOverlayPresenter<TagsPresenter.ViewDef, TagsPresenter.ProxyDef> {

    public interface ViewDef extends AbstractOverlayPresenter.ViewDef {
        void updateTags(Collection<TagModel> tags);
        HasClickHandlers getAddTagButton();
    }

    @ProxyCodeSplit
    public interface ProxyDef extends Proxy<TagsPresenter> {
    }

    private final TagModelProvider tagModelProvider;
    @Inject
    public TagsPresenter(EventBus eventBus, ViewDef view, ProxyDef proxy, TagModelProvider tagModelProvider) {
        super(eventBus, view, proxy);
        this.tagModelProvider = tagModelProvider;
    }

    @Override
    public void onBind() {
        super.onBind();
        tagModelProvider.getModel().getItemsChangedEvent().addListener(new IEventListener<EventArgs>() {

            @Override
            public void eventRaised(Event<? extends EventArgs> ev, Object sender, EventArgs args) {
                getView().updateTags(tagModelProvider.getModel().getItems());
            }

        });
        registerHandler(((ViewDef) getView()).getAddTagButton().addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                tagModelProvider.getModel().getSelectionModel().setSelected(tagModelProvider.getModel().getRootNode(), true);
                tagModelProvider.getModel().executeCommand(tagModelProvider.getModel().getNewCommand());
            }

        }));
    }

    @Override
    public void onReveal() {
        super.onReveal();
        tagModelProvider.getModel().search();
    }

    @Override
    public OverlayType getOverlayType() {
        return OverlayType.TAGS;
    }
}
