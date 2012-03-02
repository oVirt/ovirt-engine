package org.ovirt.engine.ui.userportal.section.main.view.tab.basic;

import com.google.gwt.i18n.client.Messages;

public interface MainTabBasicListItemMessages extends Messages {

    // machine status messages
    @DefaultMessage("Powering Up")
    String WaitForLaunch();

    @DefaultMessage("Powering Up")
    String PoweringUp();

    @DefaultMessage("Powering Up")
    String RebootInProgress();

    @DefaultMessage("Powering Up")
    String RestoringState();

    @DefaultMessage("Machine is Ready")
    String MigratingFrom();

    @DefaultMessage("Machine is Ready")
    String MigratingTo();

    @DefaultMessage("Machine is Ready")
    String Up();

    @DefaultMessage("Paused")
    String Paused();

    @DefaultMessage("Paused")
    String Suspended();

    @DefaultMessage("Powering Down")
    String PoweringDown();

    @DefaultMessage("Powering Down")
    String PoweredDown();

    @DefaultMessage("Not Available")
    String Unknown();

    @DefaultMessage("Not Available")
    String Unassigned();

    @DefaultMessage("Not Available")
    String NotResponding();

    @DefaultMessage("Please Wait..")
    String SavingState();

    @DefaultMessage("Please Wait..")
    String ImageLocked();

    @DefaultMessage("Machine is Down")
    String Down();

    // machine messages
    @DefaultMessage("Shutdown VM")
    String shutdownVm();

    @DefaultMessage("Suspend VM")
    String suspendVm();

    @DefaultMessage("Take VM")
    String takeVm();

    @DefaultMessage("Run VM")
    String runVm();

    @DefaultMessage("Double Click for Console")
    String doubleClickForConsole();
}
