package org.ovirt.engine.ui.webadmin.widget.vnicProfile;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.common.CommonApplicationMessages;
import org.ovirt.engine.ui.common.CommonApplicationResources;
import org.ovirt.engine.ui.common.widget.editor.TakesConstrainedValueEditor;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.profiles.NewVnicProfileModel;
import org.ovirt.engine.ui.uicommonweb.models.profiles.VnicProfileModel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.IsEditor;
import com.google.gwt.editor.client.adapters.TakesValueEditor;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.TakesValue;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasConstrainedValue;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.Widget;

public class VnicProfilesEditor extends Composite implements IsEditor<TakesValueEditor<Object>>, TakesValue<Object>, HasConstrainedValue<Object> {

    @Override
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<Object> handler) {
        // not needed - there is no selected item because all are edited
        return null;
    }

    interface WidgetUiBinder extends UiBinder<Widget, VnicProfilesEditor> {
        WidgetUiBinder uiBinder = GWT.create(WidgetUiBinder.class);
    }

    @UiField
    FlowPanel contentPanel;

    @UiField
    WidgetStyle style;

    protected static final CommonApplicationMessages messages = GWT.create(CommonApplicationMessages.class);
    protected static final CommonApplicationResources resources = GWT.create(CommonApplicationResources.class);

    private List<VnicProfileWidget> editors;

    public VnicProfilesEditor() {
        initWidget(WidgetUiBinder.uiBinder.createAndBindUi(this));
        editors = new ArrayList<VnicProfileWidget>();
    }

    @Override
    public void setValue(Object listModelValues) {
        // not needed - there is no selected item because all are edited
    }

    @Override
    public void setValue(Object value, boolean fireEvents) {
        // not needed - there is no selected item because all are edited
    }

    @Override
    public void setAcceptableValues(Collection<Object> values) {
        if (values == null) {
            return;
        }

        editors.clear();
        contentPanel.clear();

        int numOfProfiles = values.size();

        for (final Object value : values) {
            final Guid dcId = ((VnicProfileModel) value).getDcId();
            VnicProfileWidget vnicProfileWidget = new VnicProfileWidget();
            editors.add(vnicProfileWidget);
            vnicProfileWidget.edit((VnicProfileModel) value);

            final HorizontalPanel profilePanel = new HorizontalPanel();

            PushButton addButton = new PushButton(new Image(resources.increaseIcon()));
            final PushButton remvoeButton = new PushButton(new Image(resources.decreaseIcon()));
            addButton.addStyleName(style.addButtonStyle());
            remvoeButton.addStyleName(style.removeButtonStyle());
            profilePanel.add(vnicProfileWidget);
            profilePanel.add(addButton);
            profilePanel.add(remvoeButton);

            addButton.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    List models = (List<VnicProfileModel>) getValue().getItems();
                    VnicProfileModel existingProfileModel = (VnicProfileModel) value;
                    VnicProfileModel newVnicProfileModel = new NewVnicProfileModel(existingProfileModel.getSourceModel(),
                            existingProfileModel.getDcCompatibilityVersion(), dcId);
                    models.add(models.indexOf(existingProfileModel) + 1, newVnicProfileModel);

                    setAcceptableValues(models);
                }
            });

            remvoeButton.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    List models = (List<VnicProfileModel>) getValue().getItems();
                    models.remove(value);
                    setAcceptableValues(models);
                }
            });

            profilePanel.addStyleName(style.profilePanel());

            contentPanel.add(profilePanel);
        }
    }

    public ListModel flush() {
        // this flushes it
        return getValue();
    }

    @Override
    public ListModel getValue() {
        List<VnicProfileModel> values = new LinkedList<VnicProfileModel>();
        for (VnicProfileWidget editor : editors) {
            values.add(editor.flush());
        }

        ListModel model = new ListModel();
        model.setItems(values);
        return model;
    }

    @Override
    public TakesValueEditor<Object> asEditor() {
        return TakesConstrainedValueEditor.of(this, this, this);
    }

    interface WidgetStyle extends CssResource {
        String addButtonStyle();

        String removeButtonStyle();

        String profilePanel();
    }

}
