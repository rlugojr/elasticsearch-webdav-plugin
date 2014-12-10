package org.elasticsearch.webdav;

import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.common.blobstore.BlobMetaData;
import org.elasticsearch.common.blobstore.support.PlainBlobMetaData;
import org.elasticsearch.common.collect.ImmutableMap;
import org.elasticsearch.common.component.AbstractLifecycleComponent;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;

import java.io.*;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class TestWebdavService extends AbstractLifecycleComponent<WebdavService> implements WebdavService {

    private static volatile WebdavClientMock webdavClientMock;

    @Inject
    protected TestWebdavService(Settings settings) {
        super(settings);
    }

    @Override
    protected synchronized void doStart() throws ElasticsearchException {
        if (webdavClientMock == null) {
            webdavClientMock = new WebdavClientMock();
        }
    }

    @Override
    protected synchronized void doStop() throws ElasticsearchException {
    }

    @Override
    protected void doClose() throws ElasticsearchException {
        webdavClientMock = null;
    }

    @Override
    public synchronized WebdavClient client() {
        if (webdavClientMock == null) {
            webdavClientMock = new WebdavClientMock();
        }
        return webdavClientMock;
    }

    @Override
    public WebdavClient client(String username, String password) {
        return client();
    }


    private static class WebdavClientMock implements WebdavClient {

        private final DavEntry rootEntry = new DavEntry();

        @Override
        public boolean exists(URL url) throws IOException {
            return getEntry(url) != null;
        }

        @Override
        public boolean delete(URL url) throws IOException {
            DavEntry entry = rootEntry.children.get(url.getHost() + ":" + url.getPort());
            DavEntry entryPrev = null;
            if (entry != null) {
                String[] paths = url.getPath().split("/");
                for (String path : paths) {
                    if (path.equals("")) {
                        continue;
                    }
                    entryPrev = entry;
                    entry = entry.children.get(path);
                    if (entry == null) {
                        return false;
                    }
                }
                if (entryPrev != null) {
                    entryPrev.children.remove(paths[paths.length - 1]);
                    return true;
                }
            }
            return false;
        }

        @Override
        public ImmutableMap<String, BlobMetaData> listBlobs(URL url) throws IOException {
            ImmutableMap.Builder<String, BlobMetaData> builder = ImmutableMap.builder();
            DavEntry davEntry = getEntry(url);
            if (davEntry == null) {
                return builder.build();
            }
            for (Map.Entry<String, DavEntry> resourceEntry : davEntry.children.entrySet()) {
                BlobMetaData blobMetaData = new MockBlobMetaData(
                    resourceEntry.getKey(),
                    resourceEntry.getValue().outputStream != null
                        ? resourceEntry.getValue().outputStream.size()
                        : 0);
                builder.put(resourceEntry.getKey(), blobMetaData);
            }
            return builder.build();
        }

        @Override
        public InputStream openInput(URL url) throws IOException {
            DavEntry davEntry = getEntry(url);
            if (davEntry == null) {
                throw new IOException("not found");
            }
            if (davEntry.outputStream == null) {
                throw new IOException("not a file");
            }
            byte[] buf = davEntry.outputStream.toByteArray();
            return new ByteArrayInputStream(buf);
        }

        @Override
        public OutputStream createOutput(URL url) throws IOException {
            String server = url.getHost() + ":" + url.getPort();
            DavEntry entry = rootEntry.children.get(server);
            if (entry == null) {
                entry = new DavEntry();
                rootEntry.children.put(server, entry);
            }
            String[] paths = url.getPath().split("/");
            for (String path : paths) {
                if (path.equals("")) {
                    continue;
                }
                DavEntry entryPrev = entry;
                entry = entry.children.get(path);
                if (entry == null) {
                    entry = new DavEntry();
                    entryPrev.children.put(path, entry);
                }
            }
            entry.outputStream = new ByteArrayOutputStream();
            return entry.outputStream;
        }

        private DavEntry getEntry(URL url) throws IOException {
            DavEntry entry = rootEntry.children.get(url.getHost() + ":" + url.getPort());
            if (entry != null) {
                String[] paths = url.getPath().split("/");
                for (String path : paths) {
                    if (path.equals("")) {
                        continue;
                    }
                    entry = entry.children.get(path);
                    if (entry == null) {
                        break;
                    }
                }
            }
            return entry;
        }
    }

    private static class DavEntry {
        private final Map<String, DavEntry> children = new HashMap<>();
        private ByteArrayOutputStream outputStream = null;
    }

    private static class MockBlobMetaData extends PlainBlobMetaData {

        public MockBlobMetaData(String name, long length) {
            super(name, length);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            MockBlobMetaData that = (MockBlobMetaData) o;

            if (length() != that.length()) return false;
            if (name() != null ? !name().equals(that.name()) : that.name() != null) return false;

            return true;
        }
    }
}
