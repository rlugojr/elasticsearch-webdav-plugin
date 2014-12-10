package org.elasticsearch.webdav;

import org.elasticsearch.common.blobstore.BlobPath;
import org.elasticsearch.common.blobstore.ImmutableBlobContainer;
import org.elasticsearch.common.blobstore.support.BlobStores;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

public class WebdavImmutableBlobContainer extends AbstractWebdavBlobContainer implements ImmutableBlobContainer {

    protected WebdavImmutableBlobContainer(WebdavBlobStore blobStore, BlobPath blobPath, URL path, WebdavClient webdavClient) {
        super(blobStore, blobPath, path, webdavClient);
    }

    @Override
    public void writeBlob(final String blobName, final InputStream stream, final long sizeInBytes, final ImmutableBlobContainer.WriterListener listener) {
        blobStore.executor().execute(new Runnable() {
            @Override
            public void run() {
                boolean success = false;
                try {
                    try (final InputStream inputStream = stream;
                         OutputStream outputStream = webdavClient.createOutput(new URL(path, blobName))
                    ) {
                        int bytesRead;
                        byte[] buffer = new byte[blobStore.bufferSizeInBytes()];
                        while ((bytesRead = inputStream.read(buffer)) != -1) {
                            outputStream.write(buffer, 0, bytesRead);
                        }
                        outputStream.flush();
                    }
                    success = true;
                } catch (Throwable e) {
                    listener.onFailure(e);
                    try {
                        webdavClient.delete(new URL(path, blobName));
                    } catch (IOException ex) {
                        // ignore
                    }
                } finally {
                    if (success) {
                        listener.onCompleted();
                    }
                }
            }
        });
    }

    @Override
    public void writeBlob(String blobName, InputStream is, long sizeInBytes) throws IOException {
        BlobStores.syncWriteBlob(this, blobName, is, sizeInBytes);
    }
}
