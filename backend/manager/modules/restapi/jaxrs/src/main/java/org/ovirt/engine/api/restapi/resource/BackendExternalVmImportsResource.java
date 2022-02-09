package org.ovirt.engine.api.restapi.resource;

import static org.ovirt.engine.api.restapi.util.LinkHelper.addLinks;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.BaseResource;
import org.ovirt.engine.api.model.ExternalVmImport;
import org.ovirt.engine.api.model.Host;
import org.ovirt.engine.api.model.StorageDomain;
import org.ovirt.engine.api.model.Vm;
import org.ovirt.engine.api.resource.ExternalVmImportsResource;
import org.ovirt.engine.api.restapi.types.VmMapper;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.ImportVmFromExternalUrlParameters;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.OriginType;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatic;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.storage.VolumeType;
import org.ovirt.engine.core.common.queries.IdAndNameQueryParameters;
import org.ovirt.engine.core.common.queries.NameQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendExternalVmImportsResource extends BackendResource implements ExternalVmImportsResource {

    @Override
    public Response add(ExternalVmImport externalVmImport) {

        validateParameters(externalVmImport,
                "provider",
                "url",
                "name",
                "cluster.id|name",
                "storageDomain.id|name");

        ImportVmFromExternalUrlParameters parameters = buildImportParameters(externalVmImport);
        VM createdVm = performAction(ActionType.ImportVmFromExternalUrl, parameters, VM.class);

        // Postprocess output entity to clear sensitive data and resolve links.
        externalVmImport.setPassword(null);
        externalVmImport.setVm(withIdLink(new Vm(), createdVm.getId()));
        externalVmImport.setCluster(withIdLink(new org.ovirt.engine.api.model.Cluster(), parameters.getClusterId()));
        externalVmImport.setStorageDomain(withIdLink(new StorageDomain(), parameters.getStorageDomainId()));
        if (externalVmImport.isSetHost()) {
            externalVmImport.setHost(withIdLink(new Host(), parameters.getProxyHostId()));
        }

        return Response.ok(externalVmImport).status(Response.Status.CREATED).build();
    }

    private static <R extends BaseResource> R withIdLink(R resource, Guid id) {
        resource.setId(id.toString());
        addLinks(resource, null, false);
        return resource;
    }

    private ImportVmFromExternalUrlParameters buildImportParameters(ExternalVmImport vmImport) {
        ImportVmFromExternalUrlParameters parameters = new ImportVmFromExternalUrlParameters();
        parameters.setUrl(vmImport.getUrl());
        parameters.setOriginType(getOriginType(vmImport));
        parameters.setExternalName(vmImport.getName());
        parameters.setNewVmName(vmImport.getVm() != null ? vmImport.getVm().getName() : null);
        parameters.setVolumeType(getVolumeType(vmImport));
        Guid clusterId = getClusterId(vmImport);
        parameters.setProxyHostId(getProxyHostId(vmImport, clusterId));
        parameters.setVirtioIsoName(getVirtioIsoName(vmImport));
        parameters.setStorageDomainId(getStorageDomainId(vmImport));
        parameters.setClusterId(clusterId);
        parameters.setQuotaId(getQuotaId(vmImport));
        parameters.setCpuProfileId(getCpuProfileId(vmImport));
        parameters.setUsername(vmImport.getUsername());
        parameters.setPassword(vmImport.getPassword());
        return parameters;
    }

    private OriginType getOriginType(ExternalVmImport vmImport) {
        return VmMapper.mapExternalVmProviderToOrigin(vmImport.getProvider());
    }

    private Guid getProxyHostId(ExternalVmImport vmImport, Guid clusterId) {
        if (vmImport.isSetHost()) {
            if (vmImport.getHost().isSetId()) {
                return asGuid(vmImport.getHost().getId());
            } else if (vmImport.getHost().isSetName()) {
                String hostName = vmImport.getHost().getName();
                VDS vds = getEntity(VDS.class,
                        QueryType.GetVdsByName,
                        new IdAndNameQueryParameters(clusterId, vmImport.getHost().getName()),
                        hostName,
                        true);
                return vds.getId();
            }
        }
        return null;
    }

    private static String getVirtioIsoName(ExternalVmImport vmImport) {
        return vmImport.isSetDriversIso() && vmImport.getDriversIso().isSetId()
                ? vmImport.getDriversIso().getId() : null;
    }

    private static VolumeType getVolumeType(ExternalVmImport vmImport) {
        if (vmImport.isSparse() == null) {
            return null;
        }
        return vmImport.isSparse() ? VolumeType.Sparse : VolumeType.Preallocated;
    }

    private Guid getClusterId(ExternalVmImport vmImport) {
        if (vmImport.getCluster().isSetId()) {
            return asGuid(vmImport.getCluster().getId());
        } else /* if (vmImport.getCluster().isSetName()) */ {
            String clusterName = vmImport.getCluster().getName();
            Cluster cluster = getEntity(Cluster.class,
                    QueryType.GetClusterByName,
                    new NameQueryParameters(clusterName),
                    clusterName,
                    true);
            return cluster.getId();
        }
    }

    private Guid getStorageDomainId(ExternalVmImport vmImport) {
        if (vmImport.getStorageDomain().isSetId()) {
            return asGuid(vmImport.getStorageDomain().getId());
        } else /* if (vmImport.getStorageDomain().isSetName()) */ {
            String storageDomainName = vmImport.getStorageDomain().getName();
            StorageDomainStatic storageDomain = getEntity(StorageDomainStatic.class,
                    QueryType.GetStorageDomainByName,
                    new NameQueryParameters(storageDomainName),
                    storageDomainName,
                    true);
            return storageDomain.getId();
        }
    }

    private Guid getQuotaId(ExternalVmImport vmImport) {
        if (vmImport.isSetQuota() && vmImport.getQuota().isSetId()) {
            return asGuid(vmImport.getQuota().getId());
        } else {
            return null;
        }
    }

    private Guid getCpuProfileId(ExternalVmImport vmImport) {
        if (vmImport.isSetCpuProfile() && vmImport.getCpuProfile().isSetId()) {
            return asGuid(vmImport.getCpuProfile().getId());
        } else {
            return null;
        }
    }
}
