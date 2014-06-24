package org.elasticsearch.webdav;

import com.github.sardine.Sardine;
import org.elasticsearch.ElasticsearchException;
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
    private final int numberOfRetries;

    private final URL path;

    private final Sardine sardine;

    public WebdavBlobStore(Settings settings, ThreadPool threadPool, Sardine sardine, URL path) {
        super(settings);
        this.path = path;
        this.threadPool = threadPool;
        this.sardine = sardine;
        this.bufferSizeInBytes = (int) settings.getAsBytesSize("buffer_size", new ByteSizeValue(100, ByteSizeUnit.KB)).bytes();
        this.numberOfRetries = settings.getAsInt("max_retries", 3);
    }

    public URL path() {
        return path;
    }

    public Executor executor() {
        return threadPool.executor(ThreadPool.Names.SNAPSHOT_DATA);
    }

    public int bufferSizeInBytes(){
        return bufferSizeInBytes;
    }

    public int numberOfRetries(){
        return numberOfRetries;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ImmutableBlobContainer immutableBlobContainer(BlobPath path) {
        try {
            return new WebdavImmutableBlobContainer(this, path, buildPath(path), sardine);
        } catch (MalformedURLException ex) {
            throw new BlobStoreException("malformed URL " + path, ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void delete(BlobPath path) {
        throw new UnsupportedOperationException("URL repository is read only");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() {
        try {
            sardine.shutdown();
        } catch (IOException e) {
            throw new ElasticsearchException("error close sardine", e);
        }
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
