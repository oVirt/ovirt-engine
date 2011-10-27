package org.ovirt.engine.core.dao.images;

import java.util.List;

import org.hibernate.Query;
import org.hibernate.Transaction;

import org.ovirt.engine.core.common.businessentities.DiskImageTemplate;
import org.ovirt.engine.core.common.businessentities.vm_template_image_map;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.BaseDAOHibernateImpl;

public class DiskImageTemplateDAOHibernateImpl extends BaseDAOHibernateImpl<DiskImageTemplate, Guid> {
    public DiskImageTemplateDAOHibernateImpl() {
        super(DiskImageTemplate.class);
    }

    @Override
    public DiskImageTemplate get(Guid id) {
        return fillInDetails(super.get(id));
    }

    public void removeTemplate(Guid id, Guid vm) {
        Query query =
                getSession().createQuery("delete from DiskImageTemplate " +
                        "where id = :id " +
                        "and id in " +
                        "(select vtim.id.imageTemplateId from vm_template_image_map vtim " +
                        "where vtim.id.vmTemplateId = :template_id)");

        query.setParameter("id", id);
        query.setParameter("template_id", vm);

        Transaction transaction = getSession().beginTransaction();

        query.executeUpdate();
        transaction.commit();
    }

    public DiskImageTemplate getTemplateByVmTemplateAndId(Guid vm, Guid template) {
        Query query = getSession().createQuery("select ditmpl from DiskImageTemplate ditmpl, " +
                "vm_template_image_map vtimap " +
                "where ditmpl.id = :template_id " +
                "and vtimap.id.imageTemplateId = :template_id " +
                "and vtimap.id.vmTemplateId = :vm_template_id");

        query.setParameter("template_id", template);
        query.setParameter("vm_template_id", vm);

        return fillInDetails((DiskImageTemplate) query.uniqueResult());
    }

    private DiskImageTemplate fillInDetails(DiskImageTemplate template) {
        if (template != null) {
            Query query =
                    getSession().createQuery("from vm_template_image_map where id.imageTemplateId = :template_id");

            query.setParameter("template_id", template.getit_guid());

            vm_template_image_map result = (vm_template_image_map) query.uniqueResult();

            if (result != null) {
                template.setvtim_it_guid(result.id.imageTemplateId);
                template.setvmt_guid(result.id.vmTemplateId);
                template.setinternal_drive_mapping(result.internalDriveMapping);
            }
        }

        return template;
    }

    public List<DiskImageTemplate> getAllTemplatesForVmTemplate(Guid vmTemplate) {
        Query query = getSession().createQuery("select ditmpl from DiskImageTemplate ditmpl, " +
                "vm_template_image_map vtimap " +
                "where vtimap.id.imageTemplateId = ditmpl.id " +
                "and vtimap.id.vmTemplateId = :template_id");

        query.setParameter("template_id", vmTemplate);

        return fillInDetails(query.list());
    }

    private List<DiskImageTemplate> fillInDetails(List<DiskImageTemplate> templates) {
        for (DiskImageTemplate template : templates) {
            fillInDetails(template);
        }
        return templates;
    }
}
