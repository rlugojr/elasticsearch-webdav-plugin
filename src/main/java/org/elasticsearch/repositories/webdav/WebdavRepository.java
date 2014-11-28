package org.elasticsearch.repositories.webdav;

import com.github.sardine.Sardine;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.blobstore.BlobPath;
import org.elasticsearch.common.blobstore.BlobStore;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.unit.ByteSizeUnit;
import org.elasticsearch.common.unit.ByteSizeValue;
import org.elasticsearch.index.snapshots.IndexShardRepository;
import org.elasticsearch.repositories.RepositoryException;
import org.elasticsearch.repositories.RepositorySettings;
import org.elasticsearch.repositories.blobstore.BlobStoreRepository;
import org.elasticsearch.threadpool.ThreadPool;
import org.elasticsearch.webdav.WebdavBlobStore;
import org.elasticsearch.webdav.WebdavService;

import java.net.MalformedURLException;
import java.net.URL;

public class WebdavRepository extends BlobStoreRepository {

    public final static String TYPE = "webdav";

    private final BlobStore blobStore;

    private final BlobPath basePath;

    private ByteSizeValue chunkSize;

    /**
     * Constructs new webdav repository
     *
     * @param name                 repository name
     * @param repositorySettings   repository settings
     * @param indexShardRepository an instance of IndexShardRepository
     */
    @Inject
    protected WebdavRepository(String name, RepositorySettings repositorySettings, IndexShardRepository indexShardRepository, ThreadPool threadPool, WebdavService webdavService) throws MalformedURLException {
        super(name, repositorySettings, indexShardRepository);

        boolean https = repositorySettings.settings().getAsBoolean("https", componentSettings.getAsBoolean("https", false));

        String host = repositorySettings.settings().get("host", componentSettings.get("host"));
        if(host == null || host.isEmpty()){
            throw new RepositoryException(name, "No host defined for webdav repository");
        }
        Integer port = repositorySettings.settings().getAsInt("port", componentSettings.getAsInt("port", 80));
        if(port == null){
            throw new RepositoryException(name, "No port defined for webdav repository");
        }

        this.chunkSize = repositorySettings.settings().getAsBytesSize("chunk_size", componentSettings.getAsBytesSize("chunk_size", new ByteSizeValue(100, ByteSizeUnit.MB)));

        String basePath = repositorySettings.settings().get("base_path", null);
        if (Strings.hasLength(basePath)) {
            BlobPath path = new BlobPath();
            for(String elem : Strings.splitStringToArray(basePath, '/')) {
                path = path.add(elem);
            }
            this.basePath = path;
        } else {
            this.basePath = BlobPath.cleanPath();
        }

        logger.debug("host [{}], port [{}], base path [{}], chunk size [{}]", host, port, basePath, chunkSize);

        URL path = new URL(https?"https": "http", host, port, "/" + this.basePath.buildAsString("/"));

        String username = repositorySettings.settings().get("username", componentSettings.get("username"));
        String password = repositorySettings.settings().get("password", componentSettings.get("password"));

        Sardine sardine;
        if(username != null && !username.isEmpty()){
            sardine = webdavService.client(username, password);
        }else {
            sardine = webdavService.client();
        }

        blobStore = new WebdavBlobStore(settings, threadPool, sardine, path);
    }

    protected ByteSizeValue chunkSize(){
        return chunkSize;
    }

    @Override
    protected BlobStore blobStore() {
        return blobStore;
    }

    @Override
    protected BlobPath basePath() {
        return basePath;
    }
}
