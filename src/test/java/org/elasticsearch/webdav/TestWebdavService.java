package org.elasticsearch.webdav;

import com.github.sardine.*;
import com.github.sardine.model.Response;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.common.component.AbstractLifecycleComponent;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import javax.xml.namespace.QName;
import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;

public class TestWebdavService extends AbstractLifecycleComponent<WebdavService> implements WebdavService {

    private static volatile Sardine sardine;

    @Inject
    protected TestWebdavService(Settings settings) {
        super(settings);
    }

    @Override
    protected synchronized void doStart() throws ElasticsearchException {
        if (sardine == null) {
            sardine = new SardineImpl();
        }
    }

    @Override
    protected void doStop() throws ElasticsearchException {

    }

    @Override
    protected synchronized void doClose() throws ElasticsearchException {
        sardine = null;
    }

    @Override
    public synchronized Sardine client() {
        if (sardine == null) {
            sardine = new SardineImpl();
        }
        return sardine;
    }

    @Override
    public Sardine client(String username, String password) {
        return client();
    }

    private static class SardineImpl implements Sardine {

        private final DavEntry rootEntry = new DavEntry();

        public void setCredentials(String username, String password) {
            throw new NotImplementedException();
        }

        public void setCredentials(String username, String password, String domain, String workstation) {
            throw new NotImplementedException();
        }

        /**
         * @deprecated
         */
        @Deprecated
        public List<DavResource> getResources(String url) throws IOException {
            throw new NotImplementedException();
        }

        public List<DavResource> list(String url) throws IOException {
            DavEntry davEntry = getEntry(url);
            if (davEntry == null) {
                return Collections.emptyList();
            }
            if (!url.endsWith("/")) {
                url = url + "/";
            }
            List<DavResource> resources = new ArrayList<>();
            for (Map.Entry<String, DavEntry> resourceEntry : davEntry.children.entrySet()) {
                Response response = new Response();
                response.getHref().add(url + resourceEntry.getKey());
                try {
                    DavResource resource = new InternalDavResource(response);
                    resources.add(resource);
                } catch (URISyntaxException e) {
                    throw new IOException(e);
                }
            }
            return resources;
        }

        public List<DavResource> list(String url, int depth) throws IOException {
            throw new NotImplementedException();
        }

        public List<DavResource> list(String url, int depth, Set<QName> props) throws IOException {
            throw new NotImplementedException();
        }

        public List<DavResource> list(String url, int depth, boolean allProp) throws IOException {
            throw new NotImplementedException();
        }

        public List<DavResource> search(String url, String language, String query) throws IOException {
            throw new NotImplementedException();
        }

        public void setCustomProps(String url, Map<String, String> addProps, List<String> removeProps) throws IOException {
            throw new NotImplementedException();
        }

        public List<DavResource> patch(String url, Map<QName, String> addProps) throws IOException {
            throw new NotImplementedException();
        }

        public List<DavResource> patch(String url, Map<QName, String> addProps, List<QName> removeProps) throws IOException {
            throw new NotImplementedException();
        }

