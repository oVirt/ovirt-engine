package org.ovirt.engine.api.restapi.resource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.ovirt.engine.api.restapi.resource.BackendStorageDomainTemplatesResourceTest.setUpStorageDomain;
import static org.ovirt.engine.api.restapi.resource.BackendStorageDomainTemplatesResourceTest.setUpStoragePool;
import static org.ovirt.engine.api.restapi.resource.BackendTemplatesResourceTest.setUpEntityExpectations;
import static org.ovirt.engine.api.restapi.test.util.TestHelper.eqParams;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import javax.ws.rs.core.UriInfo;

import org.junit.jupiter.api.Test;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.api.model.Disk;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.queries.GetAllFromExportDomainQueryParameters;
import org.ovirt.engine.core.common.queries.GetVmTemplateParameters;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryParametersBase;
import org.ovirt.engine.core.common.queries.QueryReturnValue;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.MockConfigDescriptor;
import org.ovirt.engine.core.utils.MockedConfig;

@MockitoSettings(strictness = Strictness.LENIENT)
public class BackendExportDomainDiskResourceTest
   extends AbstractBackendSubResourceTest<Disk, org.ovirt.engine.core.common.businessentities.storage.Disk, BackendExportDomainDiskResource> {

    private static final Guid TEMPLATE_ID = GUIDS[1];
    private static final Guid DISK_ID = GUIDS[2];
    private static final Guid DATA_CENTER_ID = GUIDS[0];
    private static final Guid STORAGE_DOMAIN_ID = GUIDS[GUIDS.length-1];

    public static Stream<MockConfigDescriptor<?>> mockConfiguration() {
        return Stream.of(MockConfigDescriptor.of(ConfigValues.PropagateDiskErrors, false));
    }

    public BackendExportDomainDiskResourceTest() {
        super(new BackendExportDomainDiskResource(DISK_ID.toString(),
                new BackendExportDomainDisksResource(
                    new BackendStorageDomainTemplateResource(
                        new BackendStorageDomainTemplatesResource(STORAGE_DOMAIN_ID),
                        TEMPLATE_ID.toString()))));
    }

    @Override
    protected void init() {
        super.init();
        initResource(resource);
        initParentResource();
    }

    private void initParentResource() {
        AbstractBackendResource parent = resource.getParent().getParent().getParent();
        parent.setMappingLocator(mapperLocator);
        parent.setMessageBundle(messageBundle);
        parent.setHttpHeaders(httpHeaders);
    }

    @Override
    protected void setUriInfo(UriInfo uriInfo) {
        super.setUriInfo(uriInfo);
        resource.setUriInfo(uriInfo);
        resource.getParent().getParent().getParent().setUriInfo(uriInfo);
    }

    @Test
    @MockedConfig("mockConfiguration")
    public void testGet() {
        setUpGetStorageDomainExpectations(StorageDomainType.ImportExport);
        setUpGetEntityExpectations(StorageDomainType.ImportExport, STORAGE_DOMAIN_ID);
        setUriInfo(setUpBasicUriExpectations());
        Disk disk = resource.get();
        assertNotNull(disk);
        assertEquals(disk.getId(), DISK_ID.toString());
    }

    protected void setUpGetStorageDomainExpectations(StorageDomainType domainType) {
        setUpEntityQueryExpectations(QueryType.GetStorageDomainById,
                                     IdQueryParameters.class,
                                     new String[] { "Id" },
                                     new Object[] { STORAGE_DOMAIN_ID },
                                     setUpStorageDomain(domainType));
    }

    protected void setUpGetEntityExpectations(StorageDomainType domainType, Guid getStoragePoolsByStorageDomainId) {
        setUpGetEntityExpectations(domainType, getStoragePoolsByStorageDomainId, false);
    }

    protected void setUpGetEntityExpectations(StorageDomainType domainType, Guid getStoragePoolsByStorageDomainId, boolean notFound) {
        switch (domainType) {
        case Data:
            setUpEntityQueryExpectations(QueryType.GetVmTemplate,
                                         GetVmTemplateParameters.class,
                                         new String[] { "Id" },
                                         new Object[] { TEMPLATE_ID },
                                         notFound ? null : getEntity(1));
            break;
        case ImportExport:
            setUpEntityQueryExpectations(QueryType.GetStoragePoolsByStorageDomainId,
                                         IdQueryParameters.class,
                                         new String[] { "Id" },
                                         new Object[] { getStoragePoolsByStorageDomainId },
                                         setUpStoragePool());
            setUpEntityQueryExpectations(QueryType.GetTemplatesFromExportDomain,
                                         GetAllFromExportDomainQueryParameters.class,
                                         new String[] { "StoragePoolId", "StorageDomainId" },
                                         new Object[] { DATA_CENTER_ID, STORAGE_DOMAIN_ID },
                                         setUpTemplates(notFound));
            break;
        default:
            break;
        }
    }

    @Override
    protected org.ovirt.engine.core.common.businessentities.storage.Disk getEntity(int index) {
        DiskImage disk = new DiskImage();
        disk.setId(DISK_ID);
        return disk;
    }

    private Map<Guid, DiskImage> getDiskMap() {
        DiskImage disk = new DiskImage();
        disk.setId(DISK_ID);
        return Collections.singletonMap(DISK_ID, disk);
    }
    @Override
    protected void setUpEntityQueryExpectations(QueryType query,
            Class<? extends QueryParametersBase> queryClass,
            String[] queryNames,
            Object[] queryValues,
            Object queryReturn,
            Object failure) {
        QueryReturnValue queryResult = new QueryReturnValue();
        queryResult.setSucceeded(failure == null);
        if (failure == null) {
            queryResult.setReturnValue(queryReturn);
            when(backend.runQuery(eq(query),
                    eqParams(queryClass,
                            addSession(queryNames),
                            addSession(queryValues)))).thenReturn(queryResult);
        } else {
            if (failure instanceof String) {
                queryResult.setExceptionString((String) failure);
                setUpL10nExpectations((String) failure);
                when(backend.runQuery(eq(query),
                        eqParams(queryClass,
                                addSession(queryNames),
                                addSession(queryValues)))).thenReturn(queryResult);
            } else if (failure instanceof Exception) {
                when(backend.runQuery(eq(query),
                        eqParams(queryClass,
                                addSession(queryNames),
                                addSession(queryValues)))).thenThrow((Exception) failure);
            }
        }

    }

    protected Map<VmTemplate, List<DiskImage>> setUpTemplates(boolean notFound) {
        if (notFound) {
            return Collections.emptyMap();
        }
        return IntStream.range(0, NAMES.length)
                .boxed().collect(Collectors.toMap(this::getVmTemplateEntity, ArrayList::new));
    }

    protected VmTemplate getVmTemplateEntity(int index) {
        VmTemplate vm = setUpEntityExpectations(mock(VmTemplate.class), index);
        when(vm.getDiskTemplateMap()).thenReturn(getDiskMap());
        return vm;
    }
}
