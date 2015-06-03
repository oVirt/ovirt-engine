package org.ovirt.engine.core.common.businessentities;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import org.ovirt.engine.core.common.utils.ObjectUtils;
import org.ovirt.engine.core.common.utils.ValidationUtils;
import org.ovirt.engine.core.common.validation.annotation.ValidUri;
import org.ovirt.engine.core.common.validation.group.CreateEntity;
import org.ovirt.engine.core.common.validation.group.UpdateEntity;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.TimeSpan;

public class VmPool implements IVdcQueryable, Nameable, Commented {

    private static final long serialVersionUID = 4517650877696849024L;

    private Guid id;

    @NotNull(message = "VALIDATION.VM_POOLS.NAME.NOT_NULL", groups = { CreateEntity.class, UpdateEntity.class })
    @Size(min = 1, max = BusinessEntitiesDefinitions.VM_POOL_NAME_SIZE)
    @Pattern(regexp = ValidationUtils.POOL_NAME_PATTERN, message = "ACTION_TYPE_FAILED_INVALID_POOL_NAME", groups = { CreateEntity.class,
            UpdateEntity.class })
    private String name;

    @Size(max = BusinessEntitiesDefinitions.GENERAL_MAX_SIZE)
    private String description;

    private String comment;

    private VmPoolType type;

    @Size(max = BusinessEntitiesDefinitions.VM_POOL_PARAMS)
    private String parameters;

    private Guid vdsGroupId;

    private int prestartedVms;

    private int defaultTimeInDays;

    private TimeSpan defaultStartTime;

    private TimeSpan defaultEndTime;

    private String vdsGroupName;

    private int vmPoolAssignedCount;

    private int vmPoolRunningCount;

    @Min(value = 1, message = "VALIDATION.VM_POOLS.NUMBER_OF_MAX_ASSIGNED_VMS_OUT_OF_RANGE")
    @Max(value = Short.MAX_VALUE, message = "VALIDATION.VM_POOLS.NUMBER_OF_MAX_ASSIGNED_VMS_OUT_OF_RANGE")
    private int maxAssignedVmsPerUser;

    public static final char MASK_CHARACTER = '?';

    @ValidUri(message = "VALIDATION.VDS_GROUP.SPICE_PROXY.HOSTNAME_OR_IP", groups = { CreateEntity.class, UpdateEntity.class })
    @Size(max = BusinessEntitiesDefinitions.SPICE_PROXY_ADDR_SIZE)
    private String spiceProxy;

    public VmPool() {
        parameters = "";
        defaultStartTime = new TimeSpan();
        defaultEndTime = new TimeSpan();
        vmPoolAssignedCount = 1;
        vmPoolRunningCount = 1;
        maxAssignedVmsPerUser = 1;
        type = VmPoolType.Automatic;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + defaultTimeInDays;
        result = prime * result + ((parameters == null) ? 0 : parameters.hashCode());
        result = prime * result + ((vdsGroupId == null) ? 0 : vdsGroupId.hashCode());
        result = prime * result + ((description == null) ? 0 : description.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        result = prime * result + maxAssignedVmsPerUser;
        result = prime * result + ((spiceProxy == null) ? 0 : spiceProxy.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        VmPool other = (VmPool) obj;
        // Don't use defaultStartTime and defaultEndTime in equals method
        // as they will never match because of how they are initialized.
        return (ObjectUtils.objectsEqual(id, other.id)
                && defaultTimeInDays == other.defaultTimeInDays
                && ObjectUtils.objectsEqual(parameters, other.parameters)
                && ObjectUtils.objectsEqual(vdsGroupId, other.vdsGroupId)
                && ObjectUtils.objectsEqual(description, other.description)
                && ObjectUtils.objectsEqual(name, other.name)
                && ObjectUtils.objectsEqual(type, other.type)
                && maxAssignedVmsPerUser == other.maxAssignedVmsPerUser
                && ObjectUtils.objectsEqual(spiceProxy, other.spiceProxy));
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

    public Guid getVdsGroupId() {
        return this.vdsGroupId;
    }

    public void setVdsGroupId(Guid value) {
        this.vdsGroupId = value;
    }

    public String getVdsGroupName() {
        return this.vdsGroupName;
    }

    public void setVdsGroupName(String value) {
        this.vdsGroupName = value;
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
}
