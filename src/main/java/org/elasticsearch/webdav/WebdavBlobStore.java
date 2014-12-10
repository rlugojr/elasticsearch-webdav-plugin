package org.elasticsearch.webdav;

import org.elasticsearch.common.blobstore.BlobPath;
import org.elasticsearch.common.blobstore.BlobStore;
import org.elasticsearch.common.blobstore.BlobStoreException;
import org.elasticsearch.common.blobstore.ImmutableBlobContainer;
import org.elasticsearch.common.component.AbstractComponent;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.unit.ByteSizeUnit;
import org.elasticsearch.common.unit.ByteSizeValue;
import org.elasticsearch.threadpool.ThreadPool;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.Executor;

public class WebdavBlobStore extends AbstractComponent implements BlobStore {

    private final ThreadPool threadPool;
    private final int bufferSizeInBytes;

    private final URL path;

    private final WebdavClient webdavClient;

    public WebdavBlobStore(Settings settings, ThreadPool threadPool, WebdavClient webdavClient, URL path) {
        super(settings);
        this.path = path;
        this.threadPool = threadPool;
        this.webdavClient = webdavClient;
        this.bufferSizeInBytes = (int) settings.getAsBytesSize("buffer_size", new ByteSizeValue(100, ByteSizeUnit.KB)).bytes();
    }

    public URL path() {
        return path;
    }

    public Executor executor() {
        return threadPool.executor(ThreadPool.Names.SNAPSHOT_DATA);
    }

    public int bufferSizeInBytes() {
        return bufferSizeInBytes;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ImmutableBlobContainer immutableBlobContainer(BlobPath path) {
        try {
            return new WebdavImmutableBlobContainer(this, path, buildPath(path), webdavClient);
        } catch (MalformedURLException ex) {
            throw new BlobStoreException("malformed URL " + path, ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void delete(BlobPath path) {
        try {
            webdavClient.delete(buildPath(path));
        } catch (IOException e) {
            throw new BlobStoreException("IO exception", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() {
    }

    /**
     * Builds URL using base URL and specified path
     *
     * @param path relative path
     * @return Base URL + path
     * @throws MalformedURLException
     */
    private URL buildPath(BlobPath path) throws MalformedURLException {
        String[] paths = path.toArray();
        if (paths.length == 0) {
            return path();
        }
        URL blobPath = new URL(this.path, paths[0] + "/");
        if (paths.length > 1) {
            for (int i = 1; i < paths.length; i++) {
                blobPath = new URL(blobPath, paths[i] + "/");
            }
        }
        return blobPath;
    }
}
