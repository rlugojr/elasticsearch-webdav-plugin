Webdav Plugin for Elasticsearch
===============================

The Elasticsearch webdav plugin allows use webdav services for repositories.

## Plugin settings:

 * `https`: use https or not, default to false
 * `host`: webdav server host, example "localhost", "127.0.0.1"
 * `port`: webdav server port, example 8080
 * `base_path`: webdav server root path, example "/webdav/"
 * `username`: webdav server username, example "webdav"
 * `password`: webdav server password, example "password"


## Example of Nginx configuration:

It's required to install [nginx-dav-ext-module](https://github.com/arut/nginx-dav-ext-module) for additional webdav method `PROPFIND`

```
worker_processes  1;
pid        /usr/local/var/log/nginx/nginx.pid;
events {
    worker_connections  1024;
}
http {
    default_type        application/octet-stream;
    keepalive_timeout   65;
    gzip  on;
    server {
        listen 8080;
        server_name localhost;
        charset utf-8;
        location /webdav {
          root /usr/local/www/elasticsearch-webdav/data;
          dav_methods PUT DELETE MKCOL COPY MOVE;
          dav_ext_methods PROPFIND OPTIONS;
          create_full_put_path  on;
        }
    }
}

```

## Example of usage plugin

```
# index some data
curl -XPUT 'http://localhost:9200/twitter/tweet/_index' -d '{"tweet":"Hello world one"}'
curl -XPUT 'http://localhost:9200/twitter/tweet/_index' -d '{"tweet":"Hello world two"}'
curl -XPUT 'http://localhost:9200/twitter/tweet/_index' -d '{"tweet":"Hello world three"}'
curl -XPOST 'http://localhost:9200/twitter/_refresh'
# create webdav repository
curl -XPUT 'http://localhost:9200/_snapshot/test' -d '{
    "type": "webdav",
    "settings": {
        "https": false,
        "host": "localhost",
        "port": "8080",
        "base_path": "webdav",
        "username": "webdav",
        "password": "password"
    }
}'
# wait for
curl -XPUT 'localhost:9200/_snapshot/test/snapshot_1?wait_for_completion=true'
```

