package org.ovirt.engine.ui.webadmin.section.main.presenter;

import org.ovirt.engine.ui.uicommonweb.models.CommonModel;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.dom.client.HasKeyDownHandlers;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;

public class SearchPanelPresenterWidget extends PresenterWidget<SearchPanelPresenterWidget.ViewDef> {

    public interface ViewDef extends View {

        String getSearchString();

        void setSearchString(String searchString);

        void setSearchStringPrefix(String searchStringPrefix);

        void setHasSearchStringPrefix(boolean hasSearchStringPrefix);

        void setHasSelectedTags(boolean hasSelectedTags);

        HasClickHandlers getBookmarkButton();

        HasClickHandlers getClearButton();

        HasClickHandlers getSearchButton();

        HasKeyDownHandlers getSearchInputHandlers();

        void hideSuggestionBox();

        void enableSearchBar(boolean status);

        void setCommonModel(CommonModel commonModel);

    }

    private CommonModel commonModel;
    private final Provider<CommonModel> commonProvider;

    @Inject
    public SearchPanelPresenterWidget(EventBus eventBus, ViewDef view, Provider<CommonModel> commonProvider) {
        super(eventBus, view);
        this.commonProvider = commonProvider;

        updateCommonModel();
        addCommonModelListeners();
    }

    void updateCommonModel() {
        commonModel = commonProvider.get();
        getView().setCommonModel(commonModel);
    }

    void addCommonModelListeners() {
        commonModel.getPropertyChangedEvent().addListener((ev, sender, args) -> {
            // Update search string when 'SearchString' property changes
            if ("SearchString".equals(args.propertyName)) { //$NON-NLS-1$
                updateViewSearchString();
            }

            // Update search string prefix when 'SearchStringPrefix' property changes
            else if ("SearchStringPrefix".equals(args.propertyName)) { //$NON-NLS-1$
                updateViewSearchStringPrefix();
            }

            // Update search string prefix visibility when 'HasSearchStringPrefix' property changes
            else if ("HasSearchStringPrefix".equals(args.propertyName)) { //$NON-NLS-1$
                updateViewHasSearchStringPrefix();
            }

            else if ("HasSelectedTags".equals(args.propertyName)) { //$NON-NLS-1$
                updateViewHasSelectedTags();
            }

            else if ("SearchEnabled".equals(args.propertyName)) { //$NON-NLS-1$
                updateViewSearchEnabled();
            }
        });
    }

    @Override
    protected void onBind() {
        super.onBind();

        registerHandler(getView().getBookmarkButton().addClickHandler(event -> commonModel.getBookmarkList().getNewCommand().execute()));

        registerHandler(getView().getClearButton().addClickHandler(event -> commonModel.getClearSearchStringCommand().execute()));

        registerHandler(getView().getSearchButton().addClickHandler(event -> updateModelSearchString()));

        registerHandler(getView().getSearchInputHandlers().addKeyDownHandler(event -> {
            if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
                updateModelSearchString();
            } else if (event.getNativeKeyCode() == KeyCodes.KEY_ESCAPE) {
                getView().hideSuggestionBox();
            }
        }));
    }

    @Override
    protected void onReveal() {
        super.onReveal();

        updateViewSearchString();
        updateViewSearchStringPrefix();
        updateViewHasSearchStringPrefix();
    }

    void updateModelSearchString() {
        commonModel.setSearchString(getView().getSearchString());
        commonModel.search();
    }

    void updateViewSearchString() {
        getView().setSearchString(commonModel.getSearchString());
    }

    void updateViewSearchStringPrefix() {
        getView().setSearchStringPrefix(commonModel.getSearchStringPrefix());
    }

    void updateViewHasSearchStringPrefix() {
        getView().setHasSearchStringPrefix(commonModel.getHasSearchStringPrefix());
    }

    void updateViewHasSelectedTags() {
        getView().setHasSelectedTags(commonModel.getHasSelectedTags());
    }

    void updateViewSearchEnabled() {
        getView().enableSearchBar(commonModel.getSearchEnabled());
    }

}
