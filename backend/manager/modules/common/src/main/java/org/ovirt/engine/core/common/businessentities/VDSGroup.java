package org.ovirt.engine.core.common.businessentities;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.ovirt.engine.core.common.businessentities.mapping.GuidType;
import org.ovirt.engine.core.common.validation.annotation.ValidI18NName;
import org.ovirt.engine.core.common.validation.annotation.ValidVdsGroup;
import org.ovirt.engine.core.common.validation.group.CreateEntity;
import org.ovirt.engine.core.common.validation.group.UpdateEntity;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.NGuid;
import org.ovirt.engine.core.compat.Version;

@Entity
@Table(name = "vds_groups")
@TypeDef(name = "guid", typeClass = GuidType.class)
@NamedQueries(
              value = {
                      @NamedQuery(
                                  name = "vdsgroup_with_running_vms",
                                  query = "from VDSGroup g where g.id = :vds_group_id and :vds_group_id in (select s.vds_group_id from VmStatic s, VmDynamic d where d.status not in (0, 13, 14) and d.id = s.id)")
              })
@ValidVdsGroup(groups = { CreateEntity.class })
public class VDSGroup extends IVdcQueryable implements Serializable, BusinessEntity<Guid>, HasStoragePool<NGuid> {


    private static final long serialVersionUID = 5659359762655478095L;

    public static final Guid DEFAULT_VDS_GROUP_ID = new Guid("99408929-82CF-4DC7-A532-9D998063FA95");

