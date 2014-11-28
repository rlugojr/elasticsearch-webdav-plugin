package org.elasticsearch.webdav;

import com.github.sardine.Sardine;
import org.elasticsearch.common.component.LifecycleComponent;

public interface WebdavService extends LifecycleComponent<WebdavService> {

    public Sardine client();

    public Sardine client(String username, String password);
}
