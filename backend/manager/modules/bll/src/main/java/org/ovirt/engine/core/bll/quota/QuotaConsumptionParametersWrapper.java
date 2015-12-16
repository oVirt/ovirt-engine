package org.ovirt.engine.core.bll.quota;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableBase;

public class QuotaConsumptionParametersWrapper implements Cloneable {

    private List<QuotaConsumptionParameter> parameters;

    private AuditLogableBase auditLogable;
    private List<String> validationMessages;

    public QuotaConsumptionParametersWrapper(AuditLogableBase auditLogable, List<String> validationMessages) {
        this.auditLogable = auditLogable;
        this.validationMessages = validationMessages;
    }

    public List<String> getValidationMessages() {
        return validationMessages;
    }

    public void setValidationMessages(List<String> validationMessages) {
        this.validationMessages = validationMessages;
    }

    public StoragePool getStoragePool() {
        return auditLogable.getStoragePool();
    }

    public Guid getStoragePoolId() {
        return getStoragePool().getId();
    }

    public List<QuotaConsumptionParameter> getParameters() {
        return parameters;
    }

    public void setParameters(List<QuotaConsumptionParameter> parameters) {
        this.parameters = parameters;
    }

    public AuditLogableBase getAuditLogable() {
        return this.auditLogable;
    }

    public void setAuditLogable(AuditLogableBase auditLogable) {
        this.auditLogable = auditLogable;
    }

    @Override
    public QuotaConsumptionParametersWrapper clone() throws CloneNotSupportedException {
        super.clone();
        QuotaConsumptionParametersWrapper cloneWrapper = new QuotaConsumptionParametersWrapper(getAuditLogable(),
                validationMessages);

        if (getParameters() != null) {
            cloneWrapper.setParameters(new ArrayList<>());
            for (QuotaConsumptionParameter parameter : getParameters()) {
                cloneWrapper.getParameters().add(parameter.clone());
            }
        }

        return cloneWrapper;
    }
}
