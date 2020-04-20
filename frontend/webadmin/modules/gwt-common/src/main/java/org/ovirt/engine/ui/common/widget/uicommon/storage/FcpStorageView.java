package org.ovirt.engine.ui.common.widget.uicommon.storage;

import java.util.List;

import org.ovirt.engine.ui.common.editor.UiCommonEditorDriver;
import org.ovirt.engine.ui.common.widget.HasValidation;
import org.ovirt.engine.ui.common.widget.PaginationControl;
import org.ovirt.engine.ui.common.widget.ValidatedPanelWidget;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicommonweb.models.storage.SanStorageModelBase;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

public class FcpStorageView extends AbstractStorageView<SanStorageModelBase> implements HasValidation {

    @UiField
    @Path(value = "getLUNsFailure")
    Label errorMessage;

    @UiField
    @Path(value = "selectedLunWarning")
    Label warning;

    @UiField
    ValidatedPanelWidget contentPanel;

    @UiField
    PaginationControl paginationControl;

    private final Driver driver = GWT.create(Driver.class);

    private double panelHeight = 292;

    private double listHeight = 278;

    public FcpStorageView(boolean multiSelection) {
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        driver.initialize(this);
        this.multiSelection = multiSelection;
    }

    public FcpStorageView(boolean multiSelection, double panelHeight, double listHeight) {
        this(multiSelection);

        this.panelHeight = panelHeight;
        this.listHeight = listHeight;
    }

    @Override
    public void edit(final SanStorageModelBase object) {
        driver.edit(object);

        initLists(object);

        // Add event handlers
        object.getPropertyChangedEvent().addListener((ev, sender, args) -> {
            String propName = args.propertyName;
            if (propName.equals("IsValid")) { //$NON-NLS-1$
                onIsValidPropertyChange(object);
            }
        });
    }

    void onIsValidPropertyChange(Model model) {
        if (model.getIsValid()) {
            markAsValid();
        } else {
            markAsInvalid(model.getInvalidityReasons());
        }
    }

    @Override
    public void markAsValid() {
        contentPanel.markAsValid();
    }

    @Override
    public void markAsInvalid(List<String> validationHints) {
        contentPanel.markAsInvalid(validationHints);
    }

    @Override
    public boolean isValid() {
        return contentPanel.isValid();
    }

    @Override
    public SanStorageModelBase flush() {
        return driver.flush();
    }

    @Override
    public void cleanup() {
        driver.cleanup();
    }

    @Override
    public void focus() {
    }

    protected void initLists(SanStorageModelBase model) {
        PageFilter pageFilter = new PageFilter(50);
        SanStorageLunToTargetList sanStorageLunToTargetList =
                new SanStorageLunToTargetList(PagingProxyModel.create(pageFilter, model), true, multiSelection);
        sanStorageLunToTargetList.activateItemsUpdate();
        paginationControl.setDataProvider(StoragePagingDataProvider.create(pageFilter, sanStorageLunToTargetList));
        model.getItemsChangedEvent().addListener((ev, sender, args) -> paginationControl.updateTableControls());

        // Update style
        sanStorageLunToTargetList.setTreeContainerHeight(listHeight);
        contentPanel.getElement().getStyle().setHeight(panelHeight, Unit.PX);

        // Add view widget to panel
        contentPanel.setWidget(sanStorageLunToTargetList);
    }

    interface Driver extends UiCommonEditorDriver<SanStorageModelBase, FcpStorageView> {
    }

    interface ViewUiBinder extends UiBinder<Widget, FcpStorageView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }
}
