package org.ovirt.engine.core.common.businessentities;

import java.io.Serializable;
import java.util.Objects;

import org.ovirt.engine.core.compat.Guid;

public class vds_spm_id_map implements Serializable, BusinessEntity<Guid> {
    private static final long serialVersionUID = 308472653338744675L;

    public vds_spm_id_map() {
        storage_pool_idField = Guid.Empty;
    }

    public vds_spm_id_map(Guid storage_pool_id, Guid vds_id, int vds_spm_id) {
        this.storage_pool_idField = storage_pool_id;
        this.vds_idField = vds_id;
        this.vds_spm_idField = vds_spm_id;
    }

    private Guid storage_pool_idField;

    public Guid getstorage_pool_id() {
        return this.storage_pool_idField;
    }

    public void setstorage_pool_id(Guid value) {
        this.storage_pool_idField = value;
    }

    private Guid vds_idField;

    @Override
    public Guid getId() {
        return this.vds_idField;
    }

    @Override
    public void setId(Guid value) {
        this.vds_idField = value;
    }

    private int vds_spm_idField;

    public int getvds_spm_id() {
        return this.vds_spm_idField;
    }

    public void setvds_spm_id(int value) {
        this.vds_spm_idField = value;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                storage_pool_idField,
                vds_idField,
                vds_spm_idField
        );
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof vds_spm_id_map)) {
            return false;
        }
        vds_spm_id_map other = (vds_spm_id_map) obj;
        return Objects.equals(storage_pool_idField, other.storage_pool_idField)
                && Objects.equals(vds_idField, other.vds_idField)
                && vds_spm_idField == other.vds_spm_idField;
    }
}
