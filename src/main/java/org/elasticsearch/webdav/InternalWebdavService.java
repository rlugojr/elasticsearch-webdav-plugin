package org.elasticsearch.webdav;

import com.github.sardine.Sardine;
import com.github.sardine.SardineFactory;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.common.collect.Tuple;
import org.elasticsearch.common.component.AbstractLifecycleComponent;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class InternalWebdavService extends AbstractLifecycleComponent<WebdavService> implements WebdavService {

    private final Tuple<String, String> emptyTuple = new Tuple<>("", "");

    private final Map<Tuple<String, String>, Sardine> clients = new HashMap<>();

    @Inject
    protected InternalWebdavService(Settings settings) {
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
        for (Sardine client : clients.values()) {
            try {
                client.shutdown();
            } catch (IOException e) {
                throw new ElasticsearchException("error shutdown sardine client", e);
            }
        }
    }

    @Override
    public synchronized Sardine client() {
        Sardine sardine = clients.get(emptyTuple);
        if (sardine == null) {
            sardine = SardineFactory.begin();
            clients.put(emptyTuple, sardine);
        }
        return sardine;
    }

    @Override
    public synchronized Sardine client(String username, String password) {
        Tuple<String, String> key = new Tuple<>(username, password);
        Sardine sardine = clients.get(key);
        if (sardine == null) {
            sardine = SardineFactory.begin(username, password);
            clients.put(key, sardine);
        }
        return sardine;
    }
}