        public InputStream get(String url) throws IOException {
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

        public InputStream get(String url, Map<String, String> headers) throws IOException {
            throw new NotImplementedException();
        }

        public void put(String uri, byte[] data) throws IOException {
            URL url = new URL(uri);
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
            entry.outputStream.write(data);
        }

        public void put(String uri, InputStream dataStream) throws IOException {
            URL url = new URL(uri);
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
            byte[] buffer = new byte[4096];
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            int read;
            do {
                read = dataStream.read(buffer);
                if (read > 0) {
                    outputStream.write(buffer, 0, read);
                } else {
                    break;
                }
            } while (true);
            entry.outputStream = outputStream;
        }

        public void put(String url, byte[] data, String contentType) throws IOException {
            throw new NotImplementedException();
        }

        public void put(String url, InputStream dataStream, String contentType) throws IOException {
            throw new NotImplementedException();
        }

        public void put(String url, InputStream dataStream, String contentType, boolean expectContinue) throws IOException {
            throw new NotImplementedException();
        }

        public void put(String url, InputStream dataStream, String contentType, boolean expectContinue, long contentLength) throws IOException {
            throw new NotImplementedException();
        }

        public void put(String url, InputStream dataStream, Map<String, String> headers) throws IOException {
            throw new NotImplementedException();
        }

        public void put(String url, File localFile, String contentType) throws IOException {
            throw new NotImplementedException();
        }

        public void delete(String uri) throws IOException {
            URL url = new URL(uri);
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
                        return;
                    }
                }
                if (entryPrev != null) {
                    entryPrev.children.remove(paths[paths.length - 1]);
                }
            }
        }

        public void createDirectory(String url) throws IOException {
            throw new NotImplementedException();
        }

        public void move(String sourceUrl, String destinationUrl) throws IOException {
            throw new NotImplementedException();
        }

        public void copy(String sourceUrl, String destinationUrl) throws IOException {
            throw new NotImplementedException();
        }

        public boolean exists(String url) throws IOException {
            return getEntry(url) != null;
        }

        private DavEntry getEntry(String uri) throws IOException {
            URL url = new URL(uri);
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

        public String lock(String url) throws IOException {
            throw new NotImplementedException();
        }

        public String refreshLock(String url, String token, String file) throws IOException {
            throw new NotImplementedException();
        }

        public void unlock(String url, String token) throws IOException {
            throw new NotImplementedException();
        }

        public DavAcl getAcl(String url) throws IOException {
            throw new NotImplementedException();
        }

        public DavQuota getQuota(String url) throws IOException {
            throw new NotImplementedException();
        }

        public void setAcl(String url, List<DavAce> aces) throws IOException {
            throw new NotImplementedException();
        }

        public List<DavPrincipal> getPrincipals(String url) throws IOException {
            throw new NotImplementedException();
        }

        public List<String> getPrincipalCollectionSet(String url) throws IOException {
            throw new NotImplementedException();
        }

        public void enableCompression() {
            throw new NotImplementedException();
        }

        public void disableCompression() {
            throw new NotImplementedException();
        }

        public void enablePreemptiveAuthentication(String hostname) {
            throw new NotImplementedException();
        }

        public void enablePreemptiveAuthentication(URL url) {
            throw new NotImplementedException();
        }

        public void enablePreemptiveAuthentication(String hostname, int httpPort, int httpsPort) {
            throw new NotImplementedException();
        }

        public void disablePreemptiveAuthentication() {
            throw new NotImplementedException();
        }

        public void shutdown() throws IOException {
            throw new NotImplementedException();
        }

        private static class DavEntry {
            private final Map<String, DavEntry> children = new HashMap<>();
            private ByteArrayOutputStream outputStream = null;
        }
    }

    static class InternalDavResource extends DavResource {

        private Response response;

        public InternalDavResource(Response response) throws URISyntaxException {
            super(response);
            this.response = response;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            InternalDavResource that = (InternalDavResource) o;

            if (response != null) {
                if (response.getHref() != null ? !response.getHref().equals(that.response.getHref()) : that.response.getHref() != null)
                    return false;
                if (response.getError() != null ? !response.getError().equals(that.response.getError()) : that.response.getError() != null)
                    return false;
                if (response.getPropstat() != null ? !response.getPropstat().equals(that.response.getPropstat()) : that.response.getPropstat() != null)
                    return false;
                if (response.getResponsedescription() != null ? !response.getResponsedescription().equals(that.response.getResponsedescription()) : that.response.getResponsedescription() != null)
                    return false;
                if (response.getStatus() != null ? !response.getStatus().equals(that.response.getStatus()) : that.response.getStatus() != null)
                    return false;
            } else {
                return that.response == null;
            }

            return true;
        }

        @Override
        public int hashCode() {
            return response != null ? response.hashCode() : 0;
        }
    }
}
