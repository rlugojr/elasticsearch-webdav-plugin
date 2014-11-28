package org.elasticsearch.repositories.webdav;

import org.elasticsearch.common.inject.AbstractModule;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.index.snapshots.IndexShardRepository;
import org.elasticsearch.index.snapshots.blobstore.BlobStoreIndexShardRepository;
import org.elasticsearch.repositories.Repository;
import org.elasticsearch.webdav.InternalWebdavService;
import org.elasticsearch.webdav.WebdavService;

public class WebdavRepositoryModule extends AbstractModule {

    public static final String WEBDAV_SERVICE_TYPE_KEY = "webdav.service.type";
    private final Settings settings;

    public WebdavRepositoryModule(Settings settings) {
        this.settings = settings;
    }

    @Override
    protected void configure() {
        bind(WebdavService.class).to(getWebdavServiceClass()).asEagerSingleton();
        bind(Repository.class).to(WebdavRepository.class).asEagerSingleton();
        bind(IndexShardRepository.class).to(BlobStoreIndexShardRepository.class).asEagerSingleton();
    }

    protected Class<? extends WebdavService> getWebdavServiceClass() {
        return settings.getAsClass(WEBDAV_SERVICE_TYPE_KEY, InternalWebdavService.class);
    }
}
