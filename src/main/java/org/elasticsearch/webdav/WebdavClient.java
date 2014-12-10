package org.elasticsearch.webdav;

import org.elasticsearch.common.blobstore.BlobMetaData;
import org.elasticsearch.common.collect.ImmutableMap;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

public interface WebdavClient {

    public boolean exists(URL url) throws IOException;

    public boolean delete(URL url) throws IOException;

    public ImmutableMap<String, BlobMetaData> listBlobs(URL url) throws IOException;

    public InputStream openInput(URL url) throws IOException;

    public OutputStream createOutput(URL url) throws IOException;
}
