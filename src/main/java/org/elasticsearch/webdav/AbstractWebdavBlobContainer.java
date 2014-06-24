package org.elasticsearch.webdav;

import com.github.sardine.DavResource;
import com.github.sardine.Sardine;
import org.apache.lucene.util.IOUtils;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.common.blobstore.BlobMetaData;
import org.elasticsearch.common.blobstore.BlobPath;
import org.elasticsearch.common.blobstore.support.AbstractBlobContainer;
import org.elasticsearch.common.blobstore.support.PlainBlobMetaData;
import org.elasticsearch.common.collect.ImmutableMap;
import org.elasticsearch.common.collect.MapBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

public abstract class AbstractWebdavBlobContainer extends AbstractBlobContainer {

    protected final WebdavBlobStore blobStore;

    protected final URL path;

    protected final Sardine sardine;

    protected AbstractWebdavBlobContainer(WebdavBlobStore blobStore, BlobPath blobPath, URL path, Sardine sardine) {
        super(blobPath);
        this.blobStore = blobStore;
        this.path = path;
        this.sardine = sardine;
    }

    /**
     * This operation is not supported by AbstractWebdavBlobContainer
     */
    @Override
    public boolean blobExists(String blobName) {
        try {
            return sardine.exists(new URL(path, blobName).toString());
        } catch (IOException e) {
            throw new ElasticsearchException("error test blob ["+blobName+"]", e);
        }
    }

    /**
     * This operation is not supported by AbstractWebdavBlobContainer
     */
    @Override
    public boolean deleteBlob(String blobName) throws IOException {
        sardine.delete(new URL(path, blobName).toString());
        return true;
    }

    /**
     * This operation is not supported by AbstractWebdavBlobContainer
     */
    @Override
    public ImmutableMap<String, BlobMetaData> listBlobs() throws IOException {
        List<DavResource> resourceList = sardine.list(path.toString());

        // using MapBuilder and not ImmutableMap.Builder as it seems like File#listFiles might return duplicate files!
        MapBuilder<String, BlobMetaData> builder = MapBuilder.newMapBuilder();
        for (DavResource file : resourceList) {
            if (!file.isDirectory()) {
                builder.put(file.getName(), new PlainBlobMetaData(file.getName(), file.getContentLength()));
            }
        }
        return builder.immutableMap();
    }

    @Override
    public void readBlob(final String blobName, final ReadBlobListener listener) {
        blobStore.executor().execute(new Runnable() {
            @Override
            public void run() {
                byte[] buffer = new byte[blobStore.bufferSizeInBytes()];
                InputStream is = null;
                try {
                    is = sardine.get(new URL(path, blobName).toString());
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
