package org.ovirt.engine.ui.common.presenter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.ui.common.widget.action.ActionButtonDefinition;
import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class PluginActionButtonHandler {
    private final Map<String, List<ActionButtonDefinition<?>>> definitionMap = new HashMap<>();

    @Inject
    public PluginActionButtonHandler(EventBus eventBus) {
        eventBus.addHandler(AddTabActionButtonEvent.getType(),
                new AddTabActionButtonEvent.AddTabActionButtonHandler() {

            @Override
            public void onAddTabActionButton(AddTabActionButtonEvent event) {
                List<ActionButtonDefinition<?>> buttonDefinitionList = definitionMap.get(event.getHistoryToken());
                if (buttonDefinitionList == null) {
                    buttonDefinitionList = new ArrayList<>();
                    definitionMap.put(event.getHistoryToken(), buttonDefinitionList);
                }
                buttonDefinitionList.add(event.getButtonDefinition());
            }
        });
    }

    /**
     * Returns a list of {@code ActionButtonDefinition}s provided by UI plugins.
     * @param historyToken The token used to look up the button definitions.
     * @return A list of {@code ActionButtonDefinition}s, or an empty list if not found.
     */
    public List<ActionButtonDefinition<?>> getButtons(String historyToken) {
        List<ActionButtonDefinition<?>> result = definitionMap.get(historyToken);
        if (result == null) {
            result = Collections.emptyList();
        }
        return result;
    }
}
