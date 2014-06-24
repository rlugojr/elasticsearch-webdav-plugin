package org.elasticsearch.plugin.webdav;

import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.plugins.AbstractPlugin;
import org.elasticsearch.repositories.RepositoriesModule;
import org.elasticsearch.repositories.webdav.WebdavRepository;
import org.elasticsearch.repositories.webdav.WebdavRepositoryModule;

public class WebdavPlugin extends AbstractPlugin {

    private final Settings settings;

    public WebdavPlugin(Settings settings) {
        this.settings = settings;
    }

    @Override
    public String name() {
        return "webdav";
    }

    @Override
    public String description() {
        return "Webdav Repository Plugin";
    }

    public void onModule(RepositoriesModule repositoriesModule) {
        if (settings.getAsBoolean("cloud.enabled", true)) {
            repositoriesModule.registerRepository(WebdavRepository.TYPE, WebdavRepositoryModule.class);
        }
    }
}
