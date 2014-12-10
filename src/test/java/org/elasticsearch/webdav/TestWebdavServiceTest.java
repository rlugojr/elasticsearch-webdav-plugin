package org.elasticsearch.webdav;

import org.elasticsearch.common.blobstore.BlobMetaData;
import org.elasticsearch.common.collect.ImmutableMap;
import org.elasticsearch.common.collect.Maps;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.OutputStream;
import java.net.URL;

public class TestWebdavServiceTest {

    private TestWebdavService service;

    @Before
    public void setUp() throws Exception {
        service = new TestWebdavService(ImmutableSettings.EMPTY);
        service.doStart();
    }

    @After
    public void tearDown() throws Exception {
        service.doClose();
    }

    @Test
    public void testReturnWebdavClient() throws Exception {
        WebdavClient client = service.client();
        assert client != null;
    }

    @Test
    public void testReturnWebdavClientAuth() throws Exception {
        WebdavClient client = service.client("foo", "foo");
        assert client != null;
    }

    @Test
    public void testListEmpty() throws Exception {
        if (!service.client().listBlobs(new URL("http://localhost:8080/root")).isEmpty())
            throw new AssertionError();
    }

    @Test
    public void testList() throws Exception {
        WebdavClient client = service.client();

        assert client.listBlobs(new URL("http://localhost:8080")).isEmpty();
        assert client.listBlobs(new URL("http://localhost:8080/")).isEmpty();
        assert client.listBlobs(new URL("http://localhost:8080/root")).isEmpty();
        assert client.listBlobs(new URL("http://localhost:8080/root/")).isEmpty();
        assert client.listBlobs(new URL("http://localhost:8080/root/foo")).isEmpty();
        assert client.listBlobs(new URL("http://localhost:8080/root/foo/")).isEmpty();

        try (OutputStream output = client.createOutput(new URL("http://localhost:8080/root/foo/bar"))) {
            output.write("foo".getBytes());
        }
        try (OutputStream output = client.createOutput(new URL("http://localhost:8080/root/bar"))) {
            output.write("foo".getBytes());
        }
        try (OutputStream output = client.createOutput(new URL("http://localhost:8080/bar"))) {
            output.write("foo".getBytes());
        }

        assert !client.listBlobs(new URL("http://localhost:8080")).isEmpty();
        assert !client.listBlobs(new URL("http://localhost:8080/")).isEmpty();
        assert !client.listBlobs(new URL("http://localhost:8080/root")).isEmpty();
        assert !client.listBlobs(new URL("http://localhost:8080/root/")).isEmpty();
        assert !client.listBlobs(new URL("http://localhost:8080/root/foo")).isEmpty();
        assert !client.listBlobs(new URL("http://localhost:8080/root/foo/")).isEmpty();

        assert client.listBlobs(new URL("http://localhost:8080")).size() == 2;
        assert client.listBlobs(new URL("http://localhost:8080/")).size() == 2;
        assert client.listBlobs(new URL("http://localhost:8080/root")).size() == 2;
        assert client.listBlobs(new URL("http://localhost:8080/root/")).size() == 2;
        assert client.listBlobs(new URL("http://localhost:8080/root/foo")).size() == 1;
        assert client.listBlobs(new URL("http://localhost:8080/root/foo/")).size() == 1;

        assertMapEquals(client.listBlobs(new URL("http://localhost:8080")), client.listBlobs(new URL("http://localhost:8080/")));
        assertMapEquals(client.listBlobs(new URL("http://localhost:8080/root")), client.listBlobs(new URL("http://localhost:8080/root/")));
        assertMapEquals(client.listBlobs(new URL("http://localhost:8080/root/foo")), client.listBlobs(new URL("http://localhost:8080/root/foo/")));
    }

    private void assertMapEquals(
        ImmutableMap<String, BlobMetaData> expected,
        ImmutableMap<String, BlobMetaData> actual
    ) {
        Assert.assertTrue(Maps.difference(expected, actual).areEqual());
    }

    @Test
    public void testListNotEmptyStream() throws Exception {
        WebdavClient client = service.client();
        try (OutputStream output = client.createOutput(new URL("http://localhost:8080/root/foo"))) {
            output.write("foo".getBytes());
        }
        assert !client.listBlobs(new URL("http://localhost:8080/root")).isEmpty();
        assert client.listBlobs(new URL("http://localhost:8080/root")).size() == 1;
    }

    @Test
    public void testGetAndPut() throws Exception {
        WebdavClient client = service.client();
        try (OutputStream output = client.createOutput(new URL("http://localhost:8080/root/foo"))) {
            output.write("foo".getBytes());
        }
        assert null != client.createOutput(new URL("http://localhost:8080/root/foo"));
    }

    @Test
    public void testGetAndPut2() throws Exception {
        WebdavClient client = service.client();
        try (OutputStream output = client.createOutput(new URL("http://localhost:8080/root/foo"))) {
            output.write("foo".getBytes());
        }
        assert null != client.createOutput(new URL("http://localhost:8080/root/foo"));
    }

    @Test
    public void testDelete() throws Exception {
        WebdavClient client = service.client();
        try (OutputStream output = client.createOutput(new URL("http://localhost:8080/root/foo"))) {
            output.write("foo".getBytes());
        }
        assert !client.listBlobs(new URL("http://localhost:8080/root")).isEmpty();
        client.delete(new URL("http://localhost:8080/root/foo"));
        assert client.listBlobs(new URL("http://localhost:8080/root")).isEmpty();
    }

    @Test
    public void testExists() throws Exception {
        WebdavClient client = service.client();
        assert !client.exists(new URL("http://localhost:8080/root"));
        try (OutputStream output = client.createOutput(new URL("http://localhost:8080/root/foo"))) {
            output.write("foo".getBytes());
        }
        assert client.exists(new URL("http://localhost:8080/root"));
    }
}
