package org.ovirt.engine.core.bll.storage.repoimage;

import java.util.List;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.bll.storage.domain.IsoDomainListSynchronizer;
import org.ovirt.engine.core.common.businessentities.storage.ImageFileType;
import org.ovirt.engine.core.common.businessentities.storage.RepoImage;
import org.ovirt.engine.core.common.queries.GetImageByIdParameters;

@SuppressWarnings("unused")
public class GetImageByIdQuery<P extends GetImageByIdParameters> extends QueriesCommandBase<P> {

    @Inject
    private IsoDomainListSynchronizer isoDomainListSynchronizer;

    public GetImageByIdQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        List<RepoImage> imageList = isoDomainListSynchronizer.getUserRequestForStorageDomainRepoFileList(
                        getParameters().getStorageDomainId(), ImageFileType.All, true);

        getQueryReturnValue().setReturnValue(imageList.stream().filter(
                repoImage -> repoImage.getRepoImageId().equals(getParameters().getRepoImageId())).findFirst().orElse(null));
    }

}
