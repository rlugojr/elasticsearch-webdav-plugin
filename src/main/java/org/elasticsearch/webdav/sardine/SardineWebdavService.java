package org.elasticsearch.webdav.sardine;

import com.github.sardine.SardineFactory;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.common.collect.Tuple;
import org.elasticsearch.common.component.AbstractLifecycleComponent;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.webdav.WebdavClient;
import org.elasticsearch.webdav.WebdavService;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class SardineWebdavService extends AbstractLifecycleComponent<WebdavService> implements WebdavService {

    private final Tuple<String, String> emptyTuple = new Tuple<>("", "");

    private final Map<Tuple<String, String>, SardineWebdavClient> clients = new HashMap<>();

    @Inject
    protected SardineWebdavService(Settings settings) {
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
        for (SardineWebdavClient client : clients.values()) {
            try {
                client.getSardine().shutdown();
            } catch (IOException e) {
                throw new ElasticsearchException("error shutdown sardine client", e);
            }
        }
    }

    public synchronized WebdavClient client() {
        SardineWebdavClient webdavClient = clients.get(emptyTuple);
        if (webdavClient == null) {
            webdavClient = new SardineWebdavClient(SardineFactory.begin());
            clients.put(emptyTuple, webdavClient);
        }
        return webdavClient;
    }

    public synchronized WebdavClient client(String username, String password) {
        Tuple<String, String> key = new Tuple<>(username, password);
        SardineWebdavClient webdavClient = clients.get(key);
        if (webdavClient == null) {
            webdavClient = new SardineWebdavClient(SardineFactory.begin(username, password));
            clients.put(key, webdavClient);
        }
        return webdavClient;
    }
}
