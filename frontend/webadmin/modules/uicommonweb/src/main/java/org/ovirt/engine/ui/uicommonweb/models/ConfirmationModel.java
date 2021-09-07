package org.ovirt.engine.ui.uicommonweb.models;

import java.util.Objects;

import org.ovirt.engine.ui.uicommonweb.validation.IValidation;
import org.ovirt.engine.ui.uicommonweb.validation.SpecialAsciiI18NOrNoneValidation;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;

public class ConfirmationModel extends ListModel {

    private EntityModel<Boolean> privateLatch;

    public EntityModel<Boolean> getLatch() {
        return privateLatch;
    }

    public void setLatch(EntityModel<Boolean> value) {
        privateLatch = value;
    }

    private EntityModel<Boolean> force;

    public EntityModel<Boolean> getForce() {
        return force;
    }

    public void setForce(EntityModel<Boolean> value) {
        force = value;
    }

    private String forceLabel;

    public String getForceLabel() {
        return forceLabel;
    }

    public void setForceLabel(String forceLabel) {
        if (!Objects.equals(getForceLabel(), forceLabel)) {
            this.forceLabel = forceLabel;
            onPropertyChanged(new PropertyChangedEventArgs("ForceLabel")); //$NON-NLS-1$
        }
    }

    private String note;

    public String getNote() {
        return note;
    }

    public void setNote(String value) {
        if (!Objects.equals(note, value)) {
            note = value;
            onPropertyChanged(new PropertyChangedEventArgs("Note")); //$NON-NLS-1$
        }
    }

    private EntityModel<String> reason;

    public EntityModel<String> getReason() {
        return reason;
    }

    public void setReason(EntityModel<String> value) {
        reason = value;
    }

    private AlertType alertType;

    public AlertType getAlertType() {
        return alertType;
    }

    public void setAlertType(AlertType alertType) {
        this.alertType = alertType;
    }

    private EntityModel<Boolean> doNotShowAgain;

    public EntityModel<Boolean> getDoNotShowAgain() {
        return doNotShowAgain;
    }

    public void setDoNotShowAgain(EntityModel<Boolean> value) {
        doNotShowAgain = value;
    }

    public ConfirmationModel() {
        setTitle(ConstantsManager.getInstance().getConstants().confirmTitle());
        setLatch(new EntityModel<Boolean>());
        getLatch().setEntity(false);
        getLatch().setIsAvailable(false);

        setForce(new EntityModel<Boolean>());
        getForce().setEntity(false);
        getForce().setIsAvailable(false);

        setDoNotShowAgain(new EntityModel<Boolean>());
        getDoNotShowAgain().setEntity(false);
        getDoNotShowAgain().setIsAvailable(false);

        setReason(new EntityModel<String>());

        setAlertType(AlertType.WARNING);
    }

    public boolean validate() {
        getLatch().setIsValid(true);
        if (getLatch().getIsAvailable() && !getLatch().getEntity()) {
            getLatch().getInvalidityReasons().add(ConstantsManager.getInstance()
                    .getConstants()
                    .youMustApproveTheActionByClickingOnThisCheckboxInvalidReason());
            getLatch().setIsValid(false);
        }

        getReason().validateEntity(new IValidation[] { new SpecialAsciiI18NOrNoneValidation() });

        return getLatch().getIsValid() && getReason().getIsValid();
    }

    public enum AlertType {
        SUCCESS,
        INFO,
        WARNING,
        DANGER
    }
}
