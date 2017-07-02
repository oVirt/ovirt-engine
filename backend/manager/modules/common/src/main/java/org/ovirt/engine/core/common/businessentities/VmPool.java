package org.ovirt.engine.core.common.businessentities;

import java.util.Objects;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import org.ovirt.engine.core.common.utils.ValidationUtils;
import org.ovirt.engine.core.common.validation.annotation.ValidUri;
import org.ovirt.engine.core.common.validation.group.CreateEntity;
import org.ovirt.engine.core.common.validation.group.UpdateEntity;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.TimeSpan;

public class VmPool implements Queryable, BusinessEntity<Guid>, Nameable, Commented {

    private static final long serialVersionUID = 4517650877696849024L;

    private Guid id;

    @NotNull(message = "VALIDATION_VM_POOLS_NAME_NOT_NULL", groups = { CreateEntity.class, UpdateEntity.class })
    @Size(min = 1, max = BusinessEntitiesDefinitions.VM_POOL_NAME_SIZE)
    @Pattern(regexp = ValidationUtils.POOL_NAME_PATTERN, message = "ACTION_TYPE_FAILED_INVALID_POOL_NAME", groups = { CreateEntity.class,
            UpdateEntity.class })
    private String name;

    @Size(max = BusinessEntitiesDefinitions.GENERAL_MAX_SIZE)
    private String description;

    private String comment;

    private VmPoolType type;

    private boolean stateful;

    @Size(max = BusinessEntitiesDefinitions.VM_POOL_PARAMS)
    private String parameters;

    private Guid clusterId;

    private int prestartedVms;

    private int defaultTimeInDays;

    private TimeSpan defaultStartTime;

    private TimeSpan defaultEndTime;

    private String clusterName;

    private int vmPoolAssignedCount;

    private int vmPoolRunningCount;

    @Min(value = 1, message = "VALIDATION_VM_POOLS_NUMBER_OF_MAX_ASSIGNED_VMS_OUT_OF_RANGE")
    @Max(value = Short.MAX_VALUE, message = "VALIDATION_VM_POOLS_NUMBER_OF_MAX_ASSIGNED_VMS_OUT_OF_RANGE")
    private int maxAssignedVmsPerUser;

    public static final char MASK_CHARACTER = '?';

    @ValidUri(message = "VALIDATION_CLUSTER_SPICE_PROXY_HOSTNAME_OR_IP", groups = { CreateEntity.class, UpdateEntity.class })
    @Size(max = BusinessEntitiesDefinitions.SPICE_PROXY_ADDR_SIZE)
    private String spiceProxy;

    private boolean beingDestroyed;
    private boolean autoSelectStorage;

    public VmPool() {
        parameters = "";
        defaultStartTime = new TimeSpan();
        defaultEndTime = new TimeSpan();
        vmPoolAssignedCount = 1;
        vmPoolRunningCount = 1;
        maxAssignedVmsPerUser = 1;
        beingDestroyed = false;
        autoSelectStorage = false;
        type = VmPoolType.AUTOMATIC;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                id,
                defaultTimeInDays,
                parameters,
                clusterId,
                description,
                name,
                type,
                stateful,
                maxAssignedVmsPerUser,
                spiceProxy
        );
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof VmPool)) {
            return false;
        }
        VmPool other = (VmPool) obj;
        // Don't use defaultStartTime and defaultEndTime in equals method
        // as they will never match because of how they are initialized.
        return Objects.equals(id, other.id)
                && defaultTimeInDays == other.defaultTimeInDays
                && Objects.equals(parameters, other.parameters)
                && Objects.equals(clusterId, other.clusterId)
                && Objects.equals(description, other.description)
                && Objects.equals(name, other.name)
                && Objects.equals(type, other.type)
                && Objects.equals(stateful, other.stateful)
                && maxAssignedVmsPerUser == other.maxAssignedVmsPerUser
                && Objects.equals(spiceProxy, other.spiceProxy);
    }

    @Override
    public Guid getId() {
        return id;
    }

    @Override
    public void setId(Guid id) {
        this.id = id;
    }

    public String getParameters() {
        return parameters;
    }

    public void setParameters(String value) {
        parameters = value;
    }

    public int getDefaultTimeInDays() {
        return defaultTimeInDays;
    }

    public void setDefaultTimeInDays(int value) {
        defaultTimeInDays = value;
    }

    public TimeSpan getDefaultStartTime() {
        return defaultStartTime;
    }

    public void setDefaultStartTime(TimeSpan value) {
        defaultStartTime = value;
    }

    public TimeSpan getDefaultEndTime() {
        return defaultEndTime;
    }

    public void setDefaultEndTime(TimeSpan value) {
        defaultEndTime = value;
    }

    public int getPrestartedVms() {
        return prestartedVms;
    }

    public void setPrestartedVms(int prestartedVms) {
        this.prestartedVms = prestartedVms;
    }

    public String getVmPoolDescription() {
        return this.description;
    }

    public void setVmPoolDescription(String value) {
        this.description = value;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String value) {
        comment = value;
    }

    public Guid getVmPoolId() {
        return this.id;
    }

    public void setVmPoolId(Guid value) {
        this.id = value;
    }

    @Override
    public String getName() {
        return this.name;
    }

    public void setName(String value) {
        this.name = value;
    }

    public int getAssignedVmsCount() {
        return vmPoolAssignedCount;
    }

    public void setAssignedVmsCount(int value) {
        vmPoolAssignedCount = value;
    }

    public int getMaxAssignedVmsPerUser() {
        return maxAssignedVmsPerUser;
    }

    public void setMaxAssignedVmsPerUser(int maxAssignedVmsPerUser) {
        this.maxAssignedVmsPerUser = maxAssignedVmsPerUser;
    }

    public int getRunningVmsCount() {
        return vmPoolRunningCount;
    }

    public void setRunningVmsCount(int value) {
        vmPoolRunningCount = value;
    }

    public VmPoolType getVmPoolType() {
        return type;
    }

    public void setVmPoolType(VmPoolType value) {
        this.type = value;
    }

    public boolean isStateful() {
        return stateful;
    }

    public void setStateful(boolean stateful) {
        this.stateful = stateful;
    }

    public Guid getClusterId() {
        return this.clusterId;
    }

    public void setClusterId(Guid value) {
        this.clusterId = value;
    }

    public String getClusterName() {
        return this.clusterName;
    }

    public void setClusterName(String value) {
        this.clusterName = value;
    }

    @Override
    public Object getQueryableId() {
        return getVmPoolId();
    }

    public void setSpiceProxy(String spiceProxy) {
        this.spiceProxy = spiceProxy;
    }

    public String getSpiceProxy() {
        return spiceProxy;
    }

    public boolean isBeingDestroyed() {
        return beingDestroyed;
    }

    public void setBeingDestroyed(boolean beingDestroyed) {
        this.beingDestroyed = beingDestroyed;
    }

    public boolean isAutoStorageSelect() {
        return autoSelectStorage;
    }

    public void setAutoStorageSelect(boolean autoSelect) {
        this.autoSelectStorage = autoSelect;
    }

    @Override
    public String toString() {
        return "VmPool [" + getName() + "]";
    }

}
