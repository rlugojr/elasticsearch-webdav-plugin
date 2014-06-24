package org.elasticsearch.webdav;

import com.github.sardine.Sardine;
import org.elasticsearch.common.blobstore.BlobPath;
import org.elasticsearch.common.blobstore.ImmutableBlobContainer;
import org.elasticsearch.common.blobstore.support.BlobStores;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class WebdavImmutableBlobContainer extends AbstractWebdavBlobContainer implements ImmutableBlobContainer {

    protected WebdavImmutableBlobContainer(WebdavBlobStore blobStore, BlobPath blobPath, URL path, Sardine sardine) {
        super(blobStore, blobPath, path, sardine);
    }

    /**
     * This operation is not supported by Webdav Blob Container
     */
    @Override
    public void writeBlob(final String blobName, final InputStream stream, final long sizeInBytes, final ImmutableBlobContainer.WriterListener listener) {
        blobStore.executor().execute(new Runnable() {
            @Override
            public void run() {
                boolean success = false;
                try {
                    try(final InputStream is = stream){
                        sardine.put(new URL(path, blobName).toString(), is);
                    }
                    success = true;
                } catch (Throwable e) {
                    listener.onFailure(e);
                    try{
                        sardine.delete(new URL(path, blobName).toString());
                    }catch (IOException ex){
                        // ignore
                    }
                } finally {
                    if(success) {
                        listener.onCompleted();
                    }
                }
            }
        });
    }

    /**
     * This operation is not supported by Webdav Blob Container
     */
    @Override
    public void writeBlob(String blobName, InputStream is, long sizeInBytes) throws IOException {
        BlobStores.syncWriteBlob(this, blobName, is, sizeInBytes);
    }
}
