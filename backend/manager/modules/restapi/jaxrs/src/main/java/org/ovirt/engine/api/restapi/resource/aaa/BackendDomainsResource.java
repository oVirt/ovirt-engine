package org.ovirt.engine.api.restapi.resource.aaa;

import static org.ovirt.engine.api.utils.ReflectionHelper.assignChildModel;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.Domain;
import org.ovirt.engine.api.model.Domains;
import org.ovirt.engine.api.resource.aaa.DomainResource;
import org.ovirt.engine.api.resource.aaa.DomainsResource;
import org.ovirt.engine.api.restapi.model.Directory;
import org.ovirt.engine.api.restapi.resource.AbstractBackendCollectionResource;
import org.ovirt.engine.api.restapi.resource.SingleEntityResource;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendDomainsResource extends AbstractBackendCollectionResource<Domain, Directory>
    implements DomainsResource {

    static final String[] SUB_COLLECTIONS = { "users", "groups" };

    String id;

    public BackendDomainsResource() {
        super(Domain.class, Directory.class, SUB_COLLECTIONS);
    }

    @Override
    public Domains list() {
        return mapCollection(getCollection());
    }

    private Domains mapCollection(List<Directory> entities) {
        Domains collection = new Domains();
        for (Directory entity : entities) {
            collection.getDomains().add(injectSearchLinks(addLinks(map(entity)), SUB_COLLECTIONS));
        }
        return collection;
    }

    @Override
    @SingleEntityResource
    public DomainResource getDomainSubResource(String id) {
        return inject(new BackendDomainResource(id, this));
    }

    private List<Directory> getCollection() {
        List<Directory> dsl = new ArrayList<Directory>();
        for(String domain : getDomainList()){
            Directory ds = new Directory();
            ds.setDomain(domain);
            Guid guid = asGuid(domain.getBytes(), true);
            ds.setId(guid.toString());
            dsl.add(ds);
        }
        return dsl;
    }

    private List<String> getDomainList() {
        return getBackendCollection(
                String.class,
                VdcQueryType.GetDomainList,
                new VdcQueryParametersBase());
    }

    public Domain lookupDirectoryById(String id, boolean addlinks) {
        for (Directory directoriesService : getCollection()) {
            if (directoriesService.getId().equals(id)) {
                this.id = id;
                return addlinks?
                        addLinks(map(directoriesService))
                        :
                        map(directoriesService);
            }
        }
        return notFound();
    }

    public Domain lookupDirectoryByDomain(String domain, boolean addlinks) {
        for (Directory directoriesService : getCollection()) {
            if (directoriesService.getDomain().equals(domain)){
                this.id = directoriesService.getId().toString();
                return addlinks?
                        addLinks(map(directoriesService))
                        :
                        map(directoriesService);
            }
        }
        return notFound();
    }

    @Override
    protected Domain addParents(Domain domain) {
        if(id!=null){
            assignChildModel(domain, Domain.class).setId(id);
        }
        return domain;
    }

    @Override
    protected Response performRemove(String id) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected Domain doPopulate(Domain model, Directory entity) {
        return model;
    }
}
