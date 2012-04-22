package org.ovirt.engine.ui.userportal.widget.basic;

import java.util.HashMap;
import java.util.Map;

import org.ovirt.engine.ui.userportal.section.main.view.tab.basic.MainTabBasicListItemMessages;

import com.google.inject.Inject;

public class MainTabBasicListItemMessagesTranslator {

    private Map<String, String> dictionary = new HashMap<String, String>();

    @Inject
    public MainTabBasicListItemMessagesTranslator(MainTabBasicListItemMessages messages) {
        dictionary.put("WaitForLaunch", messages.WaitForLaunch()); //$NON-NLS-1$
        dictionary.put("PoweringUp", messages.PoweringUp()); //$NON-NLS-1$
        dictionary.put("RebootInProgress", messages.RebootInProgress()); //$NON-NLS-1$
        dictionary.put("RestoringState", messages.RestoringState()); //$NON-NLS-1$
        dictionary.put("MigratingFrom", messages.MigratingFrom()); //$NON-NLS-1$
        dictionary.put("MigratingTo", messages.MigratingTo()); //$NON-NLS-1$
        dictionary.put("Up", messages.Up()); //$NON-NLS-1$
        dictionary.put("Paused", messages.Paused()); //$NON-NLS-1$
        dictionary.put("Suspended", messages.Suspended()); //$NON-NLS-1$
        dictionary.put("PoweringDown", messages.PoweringDown()); //$NON-NLS-1$
        dictionary.put("PoweredDown", messages.PoweredDown()); //$NON-NLS-1$
        dictionary.put("Unknown", messages.Unknown()); //$NON-NLS-1$
        dictionary.put("Unassigned", messages.Unassigned()); //$NON-NLS-1$
        dictionary.put("NotResponding", messages.NotResponding()); //$NON-NLS-1$
        dictionary.put("SavingState", messages.SavingState()); //$NON-NLS-1$
        dictionary.put("ImageLocked", messages.ImageLocked()); //$NON-NLS-1$
        dictionary.put("Down", messages.Down()); //$NON-NLS-1$
    }

    public String translate(String key) {
        return dictionary.get(key);
    }
}
