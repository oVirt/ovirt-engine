package org.ovirt.engine.ui.common.editor;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.ui.common.widget.HasAccess;
import org.ovirt.engine.ui.common.widget.HasEnabledWithHints;
import org.ovirt.engine.ui.common.widget.HasValidation;
import org.ovirt.engine.ui.common.widget.editor.HasEditorValidityState;
import org.ovirt.engine.ui.common.widget.editor.ListModelMultipleSelectListBox;
import org.ovirt.engine.ui.common.widget.editor.TakesConstrainedValueEditor;
import org.ovirt.engine.ui.common.widget.editor.TakesConstrainedValueListEditor;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.IEventListener;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;
import com.google.gwt.editor.client.EditorContext;
import com.google.gwt.editor.client.EditorVisitor;
import com.google.gwt.editor.client.LeafValueEditor;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;

/**
 * Editor visitor that implements integration with UiCommon models.
 *
 * @see UiCommonEditor
 */
public class UiCommonEditorVisitor<M extends Model> extends EditorVisitor {

    private final UiCommonEventMap eventMap;
    private final Map<String, Model> ownerModels;
    private int tabIndexCounter = 0;

    /**
     * A Visitor for UICommon Edited Models.
     */
    public UiCommonEditorVisitor(UiCommonEventMap eventMap, Map<String, Model> ownerModels) {
        this.eventMap = eventMap;
        this.ownerModels = ownerModels;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> boolean visit(final EditorContext<T> ctx) {
        final String absolutePath = ctx.getAbsolutePath();
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
                    setInModel(ctx, event.getSource(), event.getValue());
                }
            });
        }

        final UiCommonEditor<T> functionalEditor = getFunctionalEditor(currentLeafEditor);

        if (functionalEditor != null) {

            // Set tab index, unless it's being set manually (i.e. already been set)
            if (functionalEditor.getTabIndex() <= 0) {
                functionalEditor.setTabIndex(++tabIndexCounter);
            }

            // Add key press handler
            functionalEditor.addKeyPressHandler(new KeyPressHandler() {
                @Override
                public void onKeyPress(KeyPressEvent event) {
                    if (KeyCodes.KEY_ENTER == event.getNativeEvent().getKeyCode()) {
                        setInModel(ctx, editor, editor.getValue());
                    }
                }
            });
        }

        // Handle owner entity models
        if (ownerModels.containsKey(absolutePath)) {
            Model ownerModel = ownerModels.get(absolutePath);

            // If this editor edits a ListModel, initialize it
            if (editor instanceof TakesConstrainedValueListEditor && ownerModel instanceof ListModel) {
                updateListEditor((TakesConstrainedValueListEditor<T>) editor, (ListModel) ownerModel);
            } else if (editor instanceof TakesConstrainedValueEditor && ownerModel instanceof ListModel) {
                updateListEditor((TakesConstrainedValueEditor<T>) editor, (ListModel) ownerModel);
            }

            if (functionalEditor != null) {
                // Register a property change listener on the owner entity model
                ownerModel.getPropertyChangedEvent().addListener(new IEventListener<PropertyChangedEventArgs>() {
                    @Override
                    public void eventRaised(Event<? extends PropertyChangedEventArgs> ev, Object sender, PropertyChangedEventArgs args) {
                        Model ownerModel = (Model) sender;
                        String propName = args.propertyName;

                        // IsValid
                        if ("IsValid".equals(propName)) { //$NON-NLS-1$
                            onIsValidPropertyChange(functionalEditor, ownerModel);
                        }

                        // IsChangable
                        else if ("IsChangable".equals(propName)) { //$NON-NLS-1$
                            onIsChangablePropertyChange(functionalEditor, ownerModel);
                        }

                        // ChangeProhibitionReason
                        else if ("ChangeProhibitionReason".equals(propName)) { //$NON-NLS-1$
                            onChangeProhibitionReasonChange(functionalEditor, ownerModel);
                        }

                        // IsAvailable
                        else if ("IsAvailable".equals(propName)) { //$NON-NLS-1$
                            onIsAvailablePropertyChange(functionalEditor, ownerModel);
                        }
                    }
                });

                // Update editor since we might have missed property change
                // events fired as part of the entity model constructor
                onIsValidPropertyChange(functionalEditor, ownerModel);
                onIsChangablePropertyChange(functionalEditor, ownerModel);
                onChangeProhibitionReasonChange(functionalEditor, ownerModel);
                onIsAvailablePropertyChange(functionalEditor, ownerModel);
            }
        }

        // Register listeners
        eventMap.registerListener(absolutePath, "EntityChanged", new IEventListener() { //$NON-NLS-1$
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                editor.setValue((T) ((EntityModel) sender).getEntity());
            }
        });
        eventMap.registerListener(absolutePath, "ItemsChanged", new IEventListener() { //$NON-NLS-1$
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                updateListEditor((TakesConstrainedValueEditor<T>) editor, (ListModel) sender);
            }
        });
        eventMap.registerListener(absolutePath, "SelectedItemChanged", new IEventListener() { //$NON-NLS-1$
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                T selectedItem = (T) ((ListModel) sender).getSelectedItem();
                if (editor instanceof TakesConstrainedValueListEditor && ownerModels.get(absolutePath) instanceof ListModel) {
                    editor.setValue((T)Arrays.asList(selectedItem));
                } else {
                    editor.setValue(selectedItem);
                }
            }
        });
        eventMap.registerListener(absolutePath, "SelectedItemsChanged", new IEventListener() { //$NON-NLS-1$
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                if (editor instanceof TakesConstrainedValueListEditor && ownerModels.get(absolutePath) instanceof ListModel) {
                    ((TakesConstrainedValueListEditor)editor).setListValue((List<T>)((ListModel) sender).getSelectedItems());
                }
            }
        });

        return super.visit(ctx);
    }

    private <T> void setInModel(final EditorContext<T> ctx, Object editor, T value) {
        if (ctx.canSetInModel()) {
            boolean editorValid = true;
            if (editor instanceof HasEditorValidityState) {
                editorValid = ((HasEditorValidityState)editor).isStateValid();
            }
            if (editorValid) {
                if (editor instanceof ListModelMultipleSelectListBox) {
                    @SuppressWarnings("unchecked")
                    T listValue = (T) ((ListModelMultipleSelectListBox<T>) editor).selectedItems();
                    ctx.setInModel(listValue);
                } else {
                    ctx.setInModel(value);
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    <T> LeafValueEditor<T> getActualEditor(LeafValueEditor<T> editor) {
        if (editor instanceof UiCommonEditor) {
            return ((UiCommonEditor<T>) editor).getActualEditor();
        } else {
            return editor;
        }
    }

    @SuppressWarnings("unchecked")
    <T> UiCommonEditor<T> getFunctionalEditor(LeafValueEditor<T> editor) {
        if (editor instanceof UiCommonEditor) {
            return (UiCommonEditor<T>) editor;
        } else {
            return null;
        }
    }

    /**
     * Update a ListEditor according to the items in the ListModel <BR>
     * (this is required since the Editor is bound to the "selectedItem" property, and not to the "items" property).
     */
    @SuppressWarnings("unchecked")
    <O> void updateListEditor(TakesConstrainedValueEditor<O> listEditor, ListModel<O> parentModel) {
        Collection<O> items = parentModel.getItems();
        if (items != null) {
            if (items.size() > 0) {
                O value;
                if (parentModel.getSelectedItem() != null) {
                    value = parentModel.getSelectedItem();
                } else {
                    value = items.iterator().next();
                    // Order is important
                    parentModel.setSelectedItem(value);
                }
                if (items.contains(value)) {
                    if (listEditor instanceof TakesConstrainedValueListEditor) {
                        if (parentModel.getSelectedItems() != null) {
                            ((TakesConstrainedValueListEditor<O>)listEditor).setListValue(parentModel.getSelectedItems());
                        }
                    } else {
                        listEditor.setValue(value);
                    }
                }
            }
            if (listEditor instanceof TakesConstrainedValueListEditor) {
                ((TakesConstrainedValueListEditor<O>)listEditor).setAcceptableListValues(items);
            } else {
                listEditor.setAcceptableValues(items);
            }

        }
    }

    void onIsValidPropertyChange(HasValidation editor, Model model) {
        if (model.getIsValid()) {
            editor.markAsValid();
        } else {
            //The entity validator will mark the entity valid before running the validation
            //this will cause the editor to be marked valid if it was invalid due to an entity validation
            //error. Then if the validation is invalid again this will update the error message. So there is
            //no possibility to go from one invalid reason to another without the editor message being updated.
            if (editor.isValid()) {
                editor.markAsInvalid(model.getInvalidityReasons());
            }
        }
    }

    void onIsChangablePropertyChange(HasEnabledWithHints editor, Model model) {
        if (model.getIsChangable()) {
            editor.setEnabled(true);
        } else {
            editor.disable(model.getChangeProhibitionReason());
        }
    }

    void onChangeProhibitionReasonChange(HasEnabledWithHints editor, Model model) {
        if (!editor.isEnabled()) {
            editor.disable(model.getChangeProhibitionReason());
        }
    }

    void onIsAvailablePropertyChange(HasAccess editor, Model model) {
        boolean isAvailable = model.getIsAvailable();
        editor.setAccessible(isAvailable);
    }

}
