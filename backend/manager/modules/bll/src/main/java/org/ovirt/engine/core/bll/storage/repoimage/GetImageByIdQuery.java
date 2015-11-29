package org.ovirt.engine.core.bll.storage.repoimage;

import java.util.List;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.bll.storage.domain.IsoDomainListSyncronizer;
import org.ovirt.engine.core.common.businessentities.storage.ImageFileType;
import org.ovirt.engine.core.common.businessentities.storage.RepoImage;
import org.ovirt.engine.core.common.queries.GetImageByIdParameters;

@SuppressWarnings("unused")
public class GetImageByIdQuery<P extends GetImageByIdParameters> extends QueriesCommandBase<P> {

    public GetImageByIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        List<RepoImage> imageList = IsoDomainListSyncronizer.getInstance()
                .getUserRequestForStorageDomainRepoFileList(
                        getParameters().getStorageDomainId(), ImageFileType.All, true);

        getQueryReturnValue().setReturnValue(imageList.stream().filter(
                repoImage -> repoImage.getRepoImageId().equals(getParameters().getRepoImageId())).findFirst().orElse(null));
    }

}
