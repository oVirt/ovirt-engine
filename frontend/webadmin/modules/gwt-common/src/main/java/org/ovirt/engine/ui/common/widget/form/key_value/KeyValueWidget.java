package org.ovirt.engine.ui.common.widget.form.key_value;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.compat.Event;
import org.ovirt.engine.core.compat.EventArgs;
import org.ovirt.engine.core.compat.IEventListener;
import org.ovirt.engine.ui.common.widget.HasEditorDriver;
import org.ovirt.engine.ui.uicommonweb.models.vms.key_value.KeyValueLineModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.key_value.KeyValueModel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class KeyValueWidget extends Composite implements HasEditorDriver<KeyValueModel> {

    interface WidgetUiBinder extends UiBinder<Widget, KeyValueWidget> {
        WidgetUiBinder uiBinder = GWT.create(WidgetUiBinder.class);
    }

    interface Driver extends SimpleBeanEditorDriver<KeyValueModel, KeyValueWidget> {
        Driver driver = GWT.create(Driver.class);
    }

    @UiField
    VerticalPanel panel;

    private ArrayList<KeyValueLineWidget> widgetList;

    KeyValueWidget() {
        initWidget(WidgetUiBinder.uiBinder.createAndBindUi(this));
        Driver.driver.initialize(this);
    }

    @Override
    public void edit(final KeyValueModel object) {
        setPanel(object);
        object.getKeyValueLines().getItemsChangedEvent().addListener(new IEventListener() {

            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                setPanel(object);
            }
        });
    }

    private void setPanel(KeyValueModel object) {
        List<KeyValueLineModel> list = (List<KeyValueLineModel>) object.getKeyValueLines().getItems();
        panel.clear();
        widgetList = new ArrayList<KeyValueLineWidget>();
        //create & edit each row.
        for (KeyValueLineModel keyValueLineModel : list) {
            KeyValueLineWidget keyValueLineWidget = new KeyValueLineWidget();
            widgetList.add(keyValueLineWidget);
            panel.add(keyValueLineWidget);
            keyValueLineWidget.edit(keyValueLineModel);
        }
    }

    @Override
    public KeyValueModel flush() {
        for (KeyValueLineWidget lineWidget : widgetList) {
            lineWidget.flush();
        }
        return Driver.driver.flush();
    }

}
