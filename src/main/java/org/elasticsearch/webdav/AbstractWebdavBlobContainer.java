package org.elasticsearch.webdav;

import org.apache.lucene.util.IOUtils;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.common.blobstore.BlobMetaData;
import org.elasticsearch.common.blobstore.BlobPath;
import org.elasticsearch.common.blobstore.support.AbstractBlobContainer;
import org.elasticsearch.common.collect.ImmutableMap;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public abstract class AbstractWebdavBlobContainer extends AbstractBlobContainer {

    protected final WebdavBlobStore blobStore;

    protected final URL path;

    protected final WebdavClient webdavClient;

    protected AbstractWebdavBlobContainer(WebdavBlobStore blobStore, BlobPath blobPath, URL path, WebdavClient webdavClient) {
        super(blobPath);
        this.blobStore = blobStore;
        this.path = path;
        this.webdavClient = webdavClient;
    }

    /**
     * This operation is not supported by AbstractWebdavBlobContainer
     */
    @Override
    public boolean blobExists(String blobName) {
        try {
            return webdavClient.exists(new URL(path, blobName));
        } catch (IOException e) {
            throw new ElasticsearchException("error test blob [" + blobName + "]", e);
        }
    }

    /**
     * This operation is not supported by AbstractWebdavBlobContainer
     */
    @Override
    public boolean deleteBlob(String blobName) throws IOException {
        return webdavClient.delete(new URL(path, blobName));
    }

    /**
     * This operation is not supported by AbstractWebdavBlobContainer
     */
    @Override
    public ImmutableMap<String, BlobMetaData> listBlobs() throws IOException {
        return webdavClient.listBlobs(path);
    }

    @Override
    public void readBlob(final String blobName, final ReadBlobListener listener) {
        blobStore.executor().execute(new Runnable() {
            @Override
            public void run() {
                byte[] buffer = new byte[blobStore.bufferSizeInBytes()];
                InputStream is = null;
                try {
                    is = webdavClient.openInput(new URL(path, blobName));
                    int bytesRead;
                    while ((bytesRead = is.read(buffer)) != -1) {
                        listener.onPartial(buffer, 0, bytesRead);
                    }
                } catch (Throwable t) {
                    IOUtils.closeWhileHandlingException(is);
                    listener.onFailure(t);
                    return;
                }
                try {
                    IOUtils.closeWhileHandlingException(is);
                    listener.onCompleted();
                } catch (Throwable t) {
                    listener.onFailure(t);
                }
            }
        });
    }
}
