package org.ovirt.engine.ui.uicommonweb.models.vms;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.Set;
import java.util.TreeSet;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.ovirt.engine.ui.uicommonweb.junit.UiCommonSetupExtension;

/**
 * Tests {@link VmModelBehaviorBase#applyI440fxDefault(Set)}.
 */
@ExtendWith(UiCommonSetupExtension.class)
public class VmModelBehaviorI440fxDefaultTest {

    /** Minimal concrete subclass — abstract dialog-wiring methods are no-ops. */
    private static class TestBehavior extends VmModelBehaviorBase {
        @Override public void defaultHost_SelectedItemChanged() { }
        @Override public void provisioning_SelectedItemChanged() { }
    }

    private UnitVmModel model;
    private VmModelBehaviorBase behavior;

    @BeforeEach
    public void setUp() {
        behavior = new TestBehavior();
        model = new UnitVmModel(behavior, null);
    }

    @Test
    public void emptyIntersectionLeavesFieldBlank() {
        // No i440fx type common to all hosts — dialog must force manual choice.
        behavior.applyI440fxDefault(new TreeSet<>());

        assertNull(model.getEmulatedMachine().getSelectedItem());
    }

    @Test
    public void singleTypeInIntersectionSelectsIt() {
        Set<String> common = new TreeSet<>();
        common.add("pc-i440fx-rhel10.0.0"); //$NON-NLS-1$

        behavior.applyI440fxDefault(common);

        assertEquals("pc-i440fx-rhel10.0.0", model.getEmulatedMachine().getSelectedItem()); //$NON-NLS-1$
    }

    @Test
    public void multipleTypesInIntersectionPicksNewest() {
        // EL10.0 + EL10.2 hosts both advertise rhel10.0.0; rhel10.2.0 is only on EL10.2.
        // The caller already gave us the intersection, so commonMachines = {rhel10.0.0}.
        // Here we test with two types to verify naturalOrder() picks the newer one.
        Set<String> common = new TreeSet<>();
        common.add("pc-i440fx-rhel10.0.0"); //$NON-NLS-1$
        common.add("pc-i440fx-rhel7.6.0"); //$NON-NLS-1$

        behavior.applyI440fxDefault(common);

        assertEquals("pc-i440fx-rhel10.0.0", model.getEmulatedMachine().getSelectedItem()); //$NON-NLS-1$
    }

    @Test
    public void floatingPcAliasExcludedFromSelection() {
        // "pc" is a floating alias that changes meaning across QEMU versions;
        // it must never be auto-selected as the pinned value.
        Set<String> common = new TreeSet<>();
        common.add("pc"); //$NON-NLS-1$
        common.add("pc-i440fx-rhel7.6.0"); //$NON-NLS-1$

        behavior.applyI440fxDefault(common);

        assertEquals("pc-i440fx-rhel7.6.0", model.getEmulatedMachine().getSelectedItem()); //$NON-NLS-1$
    }

    @Test
    public void onlyPcAliasInSetFallsBackToIt() {
        // Degenerate case: only the floating alias is present. The filter strips all
        // versioned names, so orElse falls back to the first element.
        Set<String> common = new TreeSet<>();
        common.add("pc"); //$NON-NLS-1$

        behavior.applyI440fxDefault(common);

        assertEquals("pc", model.getEmulatedMachine().getSelectedItem()); //$NON-NLS-1$
    }
}
