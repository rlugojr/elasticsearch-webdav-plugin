package org.elasticsearch.webdav.url;

import org.elasticsearch.common.blobstore.BlobMetaData;
import org.elasticsearch.common.collect.ImmutableMap;
import org.junit.Test;

import java.io.InputStream;

public class PropFindResponseParserTest {

    @Test
    public void test() throws Exception {
        InputStream stream = PropFindResponseParserTest.class.getResourceAsStream("/propfind_example.xml");
        ImmutableMap<String, BlobMetaData> response = PropFindResponseParser.parse(stream);

        assert !response.isEmpty();
        assert response.size() == 5;

        assert response.containsKey("webdav");
        assert response.get("webdav").length() == 204;

        assert response.containsKey("index");
        assert response.get("index").length() == 49;

        assert response.containsKey("indices");
        assert response.get("indices").length() == 102;

        assert response.containsKey("metadata-test_snapshot1");
        assert response.get("metadata-test_snapshot1").length() == 76;

        assert response.containsKey("snapshot-test_snapshot1");
        assert response.get("snapshot-test_snapshot1").length() == 205;
    }

    @Test
    public void test2() throws Exception {
        InputStream stream = PropFindResponseParserTest.class.getResourceAsStream("/propfind_example2.xml");
        ImmutableMap<String, BlobMetaData> response = PropFindResponseParser.parse(stream);

        assert !response.isEmpty();
        assert response.size() == 4;

        assert response.containsKey("index");
        assert response.get("index").length() == 49;

        assert response.containsKey("indices");
        assert response.get("indices").length() == 102;

        assert response.containsKey("metadata-test_snapshot1");
        assert response.get("metadata-test_snapshot1").length() == 76;

        assert response.containsKey("snapshot-test_snapshot1");
        assert response.get("snapshot-test_snapshot1").length() == 3651;
    }
}
