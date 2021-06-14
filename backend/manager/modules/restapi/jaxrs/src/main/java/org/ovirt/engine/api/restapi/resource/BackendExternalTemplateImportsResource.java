package org.ovirt.engine.api.restapi.resource;

import static org.ovirt.engine.api.restapi.util.LinkHelper.addLinks;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.BaseResource;
import org.ovirt.engine.api.model.ExternalTemplateImport;
import org.ovirt.engine.api.model.Host;
import org.ovirt.engine.api.model.StorageDomain;
import org.ovirt.engine.api.model.Template;
import org.ovirt.engine.api.resource.ExternalTemplateImportsResource;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.ImportVmTemplateFromExternalUrlParameters;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatic;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.queries.IdAndNameQueryParameters;
import org.ovirt.engine.core.common.queries.NameQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendExternalTemplateImportsResource extends BackendResource implements ExternalTemplateImportsResource {

    @Override
    public Response add(ExternalTemplateImport externalTemplateImport) {

        validateParameters(externalTemplateImport,
                "url",
                "host.id|name",
                "cluster.id|name",
                "storageDomain.id|name");

        ImportVmTemplateFromExternalUrlParameters parameters = buildImportParameters(externalTemplateImport);
        VmTemplate createdTemplate = performAction(ActionType.ImportVmTemplateFromExternalUrl, parameters, VmTemplate.class);

        // Postprocess output entity to resolve links.
        externalTemplateImport.setTemplate(withIdLink(new Template(), createdTemplate.getId()));
        externalTemplateImport.setCluster(withIdLink(new org.ovirt.engine.api.model.Cluster(), parameters.getClusterId()));
        externalTemplateImport.setStorageDomain(withIdLink(new StorageDomain(), parameters.getStorageDomainId()));
        if (externalTemplateImport.isSetHost()) {
            externalTemplateImport.setHost(withIdLink(new Host(), parameters.getProxyHostId()));
        }

        return Response.ok(externalTemplateImport).status(Response.Status.CREATED).build();
    }

    private static <R extends BaseResource> R withIdLink(R resource, Guid id) {
        resource.setId(id.toString());
        addLinks(resource, null, false);
        return resource;
    }

    private ImportVmTemplateFromExternalUrlParameters buildImportParameters(ExternalTemplateImport templateImport) {
        ImportVmTemplateFromExternalUrlParameters parameters = new ImportVmTemplateFromExternalUrlParameters();
        parameters.setUrl(templateImport.getUrl());
        parameters.setNewTemplateName(templateImport.getTemplate() != null ? templateImport.getTemplate().getName() : null);
        Guid clusterId = getClusterId(templateImport);
        parameters.setProxyHostId(getProxyHostId(templateImport, clusterId));
        parameters.setStorageDomainId(getStorageDomainId(templateImport));
        parameters.setClusterId(clusterId);
        parameters.setQuotaId(getQuotaId(templateImport));
        parameters.setCpuProfileId(getCpuProfileId(templateImport));
        if (templateImport.isSetClone()) {
            parameters.setImportAsNewEntity(templateImport.isClone());
        }
        return parameters;
    }

    private Guid getProxyHostId(ExternalTemplateImport templateImport, Guid clusterId) {
        if (templateImport.isSetHost()) {
            if (templateImport.getHost().isSetId()) {
                return asGuid(templateImport.getHost().getId());
            } else if (templateImport.getHost().isSetName()) {
                String hostName = templateImport.getHost().getName();
                VDS vds = getEntity(VDS.class,
                        QueryType.GetVdsByName,
                        new IdAndNameQueryParameters(clusterId, templateImport.getHost().getName()),
                        hostName,
                        true);
                return vds.getId();
            }
        }
        return null;
    }

    private Guid getClusterId(ExternalTemplateImport templateImport) {
        if (templateImport.getCluster().isSetId()) {
            return asGuid(templateImport.getCluster().getId());
        } else /* if (templateImport.getCluster().isSetName()) */ {
            String clusterName = templateImport.getCluster().getName();
            Cluster cluster = getEntity(Cluster.class,
                    QueryType.GetClusterByName,
                    new NameQueryParameters(clusterName),
                    clusterName,
                    true);
            return cluster.getId();
        }
    }

    private Guid getStorageDomainId(ExternalTemplateImport templateImport) {
        if (templateImport.getStorageDomain().isSetId()) {
            return asGuid(templateImport.getStorageDomain().getId());
        } else /* if (templateImport.getStorageDomain().isSetName()) */ {
            String storageDomainName = templateImport.getStorageDomain().getName();
            StorageDomainStatic storageDomain = getEntity(StorageDomainStatic.class,
                    QueryType.GetStorageDomainByName,
                    new NameQueryParameters(storageDomainName),
                    storageDomainName,
                    true);
            return storageDomain.getId();
        }
    }

    private Guid getQuotaId(ExternalTemplateImport templateImport) {
        if (templateImport.isSetQuota() && templateImport.getQuota().isSetId()) {
            return asGuid(templateImport.getQuota().getId());
        } else {
            return null;
        }
    }

    private Guid getCpuProfileId(ExternalTemplateImport templateImport) {
        if (templateImport.isSetCpuProfile() && templateImport.getCpuProfile().isSetId()) {
            return asGuid(templateImport.getCpuProfile().getId());
        } else {
            return null;
        }
    }
}
