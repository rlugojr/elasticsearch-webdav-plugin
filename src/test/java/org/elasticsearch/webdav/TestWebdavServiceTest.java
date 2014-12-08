package org.elasticsearch.webdav;

import com.github.sardine.DavAce;
import com.github.sardine.Sardine;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import javax.xml.namespace.QName;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.net.URL;
import java.util.Collections;

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
    public void testReturnSardine() throws Exception {
        WebdavClient client = service.client();
        assert client != null;
    }

    @Test
    public void testReturnSardineAuth() throws Exception {
        WebdavClient client = service.client("foo", "foo");
        assert client != null;
    }

    @Test(expected = NotImplementedException.class)
    public void testSardineSetCredentials() throws Exception {
        service.sardine().setCredentials("foo", "foo");
    }

    @Test(expected = NotImplementedException.class)
    public void testSardineSetCredentials2() throws Exception {
        service.sardine().setCredentials("foo", "foo", "foo", "foo");
    }

    @Test(expected = NotImplementedException.class)
    public void testSardineGetResources() throws Exception {
        service.sardine().getResources("foo");
    }

    @Test
    public void testListEmpty() throws Exception {
        if (!service.sardine().list("http://localhost:8080/root").isEmpty())
            throw new AssertionError();
    }

    @Test
    public void testList() throws Exception {
        Sardine sardine = service.sardine();

        assert sardine.list("http://localhost:8080").isEmpty();
        assert sardine.list("http://localhost:8080/").isEmpty();
        assert sardine.list("http://localhost:8080/root").isEmpty();
        assert sardine.list("http://localhost:8080/root/").isEmpty();
        assert sardine.list("http://localhost:8080/root/foo").isEmpty();
        assert sardine.list("http://localhost:8080/root/foo/").isEmpty();

        sardine.put("http://localhost:8080/root/foo/bar", "foo".getBytes());
        sardine.put("http://localhost:8080/root/bar", "foo".getBytes());
        sardine.put("http://localhost:8080/bar", "foo".getBytes());

        assert !sardine.list("http://localhost:8080").isEmpty();
        assert !sardine.list("http://localhost:8080/").isEmpty();
        assert !sardine.list("http://localhost:8080/root").isEmpty();
        assert !sardine.list("http://localhost:8080/root/").isEmpty();
        assert !sardine.list("http://localhost:8080/root/foo").isEmpty();
        assert !sardine.list("http://localhost:8080/root/foo/").isEmpty();

        assert sardine.list("http://localhost:8080").size() == 2;
        assert sardine.list("http://localhost:8080/").size() == 2;
        assert sardine.list("http://localhost:8080/root").size() == 2;
        assert sardine.list("http://localhost:8080/root/").size() == 2;
        assert sardine.list("http://localhost:8080/root/foo").size() == 1;
        assert sardine.list("http://localhost:8080/root/foo/").size() == 1;

        System.out.println(sardine.list("http://localhost:8080"));
        System.out.println(sardine.list("http://localhost:8080/"));
        Assert.assertEquals(sardine.list("http://localhost:8080"), sardine.list("http://localhost:8080/"));
        assert sardine.list("http://localhost:8080/root").equals(sardine.list("http://localhost:8080/root/"));
        assert sardine.list("http://localhost:8080/root/foo").equals(sardine.list("http://localhost:8080/root/foo/"));
    }

    @Test
    public void testListNotEmptyStream() throws Exception {
        Sardine sardine = service.sardine();
        sardine.put("http://localhost:8080/root/foo", new ByteArrayInputStream("foo".getBytes()));
        assert !sardine.list("http://localhost:8080/root").isEmpty();
        assert sardine.list("http://localhost:8080/root").size() == 1;
    }

    @Test(expected = NotImplementedException.class)
    public void testListError() throws Exception {
        service.sardine().list("http://localhost:8080/root", 1);
    }

    @Test(expected = NotImplementedException.class)
    public void testListError2() throws Exception {
        service.sardine().list("http://localhost:8080/root", 1, Collections.<QName>emptySet());
    }

    @Test(expected = NotImplementedException.class)
    public void testListError3() throws Exception {
        service.sardine().list("http://localhost:8080/root", 1, true);
    }

    @Test(expected = NotImplementedException.class)
    public void testSearchError() throws Exception {
        service.sardine().search("http://localhost:8080/root", "foo", "foo");
    }

    @Test(expected = NotImplementedException.class)
    public void testSetCustomPropsError() throws Exception {
        service.sardine().setCustomProps("http://localhost:8080/root", Collections.<String, String>emptyMap(), Collections.<String>emptyList());
    }

    @Test(expected = NotImplementedException.class)
    public void testPatchError() throws Exception {
        service.sardine().patch("http://localhost:8080/root", Collections.<QName, String>emptyMap());
    }

    @Test(expected = NotImplementedException.class)
    public void testPatchError2() throws Exception {
        service.sardine().patch("http://localhost:8080/root", Collections.<QName, String>emptyMap(), Collections.<QName>emptyList());
    }

    @Test
    public void testGetAndPut() throws Exception {
        Sardine sardine = service.sardine();
        sardine.put("http://localhost:8080/root/foo", "foo".getBytes());
        assert null != sardine.get("http://localhost:8080/root/foo");
    }

    @Test
    public void testGetAndPut2() throws Exception {
        Sardine sardine = service.sardine();
        sardine.put("http://localhost:8080/root/foo", new ByteArrayInputStream("foo".getBytes()));
        assert null != sardine.get("http://localhost:8080/root/foo");
    }

    @Test(expected = NotImplementedException.class)
    public void testPutError() throws Exception {
        service.sardine().put("http://localhost:8080/root/foo", new ByteArrayInputStream("foo".getBytes()), "foo");
    }

    @Test(expected = NotImplementedException.class)
    public void testPutError2() throws Exception {
        service.sardine().put("http://localhost:8080/root/foo", new ByteArrayInputStream("foo".getBytes()), "foo", true);
    }

    @Test(expected = NotImplementedException.class)
    public void testPutError3() throws Exception {
        service.sardine().put("http://localhost:8080/root/foo", new ByteArrayInputStream("foo".getBytes()), "foo", true, 3);
    }

    @Test(expected = NotImplementedException.class)
    public void testPutError4() throws Exception {
        service.sardine().put("http://localhost:8080/root/foo", new ByteArrayInputStream("foo".getBytes()), Collections.<String, String>emptyMap());
    }

    @Test(expected = NotImplementedException.class)
    public void testPutError5() throws Exception {
        service.sardine().put("http://localhost:8080/root/foo", File.createTempFile("foo", "foo"), "foo");
    }

    @Test
    public void testDelete() throws Exception {
        Sardine sardine = service.sardine();
        sardine.put("http://localhost:8080/root/foo", "foo".getBytes());
        assert !sardine.list("http://localhost:8080/root").isEmpty();
        sardine.delete("http://localhost:8080/root/foo");
        assert sardine.list("http://localhost:8080/root").isEmpty();
    }

    @Test(expected = NotImplementedException.class)
    public void testCreateDirectory() throws Exception {
        service.sardine().createDirectory("http://localhost:8080/root");
    }

    @Test(expected = NotImplementedException.class)
    public void testMove() throws Exception {
        service.sardine().move("http://localhost:8080/root", "http://localhost:8080/root/321");
    }

    @Test(expected = NotImplementedException.class)
    public void testCopy() throws Exception {
        service.sardine().copy("http://localhost:8080/root", "http://localhost:8080/root/321");
    }

    @Test
    public void testExists() throws Exception {
        Sardine sardine = service.sardine();
        assert !sardine.exists("http://localhost:8080/root");
        sardine.put("http://localhost:8080/root/foo", "foo".getBytes());
        assert sardine.exists("http://localhost:8080/root");
    }

    @Test(expected = NotImplementedException.class)
    public void testLock() throws Exception {
        service.sardine().lock("http://localhost:8080/root");
    }

    @Test(expected = NotImplementedException.class)
    public void testRefreshLock() throws Exception {
        service.sardine().refreshLock("http://localhost:8080/root", "123", "123");
    }

    @Test(expected = NotImplementedException.class)
    public void testUnlock() throws Exception {
        service.sardine().unlock("http://localhost:8080/root", "123");
    }

    @Test(expected = NotImplementedException.class)
    public void testAcl() throws Exception {
        service.sardine().getAcl("http://localhost:8080/root");
    }

    @Test(expected = NotImplementedException.class)
    public void testQuota() throws Exception {
        service.sardine().getQuota("http://localhost:8080/root");
    }

    @Test(expected = NotImplementedException.class)
    public void testSetAcl() throws Exception {
        service.sardine().setAcl("http://localhost:8080/root", Collections.<DavAce>emptyList());
    }

    @Test(expected = NotImplementedException.class)
    public void testGetPrincipals() throws Exception {
        service.sardine().getPrincipals("http://localhost:8080/root");
    }

    @Test(expected = NotImplementedException.class)
    public void testGetPrincipalCollectionSet() throws Exception {
        service.sardine().getPrincipalCollectionSet("http://localhost:8080/root");
    }

    @Test(expected = NotImplementedException.class)
    public void testEnablePreemptiveAuthentication() throws Exception {
        service.sardine().enablePreemptiveAuthentication("http://localhost:8080/root");
    }

    @Test(expected = NotImplementedException.class)
    public void testEnablePreemptiveAuthentication2() throws Exception {
        service.sardine().enablePreemptiveAuthentication(new URL("http://localhost:8080/root"));
    }

    @Test(expected = NotImplementedException.class)
    public void testEnablePreemptiveAuthentication3() throws Exception {
        service.sardine().enablePreemptiveAuthentication("localhost", 800, 900);
    }

    @Test(expected = NotImplementedException.class)
    public void testDisablePreemptiveAuthentication() throws Exception {
        service.sardine().disablePreemptiveAuthentication();
    }

    @Test(expected = NotImplementedException.class)
    public void testShutdown() throws Exception {
        service.sardine().shutdown();
    }

    @Test(expected = NotImplementedException.class)
    public void testEnableCompression() throws Exception {
        service.sardine().enableCompression();
    }

    @Test(expected = NotImplementedException.class)
    public void testDisableCompression() throws Exception {
        service.sardine().disableCompression();
    }
}
