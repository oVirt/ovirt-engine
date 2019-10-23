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

    private final Map<String, List<ActionButtonDefinition<?, ?>>> buttonDefinitionMap = new HashMap<>();
    private final Map<String, List<ActionButtonDefinition<?, ?>>> menuItemDefinitionMap = new HashMap<>();

    @Inject
    public PluginActionButtonHandler(EventBus eventBus) {
        eventBus.addHandler(AddActionButtonEvent.getType(),
            event -> {
                Map<String, List<ActionButtonDefinition<?, ?>>> buttonMap = event.isAddToKebabMenu()
                        ? menuItemDefinitionMap : buttonDefinitionMap;
                List<ActionButtonDefinition<?, ?>> buttonList = buttonMap.get(event.getHistoryToken());

                if (buttonList == null) {
                    buttonList = new ArrayList<>();
                    buttonMap.put(event.getHistoryToken(), buttonList);
                }
                buttonList.add(event.getButtonDefinition());
            });
    }

    /**
     * Returns a list of {@code ActionButtonDefinition}s provided by UI plugins.
     * @param historyToken The token used to look up the button definitions.
     * @return A list of {@code ActionButtonDefinition}s, or an empty list if not found.
     */
    public List<ActionButtonDefinition<?, ?>> getButtons(String historyToken) {
        List<ActionButtonDefinition<?, ?>> result = buttonDefinitionMap.get(historyToken);
        if (result == null) {
            result = Collections.emptyList();
        }
        return result;
    }

    /**
     * Returns a list of {@code ActionButtonDefinition}s provided by UI plugins.
     * @param historyToken The token used to look up the button definitions.
     * @return A list of {@code ActionButtonDefinition}s that are supposed to go into the kebab menu,
     * or an empty list if not found.
     */
    public List<ActionButtonDefinition<?, ?>> getMenuItems(String historyToken) {
        List<ActionButtonDefinition<?, ?>> result = menuItemDefinitionMap.get(historyToken);
        if (result == null) {
            result = Collections.emptyList();
        }
        return result;
    }

}
