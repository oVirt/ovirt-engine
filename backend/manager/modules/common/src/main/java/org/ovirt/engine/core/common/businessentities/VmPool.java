package org.ovirt.engine.core.common.businessentities;

import java.io.Serializable;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import org.ovirt.engine.core.common.utils.ValidationUtils;
import org.ovirt.engine.core.common.validation.group.CreateEntity;
import org.ovirt.engine.core.common.validation.group.UpdateEntity;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.StringFormat;
import org.ovirt.engine.core.compat.TimeSpan;

public class VmPool extends IVdcQueryable implements Serializable, Nameable {

    private static final long serialVersionUID = 4517650877696849024L;

    private Guid id;

    @NotNull(message = "VALIDATION.VM_POOLS.NAME.NOT_NULL", groups = { CreateEntity.class, UpdateEntity.class })
    @Size(min = 1, max = BusinessEntitiesDefinitions.VM_POOL_NAME_SIZE)
    @Pattern(regexp = ValidationUtils.NO_SPECIAL_CHARACTERS_WITH_DOT, message = "ACTION_TYPE_FAILED_NAME_MAY_NOT_CONTAIN_SPECIAL_CHARS", groups = { CreateEntity.class,
            UpdateEntity.class })
    private String name;

    @Size(max = BusinessEntitiesDefinitions.GENERAL_MAX_SIZE)
    private String description;

    private VmPoolType type = VmPoolType.Automatic;

    @Size(max = BusinessEntitiesDefinitions.VM_POOL_PARAMS)
    private String parameters = "";

    private Guid vdsGroupId;

    private int prestartedVms;

    private int defaultTimeInDays;

    private TimeSpan defaultStartTime = new TimeSpan();

    private TimeSpan defaultEndTime = new TimeSpan();

    private String vdsGroupName;

    private int vmPoolAssignedCount = 1;

    private int vmPoolRunningCount = 1;

    public static final char MASK_CHARACTER = '?';

    public VmPool() {
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((defaultEndTime == null) ? 0 : defaultEndTime.hashCode());
        result = prime * result + ((defaultStartTime == null) ? 0 : defaultStartTime.hashCode());
        result = prime * result + defaultTimeInDays;
        result = prime * result + ((parameters == null) ? 0 : parameters.hashCode());
        result = prime * result + ((vdsGroupId == null) ? 0 : vdsGroupId.hashCode());
        result = prime * result + ((vdsGroupName == null) ? 0 : vdsGroupName.hashCode());
        result = prime * result + vmPoolAssignedCount;
        result = prime * result + ((description == null) ? 0 : description.hashCode());
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + vmPoolRunningCount;
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        VmPool other = (VmPool) obj;
        if (defaultEndTime == null) {
            if (other.defaultEndTime != null)
                return false;
        } else if (!defaultEndTime.equals(other.defaultEndTime))
            return false;
        if (defaultStartTime == null) {
            if (other.defaultStartTime != null)
                return false;
        } else if (!defaultStartTime.equals(other.defaultStartTime))
            return false;
        if (defaultTimeInDays != other.defaultTimeInDays)
            return false;
        if (parameters == null) {
            if (other.parameters != null)
                return false;
        } else if (!parameters.equals(other.parameters))
            return false;
        if (vdsGroupId == null) {
            if (other.vdsGroupId != null)
                return false;
        } else if (!vdsGroupId.equals(other.vdsGroupId))
            return false;
        if (description == null) {
            if (other.description != null)
                return false;
        } else if (!description.equals(other.description))
            return false;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        if (type == null) {
            if (other.type != null)
                return false;
        } else if (!type.equals(other.type))
            return false;
        return true;
    }

    private void initializeTimeLeasedDefaultData(String parameter) {
        String[] values = parameter.split("[,]", -1);
        if (values.length == 3) {
            try {
                defaultTimeInDays = Integer.parseInt(values[0]);
                String[] startTime = values[1].split("[:]", -1);
                if (startTime.length > 1) {

                    defaultStartTime = new TimeSpan(Integer.parseInt(startTime[0]), Integer.parseInt(startTime[1]), 0);
                }
                String[] endTime = values[2].split("[:]", -1);
                if (endTime.length > 1) {
                    defaultEndTime = new TimeSpan(Integer.parseInt(endTime[0]), Integer.parseInt(endTime[1]), 0);
                }
            } catch (java.lang.Exception e) {
            }
        }
    }

    public String getParameters() {
        switch (getVmPoolType()) {
        case TimeLease: {
            return StringFormat.format("%1$s,%2$s:%3$s,%4$s:%5$s", defaultTimeInDays, defaultStartTime.Hours,
                    defaultStartTime.Minutes, defaultEndTime.Hours, defaultEndTime.Minutes);
        }
        default: {
            return parameters;
        }
        }

    }

    public void setParameters(String value) {
        switch (getVmPoolType()) {
        case TimeLease: {
            initializeTimeLeasedDefaultData(value);
            break;
        }
        default: {
            parameters = value;
            break;
        }
        }

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
}
