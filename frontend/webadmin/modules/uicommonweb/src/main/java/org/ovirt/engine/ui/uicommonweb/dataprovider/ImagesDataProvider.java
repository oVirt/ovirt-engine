package org.ovirt.engine.ui.uicommonweb.dataprovider;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.ovirt.engine.core.common.businessentities.comparators.LexoNumericComparator;
import org.ovirt.engine.core.common.businessentities.storage.ImageFileType;
import org.ovirt.engine.core.common.businessentities.storage.RepoImage;
import org.ovirt.engine.core.common.queries.GetImagesListByStoragePoolIdParameters;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Converter;
import org.ovirt.engine.ui.frontend.Frontend;

public class ImagesDataProvider {

    private static final String ISO_PREFIX = "iso://"; //$NON-NLS-1$

    public static void getFloppyImageList(AsyncQuery<List<String>> aQuery, Guid storagePoolId) {
        getIrsImageList(aQuery, storagePoolId, false, ImageFileType.Floppy,
                new RepoImageToImageFileNameAsyncConverter());
    }

    public static void getUnknownImageList(AsyncQuery<List<String>> aQuery, Guid storagePoolId, boolean forceRefresh) {
        getIrsImageList(aQuery,
                storagePoolId,
                forceRefresh,
                ImageFileType.All,
                new RepoImageToImageFileNameAsyncConverter(image -> ISO_PREFIX + image.getRepoImageId(),
                        image -> ImageFileType.Unknown == image.getFileType()));
    }

    public static void getISOImagesList(AsyncQuery<List<RepoImage>> aQuery, Guid storagePoolId) {
        getISOImagesList(aQuery, storagePoolId, false);
    }

    public static void getISOImagesList(AsyncQuery<List<RepoImage>> aQuery, Guid storagePoolId, boolean forceRefresh) {
        getIrsImageList(aQuery,
                storagePoolId,
                forceRefresh,
                ImageFileType.ISO,
                new AsyncDataProvider.ListConverter());
    }

    private static <T> void getIrsImageList(AsyncQuery<List<T>> aQuery,
            Guid storagePoolId,
            boolean forceRefresh,
            ImageFileType imageFileType,
            Converter<List<T>, List<RepoImage>> converterCallBack) {
        if (converterCallBack != null) {
            aQuery.converterCallback = converterCallBack;
        }

        GetImagesListByStoragePoolIdParameters parameters =
                new GetImagesListByStoragePoolIdParameters(storagePoolId, imageFileType);
        parameters.setForceRefresh(forceRefresh);
        Frontend.getInstance().runQuery(QueryType.GetImagesListByStoragePoolId, parameters, aQuery);
    }


    private static class RepoImageToImageFileNameAsyncConverter implements Converter<List<String>, List<RepoImage>> {

        private Function<RepoImage, String> transform = image -> image.getRepoImageId();
        private Predicate<RepoImage> imagePredicate = image -> true;

        public RepoImageToImageFileNameAsyncConverter() {
        }

        RepoImageToImageFileNameAsyncConverter(Function<RepoImage, String> transform, Predicate<RepoImage> imagePredicate) {
            this.transform = transform;
            this.imagePredicate = imagePredicate;
        }

        @Override
        public List<String> convert(List<RepoImage> source) {
            if (source != null) {
                return source.stream().filter(imagePredicate).map(transform)
                        .sorted(new LexoNumericComparator()).collect(Collectors.toList());
            }
            return new ArrayList<>();
        }
    }
}
