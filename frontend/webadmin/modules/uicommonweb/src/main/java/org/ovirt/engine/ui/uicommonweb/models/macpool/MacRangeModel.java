package org.ovirt.engine.ui.uicommonweb.models.macpool;

import org.ovirt.engine.core.common.businessentities.MacRange;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicommonweb.validation.IValidation;
import org.ovirt.engine.ui.uicommonweb.validation.MacAddressValidation;
import org.ovirt.engine.ui.uicommonweb.validation.MacRangeValidation;

public class MacRangeModel extends Model {

    private final MacRange macRange;

    private final EntityModel<String> leftBound = new EntityModel<>();
    private final EntityModel<String> rightBound = new EntityModel<>();
    private final EntityModel<Integer> macsCount = new EntityModel<>();

    public EntityModel<String> getLeftBound() {
        return leftBound;
    }

    public EntityModel<String> getRightBound() {
        return rightBound;
    }

    public EntityModel<Integer> getMacsCount() {
        return macsCount;
    }

    public MacRangeModel() {
        this(new MacRange());
    }

    public MacRangeModel(MacRange macRange) {
        this.macRange = macRange;
        init();
    }

    private void init() {
        leftBound.setEntity(macRange.getMacFrom() == null ? "" : macRange.getMacFrom()); //$NON-NLS-1$
        rightBound.setEntity(macRange.getMacTo() == null ? "" : macRange.getMacTo()); //$NON-NLS-1$
        recalculateMacsCount();
    }

    public MacRange flush() {
        macRange.setMacFrom(leftBound.getEntity());
        macRange.setMacTo(rightBound.getEntity());
        return macRange;
    }

    public boolean validate() {
        leftBound.validateEntity(new IValidation[] { new MacAddressValidation() });
        rightBound.validateEntity(new IValidation[] { new MacAddressValidation() });
        if (leftBound.getIsValid() && rightBound.getIsValid()) {
            rightBound.validateEntity(new IValidation[] { new MacRangeValidation(leftBound.getEntity())});
        }
        setIsValid(leftBound.getIsValid() && rightBound.getIsValid());
        return getIsValid();
    }

    public void recalculateMacsCount() {
        if (!validate()) {
            getMacsCount().setEntity(null);
            return;
        }

        String from = getLeftBound().getEntity();
        String to = getRightBound().getEntity();
        Long count = MacRangeValidation.macToLong(to) - MacRangeValidation.macToLong(from) + 1;
        getMacsCount().setEntity(count.intValue());
    }
}
