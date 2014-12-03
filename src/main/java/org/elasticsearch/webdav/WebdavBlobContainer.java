package org.elasticsearch.webdav;

import com.github.sardine.DavResource;
import com.github.sardine.Sardine;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.common.blobstore.BlobMetaData;
import org.elasticsearch.common.blobstore.BlobPath;
import org.elasticsearch.common.blobstore.support.AbstractBlobContainer;
import org.elasticsearch.common.blobstore.support.PlainBlobMetaData;
import org.elasticsearch.common.collect.ImmutableMap;
import org.elasticsearch.common.collect.MapBuilder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.List;

public class WebdavBlobContainer extends AbstractBlobContainer {

    protected final WebdavBlobStore blobStore;

    protected final URL path;

    protected final Sardine sardine;

    protected WebdavBlobContainer(WebdavBlobStore blobStore, BlobPath blobPath, URL path, Sardine sardine) {
        super(blobPath);
        this.blobStore = blobStore;
        this.path = path;
        this.sardine = sardine;
    }

    @Override
    public boolean blobExists(String blobName) {
        try {
            return sardine.exists(new URL(path, blobName).toString());
        } catch (IOException e) {
            throw new ElasticsearchException("error test blob ["+blobName+"]", e);
        }
    }

    @Override
    public boolean deleteBlob(String blobName) throws IOException {
        sardine.delete(new URL(path, blobName).toString());
        return true;
    }

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
    public InputStream openInput(String blobName) throws IOException {
        return sardine.get(new URL(path, blobName).toString());
    }

    @Override
    public OutputStream createOutput(final String blobName) throws IOException {
        return new OutputStream() {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            @Override
            public void write(int b) throws IOException {
                outputStream.write(b);
            }

            public void close() throws IOException {
                sardine.put(new URL(path, blobName).toString(), outputStream.toByteArray());
            }
        };
    }
}
