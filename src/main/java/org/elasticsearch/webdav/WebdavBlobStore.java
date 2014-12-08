package org.elasticsearch.webdav;

import org.elasticsearch.common.blobstore.BlobContainer;
import org.elasticsearch.common.blobstore.BlobPath;
import org.elasticsearch.common.blobstore.BlobStore;
import org.elasticsearch.common.blobstore.BlobStoreException;
import org.elasticsearch.common.component.AbstractComponent;
import org.elasticsearch.common.settings.Settings;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

public class WebdavBlobStore extends AbstractComponent implements BlobStore {

    private final URL path;

    private final WebdavClient webdavClient;

    public WebdavBlobStore(Settings settings, WebdavClient webdavClient, URL path) {
        super(settings);
        this.path = path;
        this.webdavClient = webdavClient;
    }

    public URL path() {
        return path;
    }

    @Override
    public BlobContainer blobContainer(BlobPath blobPath) {
        try {
            URL url = buildPath(blobPath);
            return new WebdavBlobContainer(this, blobPath, url, webdavClient);
        } catch (MalformedURLException ex) {
            throw new BlobStoreException("malformed URL " + path, ex);
        }
    }

    @Override
    public void delete(BlobPath blobPath) {
        try {
            URL url = buildPath(blobPath);
            webdavClient.delete(url);
        } catch (MalformedURLException ex) {
            throw new BlobStoreException("malformed URL " + path, ex);
        } catch (IOException ex) {
            throw new BlobStoreException("error delete " + path, ex);
        }
        throw new UnsupportedOperationException("URL repository is read only");
    }

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
