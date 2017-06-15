package org.ovirt.engine.ui.webadmin.section.main.view.popup.bookmark;

import org.ovirt.engine.ui.common.editor.UiCommonEditorDriver;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.idhandler.WithElementId;
import org.ovirt.engine.ui.common.view.popup.AbstractModelBoundPopupView;
import org.ovirt.engine.ui.common.widget.dialog.SimpleDialogPanel;
import org.ovirt.engine.ui.common.widget.editor.generic.StringEntityModelTextBoxEditor;
import org.ovirt.engine.ui.uicommonweb.models.bookmarks.BookmarkModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.bookmark.BookmarkPopupPresenterWidget;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.inject.Inject;

public class BookmarkPopupView extends AbstractModelBoundPopupView<BookmarkModel> implements BookmarkPopupPresenterWidget.ViewDef {

    interface Driver extends UiCommonEditorDriver<BookmarkModel, BookmarkPopupView> {
    }

    interface ViewUiBinder extends UiBinder<SimpleDialogPanel, BookmarkPopupView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    interface ViewIdHandler extends ElementIdHandler<BookmarkPopupView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    @UiField
    @Path(value = "name.entity")
    @WithElementId("name")
    StringEntityModelTextBoxEditor nameEditor;

    @UiField
    @Path(value = "searchString.entity")
    @WithElementId("searchString")
    StringEntityModelTextBoxEditor searchStringEditor;

    private final Driver driver = GWT.create(Driver.class);

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    @Inject
    public BookmarkPopupView(EventBus eventBus) {
        super(eventBus);
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        localize();
        driver.initialize(this);
        ViewIdHandler.idHandler.generateAndSetIds(this);
    }

    void localize() {
        nameEditor.setLabel(constants.bookmarkPopupNameLabel());
        searchStringEditor.setLabel(constants.bookmarkPopupSearchStringLabel());
    }

    @Override
    public void edit(BookmarkModel object) {
        driver.edit(object);
    }

    @Override
    public BookmarkModel flush() {
        return driver.flush();
    }

    @Override
    public void cleanup() {
        driver.cleanup();
    }

    @Override
    public void focusInput() {
        nameEditor.setFocus(true);
    }

}
