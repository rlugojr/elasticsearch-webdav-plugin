package org.elasticsearch.webdav;

import com.github.sardine.Sardine;
import org.elasticsearch.common.blobstore.BlobPath;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

public class WebdavImmutableBlobContainer extends AbstractWebdavBlobContainer {

    protected WebdavImmutableBlobContainer(WebdavBlobStore blobStore, BlobPath blobPath, URL path, Sardine sardine) {
        super(blobStore, blobPath, path, sardine);
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