    @Id
    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "org.ovirt.engine.core.dao.GuidGenerator")
    @Column(name = "vds_group_id")
    @Type(type = "guid")
    private Guid id;

    @NotNull(message = "VALIDATION.VDS_GROUP.NAME.NOT_NULL", groups = { CreateEntity.class, UpdateEntity.class })
    @Size(min = 1, max = BusinessEntitiesDefinitions.CLUSTER_NAME_SIZE, message = "VALIDATION.VDS_GROUP.NAME.MAX",
            groups = {
            CreateEntity.class, UpdateEntity.class })
    @ValidI18NName(message = "VALIDATION.VDS_GROUP.NAME.INVALID", groups = { CreateEntity.class, UpdateEntity.class })
    @Column(name = "name")
    private String name = ""; // GREGM Prevents NPE

    @Size(max = BusinessEntitiesDefinitions.GENERAL_MAX_SIZE)
    @Column(name = "description")
    private String description;

    @Size(max = BusinessEntitiesDefinitions.CLUSTER_CPU_NAME_SIZE)
    @Column(name = "cpu_name")
    private String cpu_name;

    @Column(name = "selection_algorithm")
    private VdsSelectionAlgorithm selection_algorithm = VdsSelectionAlgorithm.None;

    @Column(name = "high_utilization")
    private int high_utilization = 0;

    @Column(name = "low_utilization")
    private int low_utilization = 0;

    @Column(name = "cpu_over_commit_duration_minutes")
    private int cpu_over_commit_duration_minutes = 0;

    @Column(name = "storage_pool_id")
    @Type(type = "guid")
    private NGuid storagePoolId;

    @Column(name = "storage_pool_name")
    private String storagePoolName;

    @Column(name = "max_vds_memory_over_commit")
    private int max_vds_memory_over_commit = 0;

    @Column(name = "count_threads_as_cores")
    private boolean countThreadsAsCores = false;

    @Size(max = BusinessEntitiesDefinitions.GENERAL_VERSION_SIZE)
    @Column(name = "compatibility_version")
    private String compatibility_version;

    @Transient
    private Version compatVersion;

    @Column(name = "transparent_hugepages")
    private boolean transparentHugepages;

    @NotNull(message = "VALIDATION.VDS_GROUP.MigrateOnError.NOT_NULL")
    @Column(name = "migrate_on_error")
    private MigrateOnErrorOptions migrateOnError;

    @Column(name = "virt_service")
    private boolean virtService = true;

    @Column(name = "gluster_service")
    private boolean glusterService = false;

    public VDSGroup() {
        selection_algorithm = VdsSelectionAlgorithm.None;
        high_utilization = -1;
        low_utilization = -1;
        cpu_over_commit_duration_minutes = -1;
        migrateOnError = MigrateOnErrorOptions.YES;
    }

    public VDSGroup(String name, String description, String cpu_name) {
        this();
        this.name = name;
        this.description = description;
        this.cpu_name = cpu_name;
    }

    @Override
    public Guid getId() {
        return id;
    }

    @Override
    public void setId(Guid value) {
        id = value;
    }

    public void setvds_group_id(Guid value) {
        setId(value);
    }

    public String getname() {
        return name;
    }

    public void setname(String value) {
        name = value;
    }

    public String getdescription() {
        return description;
    }

    public void setdescription(String value) {
        description = value;
    }

    public String getcpu_name() {
        return this.cpu_name;
    }

    public void setcpu_name(String value) {
        this.cpu_name = value;
    }

    public VdsSelectionAlgorithm getselection_algorithm() {
        return selection_algorithm;
    }

    public void setselection_algorithm(VdsSelectionAlgorithm value) {
        selection_algorithm = value;
    }

    public int gethigh_utilization() {
        return this.high_utilization;
    }

    public void sethigh_utilization(int value) {
        this.high_utilization = value;
    }

    public int getlow_utilization() {
        return this.low_utilization;
    }

    public void setlow_utilization(int value) {
        this.low_utilization = value;
    }

    public int getcpu_over_commit_duration_minutes() {
        return this.cpu_over_commit_duration_minutes;
    }

    public void setcpu_over_commit_duration_minutes(int value) {
        this.cpu_over_commit_duration_minutes = value;
    }

    @Override
    public NGuid getStoragePoolId() {
        return storagePoolId;
    }

    @Override
    public void setStoragePoolId(NGuid storagePool) {
        this.storagePoolId = storagePool;
    }

    public String getStoragePoolName() {
        return this.storagePoolName;
    }

    public void setStoragePoolName(String value) {
        this.storagePoolName = value;
    }

    public int getmax_vds_memory_over_commit() {
        return this.max_vds_memory_over_commit;
    }

    public void setmax_vds_memory_over_commit(int value) {
        this.max_vds_memory_over_commit = value;
    }

    public boolean getCountThreadsAsCores() {
        return this.countThreadsAsCores;
    }

    public void setCountThreadsAsCores(boolean value) {
        this.countThreadsAsCores = value;
    }

    public Version getcompatibility_version() {
        return compatVersion;
    }

    public boolean getTransparentHugepages() {
        return this.transparentHugepages;
    }

    public void setTransparentHugepages(boolean value) {
        this.transparentHugepages = value;
    }

    public void setcompatibility_version(Version value) {
        compatibility_version = value.getValue();
        compatVersion = value;
    }

    @Override
    public Object getQueryableId() {
        return getId();
    }

    public void setMigrateOnError(MigrateOnErrorOptions migrateOnError) {
        this.migrateOnError = migrateOnError;
    }

    public MigrateOnErrorOptions getMigrateOnError() {
        return migrateOnError;
    }

    public void setVirtService(boolean virtService) {
        this.virtService = virtService;
    }

    public boolean supportsVirtService() {
        return virtService;
    }

    public void setGlusterService(boolean glusterService) {
        this.glusterService = glusterService;
    }

    public boolean supportsGlusterService() {
        return glusterService;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((compatVersion == null) ? 0 : compatVersion.hashCode());
        result = prime * result + ((compatibility_version == null) ? 0 : compatibility_version.hashCode());
        result = prime * result + ((cpu_name == null) ? 0 : cpu_name.hashCode());
        result =
            prime
            * result
            + cpu_over_commit_duration_minutes;
        result = prime * result + ((description == null) ? 0 : description.hashCode());
        result = prime * result + high_utilization;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + low_utilization;
        result = prime * result + max_vds_memory_over_commit;
        result = prime * result + (countThreadsAsCores ? 1231 : 1237);
        result = prime * result + ((migrateOnError == null) ? 0 : migrateOnError.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((selection_algorithm == null) ? 0 : selection_algorithm.hashCode());
        result = prime * result + ((storagePoolId == null) ? 0 : storagePoolId.hashCode());
        result = prime * result + ((storagePoolName == null) ? 0 : storagePoolName.hashCode());
        result = prime * result + (transparentHugepages ? 1231 : 1237);
        result = prime * result + (virtService ? 1231 : 1237);
        result = prime * result + (glusterService ? 1231 : 1237);
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
        VDSGroup other = (VDSGroup) obj;
        if (compatVersion == null) {
            if (other.compatVersion != null)
                return false;
        } else if (!compatVersion.equals(other.compatVersion))
            return false;
        if (compatibility_version == null) {
            if (other.compatibility_version != null)
                return false;
        } else if (!compatibility_version.equals(other.compatibility_version))
            return false;
        if (cpu_name == null) {
            if (other.cpu_name != null)
                return false;
        } else if (!cpu_name.equals(other.cpu_name))
            return false;
        if (cpu_over_commit_duration_minutes != other.cpu_over_commit_duration_minutes)
            return false;
        if (description == null) {
            if (other.description != null)
                return false;
        } else if (!description.equals(other.description))
        if (high_utilization != other.high_utilization)
            return false;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        if (low_utilization != other.low_utilization)
            return false;
        if (max_vds_memory_over_commit != other.max_vds_memory_over_commit)
            return false;
        if (countThreadsAsCores != other.countThreadsAsCores)
            return false;
        if (migrateOnError != other.migrateOnError)
            return false;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        if (selection_algorithm == null) {
            if (other.selection_algorithm != null)
                return false;
        } else if (!selection_algorithm.equals(other.selection_algorithm))
            return false;
        if (storagePoolId == null) {
            if (other.storagePoolId != null)
                return false;
        } else if (!storagePoolId.equals(other.storagePoolId))
            return false;
        if (storagePoolName == null) {
            if (other.storagePoolName != null)
                return false;
        } else if (!storagePoolName.equals(other.storagePoolName))
            return false;
        if (transparentHugepages != other.transparentHugepages)
            return false;
        if (virtService != other.virtService) {
            return false;
        }
        if (glusterService != other.glusterService) {
            return false;
        }
        return true;
    }

}
