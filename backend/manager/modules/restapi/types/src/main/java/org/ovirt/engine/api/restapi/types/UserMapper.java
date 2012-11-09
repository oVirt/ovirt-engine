package org.ovirt.engine.api.restapi.types;

import org.ovirt.engine.api.model.Domain;
import org.ovirt.engine.api.model.Group;
import org.ovirt.engine.api.model.Groups;
import org.ovirt.engine.api.model.User;
import org.ovirt.engine.core.common.businessentities.AdUser;
import org.ovirt.engine.core.common.businessentities.DbUser;
import org.ovirt.engine.core.common.businessentities.ad_groups;
import org.ovirt.engine.core.common.users.VdcUser;
import org.ovirt.engine.core.compat.NGuid;
import org.ovirt.engine.core.compat.StringHelper;

public class UserMapper {

    @Mapping(from = DbUser.class, to = User.class)
    public static User map(DbUser entity, User template) {
        User model = template != null ? template : new User();
        model.setName(entity.getname());
        model.setUserName(entity.getusername());
        model.setId(entity.getuser_id().toString());
        model.setLastName(entity.getsurname());
        model.setEmail(entity.getemail());
        model.setDepartment(entity.getdepartment());
        if (entity.getgroups() != null && entity.getgroups().trim().length() > 0) {
            model.setGroups(new Groups());
            for (String name : entity.getgroups().split(",")) {
                Group group = new Group();
                group.setName(name);
                model.getGroups().getGroups().add(group);
            }
        }
        if(!StringHelper.isNullOrEmpty(entity.getdomain())){
            Domain dom = new Domain();
            dom.setName(entity.getdomain());
            dom.setId(new NGuid(entity.getdomain().getBytes(), true).toString());
            model.setDomain(dom);
        }
        return model;
    }

    @Mapping(from = AdUser.class, to = VdcUser.class)
    public static VdcUser map(AdUser adUser, VdcUser template) {
        VdcUser vdcUser = template != null ? template : new VdcUser();
        vdcUser.setUserId(adUser.getUserId());
        vdcUser.setUserName(adUser.getUserName());
        vdcUser.setSurName(adUser.getSurName());
        vdcUser.setDomainControler(adUser.getDomainControler());
        return vdcUser;
    }

    @Mapping(from = AdUser.class, to = User.class)
    public static User map(AdUser entity, User template) {
        User model = template != null ? template : new User();
        model.setName(entity.getName());
        model.setUserName(entity.getUserName());
        model.setId(entity.getUserId().toString());
        model.setLastName(entity.getSurName());
        model.setEmail(entity.getEmail());
        model.setDepartment(entity.getDepartment());
        if (entity.getGroups() != null) {
            model.setGroups(new Groups());
            for (ad_groups adgroup : entity.getGroups().values()) {
                Group group = new Group();
                group.setName(adgroup.getname());
                model.getGroups().getGroups().add(group);
            }
        }
        if(!StringHelper.isNullOrEmpty(entity.getDomainControler())){
            Domain dom = new Domain();
            dom.setName(entity.getDomainControler());
            dom.setId(new NGuid(entity.getDomainControler().getBytes(), true).toString());
            model.setDomain(dom);
        }
        return model;
    }
}
