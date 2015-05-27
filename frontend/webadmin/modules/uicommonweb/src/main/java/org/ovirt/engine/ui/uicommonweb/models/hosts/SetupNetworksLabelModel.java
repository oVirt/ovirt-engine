package org.ovirt.engine.ui.uicommonweb.models.hosts;

import java.util.Set;

import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicommonweb.validation.IValidation;
import org.ovirt.engine.ui.uicommonweb.validation.NotEmptyValidation;
import org.ovirt.engine.ui.uicommonweb.validation.ValidationResult;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

public class SetupNetworksLabelModel extends Model {

    private final EntityModel<String> label = new EntityModel<>();
    private final Set<String> dcLabels;

    public EntityModel<String> getLabel() {
        return label;
    }

    public SetupNetworksLabelModel(Set<String> dcLabels) {
        this.dcLabels = dcLabels;
        setTitle(ConstantsManager.getInstance().getConstants().addNewLabelTitle());
    }

    public boolean validate() {
        label.validateEntity(new IValidation[] { new NotEmptyValidation(), new LabelNotTakenValidation() });
        return label.getIsValid();
    }

    private class LabelNotTakenValidation implements IValidation {

        @Override
        public ValidationResult validate(Object value) {
            ValidationResult res = new ValidationResult();
            if (dcLabels.contains(value)) {
                res.setSuccess(false);
                res.getReasons().add(ConstantsManager.getInstance().getConstants().labelAlreadyExists());
            }
            return res;
        }
    }

}
