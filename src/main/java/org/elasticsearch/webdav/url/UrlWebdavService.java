package org.elasticsearch.webdav.url;

import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.common.component.Lifecycle;
import org.elasticsearch.common.component.LifecycleListener;
import org.elasticsearch.webdav.WebdavClient;
import org.elasticsearch.webdav.WebdavService;

public class UrlWebdavService implements WebdavService {

    @Override
    public WebdavClient client() {
        return new UrlWebdavClient();
    }

    @Override
    public WebdavClient client(String username, String password) {
        return new UrlWebdavClient(username, password);
    }

    @Override
    public Lifecycle.State lifecycleState() {
        return null;
    }

    @Override
    public void addLifecycleListener(LifecycleListener listener) {

    }

    @Override
    public void removeLifecycleListener(LifecycleListener listener) {

    }

    @Override
    public WebdavService start() throws ElasticsearchException {
        return null;
    }

    @Override
    public WebdavService stop() throws ElasticsearchException {
        return null;
    }

    @Override
    public void close() throws ElasticsearchException {

    }
}
