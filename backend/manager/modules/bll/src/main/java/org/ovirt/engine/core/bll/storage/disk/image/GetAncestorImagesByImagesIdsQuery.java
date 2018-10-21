package org.ovirt.engine.core.bll.storage.disk.image;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.queries.IdsQueryParameters;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.DiskImageDao;

public class GetAncestorImagesByImagesIdsQuery<P extends IdsQueryParameters>
        extends QueriesCommandBase<P> {

    @Inject
    private DiskImageDao diskImageDao;

    public GetAncestorImagesByImagesIdsQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        Set<Guid> haveInvalidAncestor = new HashSet<>();
        Function<Guid, DiskImage> getAncestor = (Guid imageId) -> {
            DiskImage ancestor = diskImageDao.getAncestor(imageId, getUserID(), getParameters().isFiltered());
            if (ancestor == null) {
                haveInvalidAncestor.add(imageId);
            }
            return ancestor;
        };

        Map<Guid, DiskImage> haveValidAncestor = getParameters().getIds().stream()
                .map(guid -> new Pair<>(guid, getAncestor.apply(guid)))
                .filter(pair -> pair.getSecond() != null)
                .collect(Collectors.toMap(Pair::getFirst, Pair::getSecond));

        if (!haveInvalidAncestor.isEmpty()) {
            log.warn("Ancestor is missing for the following image id(s): {}", StringUtils.join(haveInvalidAncestor, ','));
        }
        getQueryReturnValue().setReturnValue(haveValidAncestor);
    }
}
