package org.ovirt.engine.ui.webadmin.editor;

import java.util.Collection;
import java.util.Map;

import org.ovirt.engine.core.compat.Event;
import org.ovirt.engine.core.compat.EventArgs;
import org.ovirt.engine.core.compat.IEventListener;
import org.ovirt.engine.core.compat.PropertyChangedEventArgs;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.webadmin.widget.HasAccess;
import org.ovirt.engine.ui.webadmin.widget.HasValidation;
import org.ovirt.engine.ui.webadmin.widget.editor.TakesConstrainedValueEditor;
import org.ovirt.engine.ui.webadmin.widget.editor.WidgetWithLabelEditor;

import com.google.gwt.editor.client.EditorContext;
import com.google.gwt.editor.client.EditorVisitor;
import com.google.gwt.editor.client.LeafValueEditor;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.HasEnabled;

public class UiCommonEditorVisitor<M extends Model> extends EditorVisitor {

    private final UiCommonEventMap eventMap;
    private final Map<String, EntityModel> ownerModels;
    private int tabIndexCounter = 0;

    /**
     * A Visitor for UICommon Edited Models.
     */
    public UiCommonEditorVisitor(UiCommonEventMap eventMap, Map<String, EntityModel> ownerModels) {
        this.eventMap = eventMap;
        this.ownerModels = ownerModels;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> boolean visit(final EditorContext<T> ctx) {
        String absolutePath = ctx.getAbsolutePath();
        LeafValueEditor<T> currentLeafEditor = ctx.asLeafValueEditor();

        if (currentLeafEditor == null) {
            // Ignore non-leaf Editors
            return super.visit(ctx);
        }

        final LeafValueEditor<T> editor = getActualEditor(currentLeafEditor);

        // If this Editor implements HasValueChangeHandlers, register a value change listener
        if (editor instanceof HasValueChangeHandlers) {
            ((HasValueChangeHandlers<T>) editor).addValueChangeHandler(new ValueChangeHandler<T>() {
                @Override
                public void onValueChange(ValueChangeEvent<T> event) {
                    ctx.setInModel(event.getValue());
                }
            });
        }

        final WidgetWithLabelEditor<T, ? extends LeafValueEditor<T>, ?> functionalEditor =
                getFunctionalEditor(currentLeafEditor);

        if (functionalEditor != null) {
            // Set tab index
            functionalEditor.setTabIndex(++tabIndexCounter);

            // Add key press handler
            functionalEditor.addKeyPressHandler(new KeyPressHandler() {
                @Override
                public void onKeyPress(KeyPressEvent event) {
                    if (KeyCodes.KEY_ENTER == event.getNativeEvent().getKeyCode()) {
                        // Set value in model
                        ctx.setInModel(editor.getValue());
                    }
                }
            });
        }

        // Handle owner entity models
        if (ownerModels.containsKey(absolutePath)) {
            EntityModel ownerModel = ownerModels.get(absolutePath);

            // If this editor edits a ListModel, initialize it
            if (editor instanceof TakesConstrainedValueEditor && ownerModel instanceof ListModel) {
                updateListEditor((TakesConstrainedValueEditor<T>) editor, (ListModel) ownerModel);
            }

            if (functionalEditor != null) {
                // Register a property change listener on the owner entity model
                ownerModel.getPropertyChangedEvent().addListener(new IEventListener() {
                    @Override
                    public void eventRaised(Event ev, Object sender, EventArgs args) {
                        EntityModel ownerModel = (EntityModel) sender;
                        String propName = ((PropertyChangedEventArgs) args).PropertyName;

                        // IsValid
                        if ("IsValid".equals(propName)) {
                            onIsValidPropertyChange(functionalEditor, ownerModel);
                        }

                        // IsChangable
                        else if ("IsChangable".equals(propName)) {
                            onIsChangablePropertyChange(functionalEditor, ownerModel);
                        }

                        // IsAvailable
                        else if ("IsAvailable".equals(propName)) {
                            onIsAvailablePropertyChange(functionalEditor, ownerModel);
                        }
                    }
                });

                // Update editor since we might have missed property change
                // events fired as part of the entity model constructor
                onIsValidPropertyChange(functionalEditor, ownerModel);
                onIsChangablePropertyChange(functionalEditor, ownerModel);
                onIsAvailablePropertyChange(functionalEditor, ownerModel);
            }
        }

        // Register listeners
        eventMap.registerListener(absolutePath, "EntityChanged", new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                editor.setValue((T) ((EntityModel) sender).getEntity());
            }
        });
        eventMap.registerListener(absolutePath, "ItemsChanged", new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                updateListEditor((TakesConstrainedValueEditor<T>) editor, (ListModel) sender);
            }
        });
        eventMap.registerListener(absolutePath, "SelectedItemChanged", new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                editor.setValue((T) ((ListModel) sender).getSelectedItem());
            }
        });

        return super.visit(ctx);
    }

    @SuppressWarnings("unchecked")
    <T> LeafValueEditor<T> getActualEditor(LeafValueEditor<T> editor) {
        if (editor instanceof WidgetWithLabelEditor) {
            return ((WidgetWithLabelEditor<T, ? extends LeafValueEditor<T>, ?>) editor).getSubEditor();
        } else {
            return editor;
        }
    }

    @SuppressWarnings("unchecked")
    <T> WidgetWithLabelEditor<T, ? extends LeafValueEditor<T>, ?> getFunctionalEditor(LeafValueEditor<T> editor) {
        if (editor instanceof WidgetWithLabelEditor) {
            return (WidgetWithLabelEditor<T, ? extends LeafValueEditor<T>, ?>) editor;
        } else {
            return null;
        }
    }

    /**
     * Update a ListEditor according to the items in the ListModel <BR>
     * (this is required since the Editor is bound to the "selectedItem" property, and not to the "items" property).
     *
     * @param listEditor
     * @param parentModel
     */
    @SuppressWarnings("unchecked")
    <O> void updateListEditor(TakesConstrainedValueEditor<O> listEditor, ListModel parentModel) {
        Collection<O> items = (Collection<O>) parentModel.getItems();
        if (items != null) {
            if (items.size() > 0) {
                // Order is important
                parentModel.setSelectedItem(items.iterator().next());
                listEditor.setValue(items.iterator().next());
            }

            listEditor.setAcceptableValues(items);
        }
    }

    void onIsValidPropertyChange(HasValidation editor, EntityModel model) {
        if (model.getIsValid()) {
            editor.markAsValid();
        } else {
            editor.markAsInvalid(model.getInvalidityReasons());
        }
    }

    void onIsChangablePropertyChange(HasEnabled editor, EntityModel model) {
        boolean isChangable = model.getIsChangable();
        editor.setEnabled(isChangable);
    }

    void onIsAvailablePropertyChange(HasAccess editor, EntityModel model) {
        boolean isAvailable = model.getIsAvailable();
        editor.setAccessible(isAvailable);
    }

}
