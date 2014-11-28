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

    @Inject
    protected TestWebdavService(Settings settings) {
        super(settings);
    }

    @Override
    protected void doStart() throws ElasticsearchException {

    }

    @Override
    protected void doStop() throws ElasticsearchException {

    }

    @Override
    protected void doClose() throws ElasticsearchException {

    }

    @Override
    public Sardine client() {
        return new SardineImpl();
    }

    @Override
    public Sardine client(String username, String password) {
        return new SardineImpl();
    }

    private static class SardineImpl implements Sardine {

        private final DavEntry rootEntry = new DavEntry();

        public void setCredentials(String var1, String var2) {
            throw new NotImplementedException();
        }

        public void setCredentials(String var1, String var2, String var3, String var4) {
            throw new NotImplementedException();
        }

        /**
         * @deprecated
         */
        @Deprecated
        public List<DavResource> getResources(String var1) throws IOException {
            throw new NotImplementedException();
        }

        public List<DavResource> list(String var1) throws IOException {
            DavEntry davEntry = getEntry(var1);
            if (davEntry == null) {
                return Collections.emptyList();
            }
            List<DavResource> resources = new ArrayList<>();
            for (Map.Entry<String, DavEntry> resourceEntry : davEntry.children.entrySet()) {
                Response response = new Response();
                response.getHref().add(var1 + '/' + resourceEntry.getKey());
                try {
                    DavResource resource = new DavResource(response);
                    resources.add(resource);
                } catch (URISyntaxException e) {
                    throw new IOException(e);
                }
            }
            return resources;
        }

        public List<DavResource> list(String var1, int var2) throws IOException {
            throw new NotImplementedException();
        }

        public List<DavResource> list(String var1, int var2, Set<QName> var3) throws IOException {
            throw new NotImplementedException();
        }

        public List<DavResource> list(String var1, int var2, boolean var3) throws IOException {
            throw new NotImplementedException();
        }

        @Override
        public List<DavResource> search(String s, String s1, String s2) throws IOException {
            return null;
        }

        /**
         * @deprecated
         */
        @Deprecated
        public void setCustomProps(String var1, Map<String, String> var2, List<String> var3) throws IOException {
            throw new NotImplementedException();
        }

        public List<DavResource> patch(String var1, Map<QName, String> var2) throws IOException {
            throw new NotImplementedException();
        }

        public List<DavResource> patch(String var1, Map<QName, String> var2, List<QName> var3) throws IOException {
            throw new NotImplementedException();
        }

        public InputStream get(String var1) throws IOException {
            DavEntry davEntry = getEntry(var1);
            if (davEntry == null) {
                throw new IOException("not found");
            }
            if (davEntry.outputStream == null) {
                throw new IOException("not a file");
            }
            byte[] buf = davEntry.outputStream.toByteArray();
            return new ByteArrayInputStream(buf);
        }

        public InputStream get(String var1, Map<String, String> var2) throws IOException {
            throw new NotImplementedException();
        }

        public void put(String var1, byte[] var2) throws IOException {
            throw new NotImplementedException();
        }

        public void put(String var1, InputStream var2) throws IOException {
            URL url = new URL(var1);
            String server = url.getHost() + ":" + url.getPort();
            DavEntry entry = rootEntry.children.get(server);
            if (entry == null) {
                entry = new DavEntry();
                rootEntry.children.put(server, entry);
            }
            String[] paths = url.getPath().split("/");
            for (String path : paths) {
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
                read = var2.read(buffer);
                if (read > 0) {
                    outputStream.write(buffer, 0, read);
                } else {
                    break;
                }
            } while (true);
            entry.outputStream = outputStream;
        }

        public void put(String var1, byte[] var2, String var3) throws IOException {
            throw new NotImplementedException();
        }

        public void put(String var1, InputStream var2, String var3) throws IOException {
            throw new NotImplementedException();
        }

        public void put(String var1, InputStream var2, String var3, boolean var4) throws IOException {
            throw new NotImplementedException();
        }

        public void put(String var1, InputStream var2, String var3, boolean var4, long var5) throws IOException {
            throw new NotImplementedException();
        }

        public void put(String var1, InputStream var2, Map<String, String> var3) throws IOException {
            throw new NotImplementedException();
        }

        @Override
        public void put(String s, File file, String s1) throws IOException {

        }

        public void delete(String var1) throws IOException {
            URL url = new URL(var1);
            DavEntry entry = rootEntry.children.get(url.getHost() + ":" + url.getPort());
            DavEntry entryPrev = null;
            if (entry != null) {
                String[] paths = url.getPath().split("/");
                for (String path : paths) {
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

        public void createDirectory(String var1) throws IOException {
            throw new NotImplementedException();
        }

        public void move(String var1, String var2) throws IOException {
            throw new NotImplementedException();
        }

        public void copy(String var1, String var2) throws IOException {
            throw new NotImplementedException();
        }

        public boolean exists(String var1) throws IOException {
            return getEntry(var1) != null;
        }

        private DavEntry getEntry(String uri) throws IOException {
            URL url = new URL(uri);
            DavEntry entry = rootEntry.children.get(url.getHost() + ":" + url.getPort());
            if (entry != null) {
                String[] paths = url.getPath().split("/");
                for (String path : paths) {
                    entry = entry.children.get(path);
                    if (entry == null) {
                        break;
                    }
                }
            }
            return entry;
        }

        public String lock(String var1) throws IOException {
            throw new NotImplementedException();
        }

        public String refreshLock(String var1, String var2, String var3) throws IOException {
            throw new NotImplementedException();
        }

        public void unlock(String var1, String var2) throws IOException {
            throw new NotImplementedException();
        }

        public DavAcl getAcl(String var1) throws IOException {
            throw new NotImplementedException();
        }

        public DavQuota getQuota(String var1) throws IOException {
            throw new NotImplementedException();
        }

        public void setAcl(String var1, List<DavAce> var2) throws IOException {
            throw new NotImplementedException();
        }

        public List<DavPrincipal> getPrincipals(String var1) throws IOException {
            throw new NotImplementedException();
        }

        public List<String> getPrincipalCollectionSet(String var1) throws IOException {
            throw new NotImplementedException();
        }

        public void enableCompression() {
            throw new NotImplementedException();
        }

        public void disableCompression() {
            throw new NotImplementedException();
        }

        public void enablePreemptiveAuthentication(String var1) {
            throw new NotImplementedException();
        }

        public void enablePreemptiveAuthentication(URL var1) {
            throw new NotImplementedException();
        }

        public void enablePreemptiveAuthentication(String var1, int var2, int var3) {
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
}
