package org.elasticsearch.webdav;

import org.elasticsearch.common.component.LifecycleComponent;

public interface WebdavService extends LifecycleComponent<WebdavService> {

    public WebdavClient client();

    public WebdavClient client(String username, String password);
}
