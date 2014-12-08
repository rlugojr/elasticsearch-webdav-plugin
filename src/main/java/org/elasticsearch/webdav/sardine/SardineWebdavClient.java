package org.elasticsearch.webdav.sardine;

import com.github.sardine.DavResource;
import com.github.sardine.Sardine;
import org.elasticsearch.common.blobstore.BlobMetaData;
import org.elasticsearch.common.blobstore.support.PlainBlobMetaData;
import org.elasticsearch.common.collect.ImmutableMap;
import org.elasticsearch.common.collect.MapBuilder;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.webdav.WebdavClient;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.List;

public class SardineWebdavClient implements WebdavClient {

    private final Sardine sardine;

    @Inject
    public SardineWebdavClient(Sardine sardine) {
        this.sardine = sardine;
    }

    public Sardine getSardine() {
        return sardine;
    }

    @Override
    public boolean exists(URL url) throws IOException {
        return sardine.exists(url.toString());
    }

    @Override
    public boolean delete(URL url) throws IOException {
        sardine.delete(url.toString());
        return true;
    }

    @Override
    public ImmutableMap<String, BlobMetaData> listBlobs(URL url) throws IOException {
        List<DavResource> resourceList = sardine.list(url.toString());

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
    public InputStream openInput(URL url) throws IOException {
        return sardine.get(url.toString());
    }

    @Override
    public OutputStream createOutput(final URL url) throws IOException {
        return new OutputStream() {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            @Override
            public void write(int b) throws IOException {
                outputStream.write(b);
            }

            public void close() throws IOException {
                sardine.put(url.toString(), outputStream.toByteArray());
            }
        };
    }
}
