package org.elasticsearch.repositories.webdav;

import org.elasticsearch.common.inject.AbstractModule;
import org.elasticsearch.index.snapshots.IndexShardRepository;
import org.elasticsearch.index.snapshots.blobstore.BlobStoreIndexShardRepository;
import org.elasticsearch.repositories.Repository;

public class WebdavRepositoryModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(Repository.class).to(WebdavRepository.class).asEagerSingleton();
        bind(IndexShardRepository.class).to(BlobStoreIndexShardRepository.class).asEagerSingleton();
    }
}
