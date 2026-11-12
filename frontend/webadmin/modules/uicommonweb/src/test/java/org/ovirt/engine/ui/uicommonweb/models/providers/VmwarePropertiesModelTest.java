package org.ovirt.engine.ui.uicommonweb.models.providers;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.ovirt.engine.ui.uicommonweb.junit.UiCommonSetupExtension;

@ExtendWith(UiCommonSetupExtension.class)
public class VmwarePropertiesModelTest {

    private VmwarePropertiesModel model;

    @BeforeEach
    public void setUp() {
        model = new VmwarePropertiesModel();
        model.setIsAvailable(true);
        model.getvCenter().setEntity("vcenter.example.com"); //$NON-NLS-1$
        model.getEsx().setEntity("esx.example.com"); //$NON-NLS-1$
        model.getVmwareDatacenter().setEntity("dc"); //$NON-NLS-1$
    }

    @Test
    public void vCenterWithoutPortIsValid() {
        assertTrue(model.validate());
    }

    @Test
    public void vCenterWithPortIsValid() {
        model.getvCenter().setEntity("vcenter.example.com:43543"); //$NON-NLS-1$
        assertTrue(model.validate());
        assertTrue(model.getvCenter().getIsValid());
    }

    @Test
    public void vCenterWithProtocolIsInvalid() {
        model.getvCenter().setEntity("https://vcenter.example.com:43543"); //$NON-NLS-1$
        assertFalse(model.validate());
        assertFalse(model.getvCenter().getIsValid());
    }

    @Test
    public void esxWithPortIsInvalid() {
        model.getEsx().setEntity("esx.example.com:443"); //$NON-NLS-1$
        assertFalse(model.validate());
        assertFalse(model.getEsx().getIsValid());
    }
}
