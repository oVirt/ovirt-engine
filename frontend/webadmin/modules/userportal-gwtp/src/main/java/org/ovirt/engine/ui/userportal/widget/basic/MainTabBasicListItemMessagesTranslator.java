package org.ovirt.engine.ui.userportal.widget.basic;

import java.util.HashMap;
import java.util.Map;

import org.ovirt.engine.ui.userportal.ApplicationConstants;
import org.ovirt.engine.ui.userportal.gin.AssetProvider;
import com.google.inject.Inject;

public class MainTabBasicListItemMessagesTranslator {

    private Map<String, String> dictionary = new HashMap<>();

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    @Inject
    public MainTabBasicListItemMessagesTranslator() {
        dictionary.put("WaitForLaunch", constants.WaitForLaunch()); //$NON-NLS-1$
        dictionary.put("PoweringUp", constants.PoweringUp()); //$NON-NLS-1$
        dictionary.put("RebootInProgress", constants.RebootInProgress()); //$NON-NLS-1$
        dictionary.put("RestoringState", constants.RestoringState()); //$NON-NLS-1$
        dictionary.put("MigratingFrom", constants.MigratingFrom()); //$NON-NLS-1$
        dictionary.put("MigratingTo", constants.MigratingTo()); //$NON-NLS-1$
        dictionary.put("Up", constants.Up()); //$NON-NLS-1$
        dictionary.put("Paused", constants.Paused()); //$NON-NLS-1$
        dictionary.put("Suspended", constants.Suspended()); //$NON-NLS-1$
        dictionary.put("PoweringDown", constants.PoweringDown()); //$NON-NLS-1$
        dictionary.put("Unknown", constants.Unknown()); //$NON-NLS-1$
        dictionary.put("Unassigned", constants.Unassigned()); //$NON-NLS-1$
        dictionary.put("NotResponding", constants.NotResponding()); //$NON-NLS-1$
        dictionary.put("SavingState", constants.SavingState()); //$NON-NLS-1$
        dictionary.put("ImageLocked", constants.ImageLocked()); //$NON-NLS-1$
        dictionary.put("Down", constants.Down()); //$NON-NLS-1$
    }

    public String translate(String key) {
        return dictionary.get(key);
    }
}
