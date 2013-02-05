package org.ovirt.engine.api.restapi.types;

import org.ovirt.engine.api.model.DataCenter;
import org.ovirt.engine.api.model.Quota;
import org.ovirt.engine.api.restapi.utils.GuidUtils;


public class QuotaMapper {

    @Mapping(from = Quota.class, to = org.ovirt.engine.core.common.businessentities.Quota.class)
    public static org.ovirt.engine.core.common.businessentities.Quota map(Quota model, org.ovirt.engine.core.common.businessentities.Quota template) {
        org.ovirt.engine.core.common.businessentities.Quota entity = (template==null) ? new org.ovirt.engine.core.common.businessentities.Quota() : template;
        if (model.isSetId()) {
            entity.setId(GuidUtils.asGuid(model.getId()));
        }
        if (model.isSetName()) {
            entity.setQuotaName(model.getName());
        }
        if (model.isSetDescription()) {
            entity.setDescription(model.getDescription());
        }
        if (model.isSetDataCenter()) {
            entity.setStoragePoolId(GuidUtils.asGuid(model.getDataCenter().getId()));
        }
        return entity;
    }

    @Mapping(from = org.ovirt.engine.core.common.businessentities.Quota.class, to = Quota.class)
    public static Quota map(org.ovirt.engine.core.common.businessentities.Quota template, Quota model) {
        Quota ret = (model==null) ? new Quota() : model;
        if (template.getId()!=null) {
            ret.setId(template.getId().toString());
        }
        if (template.getQuotaName()!=null) {
            ret.setName(template.getQuotaName());
        }
        if (template.getDescription()!=null) {
            ret.setDescription(template.getDescription());
        }
        if (template.getStoragePoolId()!=null) {
            if (ret.getDataCenter()==null) {
                ret.setDataCenter(new DataCenter());
            }
            ret.getDataCenter().setId(template.getStoragePoolId().toString());
        }
        return ret;
    }
}
