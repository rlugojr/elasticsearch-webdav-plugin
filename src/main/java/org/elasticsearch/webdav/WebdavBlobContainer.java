package org.elasticsearch.webdav;

import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.common.blobstore.BlobMetaData;
import org.elasticsearch.common.blobstore.BlobPath;
import org.elasticsearch.common.blobstore.support.AbstractBlobContainer;
import org.elasticsearch.common.collect.ImmutableMap;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

public class WebdavBlobContainer extends AbstractBlobContainer {

    protected final WebdavBlobStore blobStore;

    protected final URL path;

    protected final WebdavClient webdavClient;

    protected WebdavBlobContainer(WebdavBlobStore blobStore, BlobPath blobPath, URL path, WebdavClient webdavClient) {
        super(blobPath);
        this.blobStore = blobStore;
        this.path = path;
        this.webdavClient = webdavClient;
    }

    @Override
    public boolean blobExists(String blobName) {
        try {
            return webdavClient.exists(new URL(path, blobName));
        } catch (IOException e) {
            throw new ElasticsearchException("error test blob [" + blobName + "]", e);
        }
    }

    @Override
    public boolean deleteBlob(String blobName) throws IOException {
        return webdavClient.delete(new URL(path, blobName));
    }

    @Override
    public ImmutableMap<String, BlobMetaData> listBlobs() throws IOException {
        return webdavClient.listBlobs(path);
    }

    @Override
    public InputStream openInput(String blobName) throws IOException {
        return webdavClient.openInput(new URL(path, blobName));
    }

    @Override
    public OutputStream createOutput(final String blobName) throws IOException {
        return webdavClient.createOutput(new URL(path, blobName));
    }
}
