package org.ovirt.engine.ui.uicommonweb.models.vms;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.CpuPinningPolicy;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.CpuPinningListModel.CpuPinningListModelItem;
import org.ovirt.engine.ui.uicommonweb.validation.IValidation;
import org.ovirt.engine.ui.uicommonweb.validation.ValidationResult;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;
import org.ovirt.engine.ui.uicompat.UIConstants;

public class CpuPinningListModel extends ListModel<CpuPinningListModelItem> {

    public static final String ITEMS_ENABLED_PROPERTY_CHANGE = "ItemsEnabledPropertyChange";//$NON-NLS-1$

    static final UIConstants constants = ConstantsManager.getInstance().getConstants();

    public CpuPinningListModel() {
        List<CpuPinningListModelItem> items = new ArrayList<>();
        items.add(new CpuPinningListModelItem(
                CpuPinningPolicy.NONE,
                constants.cpuPinningNoneDescription(),
                constants.emptyString())); // it should be always enabled
        items.add(new CpuPinningListModelItem(
                CpuPinningPolicy.MANUAL,
                constants.cpuPinningManualDescription(),
                constants.cpuPinningManualDisabled()));
        items.add(new CpuPinningListModelItem(
                CpuPinningPolicy.RESIZE_AND_PIN_NUMA,
                constants.cpuPinningResizeAndPinDescription(),
                constants.emptyString())); // it should be always enabled

        setItems(items);
        setSelectedCpuPolicy(CpuPinningPolicy.NONE);
    }

    public void setSelectedCpuPolicy(CpuPinningPolicy policy) {
        for (CpuPinningListModelItem item : getItems()) {
            if (item.getPolicy() == policy) {
                setSelectedItem(item);
            }
        }
    }

    public void setCpuPolicyEnabled(CpuPinningPolicy policy, boolean enabled) {
        for (CpuPinningListModelItem item : getItems()) {
            if (item.getPolicy() == policy) {
                item.setEnabled(enabled);
            }
        }
    }

    public void validateSelectedItem() {
        super.validateSelectedItem(new IValidation[] {new CpuPinningListModelItemValidation()});
    }

    public class CpuPinningListModelItem {

        private CpuPinningPolicy policy;

        private boolean enabled;

        private String description;

        private String disablementReason;

        public CpuPinningListModelItem(CpuPinningPolicy policy, String description, String disablementReason) {
            super();
            this.policy = policy;
            this.description = description;
            this.disablementReason = disablementReason;
            this.enabled = true;
        }

        public CpuPinningPolicy getPolicy() {
            return policy;
        }

        public void setPolicy(CpuPinningPolicy policy) {
            this.policy = policy;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            if (this.enabled != enabled) {
                this.enabled = enabled;
                onPropertyChanged(new PropertyChangedEventArgs(ITEMS_ENABLED_PROPERTY_CHANGE));
            }
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getDisablementReason() {
            return disablementReason;
        }

        public void setDisablementReason(String disablementReason) {
            this.disablementReason = disablementReason;
        }
    }

    public class CpuPinningListModelItemValidation implements IValidation {

        @Override
        public ValidationResult validate(Object value) {
            if (value instanceof CpuPinningListModelItem) {
                ValidationResult result = new ValidationResult();
                result.setSuccess(((CpuPinningListModelItem) value).isEnabled());
                return result;
            }
            return ValidationResult.fail();
        }
    }
}
