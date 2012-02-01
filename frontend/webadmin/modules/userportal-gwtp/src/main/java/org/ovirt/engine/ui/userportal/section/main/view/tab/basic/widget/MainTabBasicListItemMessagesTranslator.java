package org.ovirt.engine.ui.userportal.section.main.view.tab.basic.widget;

import java.util.HashMap;
import java.util.Map;

import org.ovirt.engine.ui.userportal.section.main.view.tab.basic.MainTabBasicListItemMessages;

import com.google.inject.Inject;

public class MainTabBasicListItemMessagesTranslator {

    private Map<String, String> dictionary = new HashMap<String, String>();

    @Inject
    public MainTabBasicListItemMessagesTranslator(MainTabBasicListItemMessages messages) {
        dictionary.put("WaitForLaunch", messages.WaitForLaunch());
        dictionary.put("PoweringUp", messages.PoweringUp());
        dictionary.put("RebootInProgress", messages.RebootInProgress());
        dictionary.put("RestoringState", messages.RestoringState());
        dictionary.put("MigratingFrom", messages.MigratingFrom());
        dictionary.put("MigratingTo", messages.MigratingTo());
        dictionary.put("Up", messages.Up());
        dictionary.put("Paused", messages.Paused());
        dictionary.put("Suspended", messages.Suspended());
        dictionary.put("PoweringDown", messages.PoweringDown());
        dictionary.put("PoweredDown", messages.PoweredDown());
        dictionary.put("Unknown", messages.Unknown());
        dictionary.put("Unassigned", messages.Unassigned());
        dictionary.put("NotResponding", messages.NotResponding());
        dictionary.put("SavingState", messages.SavingState());
        dictionary.put("ImageLocked", messages.ImageLocked());
        dictionary.put("Down", messages.Down());
    }

    public String translate(String key) {
        return dictionary.get(key);
    }
}
