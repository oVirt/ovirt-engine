package org.ovirt.engine.ui.common.widget.uicommon.storage;

import java.util.List;

import org.ovirt.engine.ui.common.widget.HasValidation;
import org.ovirt.engine.ui.common.widget.ValidatedPanelWidget;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicommonweb.models.storage.SanStorageModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.SanStorageModelBase;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.IEventListener;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

public class FcpStorageView extends AbstractStorageView<SanStorageModel> implements HasValidation {

    @UiField
    @Path(value = "getLUNsFailure")
    Label errorMessage;

    @UiField
    @Path(value = "selectedLunWarning")
    Label warning;

    @UiField
    ValidatedPanelWidget contentPanel;

    private final Driver driver = GWT.create(Driver.class);

    private SanStorageLunToTargetList sanStorageLunToTargetList;

    private double panelHeight = 378;

    private double listHeight = 340;

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
    public void edit(final SanStorageModel object) {
        driver.edit(object);

        initLists(object);

        // Add event handlers
        object.getPropertyChangedEvent().addListener(new IEventListener<PropertyChangedEventArgs>() {
            @Override
            public void eventRaised(Event<? extends PropertyChangedEventArgs> ev, Object sender, PropertyChangedEventArgs args) {
                String propName = args.propertyName;
                if (propName.equals("IsValid")) { //$NON-NLS-1$
                    onIsValidPropertyChange(object);
                }
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
    public SanStorageModel flush() {
        return driver.flush();
    }

    @Override
    public void focus() {
    }

    protected void initLists(SanStorageModelBase object) {
        // Create and update storage list
        sanStorageLunToTargetList = new SanStorageLunToTargetList(object, true, multiSelection);
        sanStorageLunToTargetList.activateItemsUpdate();

        // Update style
        sanStorageLunToTargetList.setTreeContainerHeight(listHeight);
        contentPanel.getElement().getStyle().setHeight(panelHeight, Unit.PX);

        // Add view widget to panel
        contentPanel.setWidget(sanStorageLunToTargetList);
    }

    interface Driver extends SimpleBeanEditorDriver<SanStorageModel, FcpStorageView> {
    }

    interface ViewUiBinder extends UiBinder<Widget, FcpStorageView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }
}
