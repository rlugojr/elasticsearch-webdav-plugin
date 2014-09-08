#!/bin/sh

# example use webdav
curl -XPUT 'https://localhost:8080/webdav/hello.txt' -d 'hello world'

curl -XPUT 'https://localhost:8080/webdav/hello/hello.txt' -d 'hello world'

echo
curl -XDELETE 'https://localhost:8080/webdav/test/' 2>/dev/null

echo
curl -XDELETE 'https://localhost:8080/webdav/webdav/' 2>/dev/null

echo
curl -XDELETE 'http://localhost:9200/_snapshot/test' 2>/dev/null

echo
curl -XDELETE 'http://localhost:9200/twitter/' 2>/dev/null

echo
curl -XPUT 'http://localhost:9200/twitter/' -d '
index :
    number_of_shards : 2
    number_of_replicas : 1
'

curl -XPUT 'http://localhost:9200/twitter/tweet/_index' -d '{"eqq":123123123}'
curl -XPUT 'http://localhost:9200/twitter/tweet/_index' -d '{"eqq":123123123}'
curl -XPUT 'http://localhost:9200/twitter/tweet/_index' -d '{"eqq":123123123}'
curl -XPUT 'http://localhost:9200/twitter/tweet/_index' -d '{"eqq":123123123}'
curl -XPUT 'http://localhost:9200/twitter/tweet/_index' -d '{"eqq":123123123}'
curl -XPUT 'http://localhost:9200/twitter/tweet/_index' -d '{"eqq":123123123}'
curl -XPUT 'http://localhost:9200/twitter/tweet/_index' -d '{"eqq":123123123}'
curl -XPUT 'http://localhost:9200/twitter/tweet/_index' -d '{"eqq":123123123}'
curl -XPUT 'http://localhost:9200/twitter/tweet/_index' -d '{"eqq":123123123}'
curl -XPUT 'http://localhost:9200/twitter/tweet/_index' -d '{"eqq":123123123}'
curl -XPUT 'http://localhost:9200/twitter/tweet/_index' -d '{"eqq":123123123}'
curl -XPUT 'http://localhost:9200/twitter/tweet/_index' -d '{"eqq":123123123}'

echo
curl -XPOST 'http://localhost:9200/twitter/_refresh'


echo
# create webdav repo
curl -XPUT 'http://localhost:9200/_snapshot/test' -d '{
    "type": "webdav",
    "settings": {
        "https": false,
        "host": "localhost",
        "port": "8080",
        "base_path": "webdav",
        "username": "mitallast",
        "password": "4efHn6k"
    }
}' 2>/dev/null

#echo
#curl -XGET 'http://localhost:9200/_snapshot?pretty'

echo
curl -XPUT 'localhost:9200/_snapshot/test/snapshot_1?wait_for_completion=true'