package org.elasticsearch.webdav.url;

import org.elasticsearch.common.Base64;
import org.elasticsearch.common.blobstore.BlobMetaData;
import org.elasticsearch.common.collect.ImmutableMap;
import org.elasticsearch.webdav.WebdavClient;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.URL;

public class UrlWebdavClient implements WebdavClient {

    private final static int SC_OK = 200;
    private final static int SC_MULTIPLE_CHOICES = 300;
    private final static int SC_NOT_FOUND = 404;
    private final static ImmutableMap<String, BlobMetaData> emptyMap =
        ImmutableMap.<String, BlobMetaData>builder().build();

    private final String basicAuth;

    public UrlWebdavClient() {
        basicAuth = null;
    }

    public UrlWebdavClient(String username, String password) {
        if (username != null && password != null) {
            String credentials = username + ":" + password;
            this.basicAuth = "Basic " + Base64.encodeBytes(credentials.getBytes());
        } else {
            basicAuth = null;
        }
    }

    @Override
    public boolean exists(URL url) throws IOException {
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        try {
            if (basicAuth != null) {
                con.setRequestProperty("Authorization", basicAuth);
            }
            con.setRequestMethod("HEAD");
            int statusCode = con.getResponseCode();
            String responseMessage = con.getResponseMessage();
            if (statusCode >= SC_OK && statusCode < SC_MULTIPLE_CHOICES) {
                return true;
            }
            if (statusCode == SC_NOT_FOUND) {
                return false;
            }
            throw new IOException("status code: " + statusCode + " message: " + responseMessage);
        } finally {
            con.disconnect();
        }
    }

    @Override
    public boolean delete(URL url) throws IOException {
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        try {
            if (basicAuth != null) {
                con.setRequestProperty("Authorization", basicAuth);
            }
            con.setRequestMethod("DELETE");
            int statusCode = con.getResponseCode();
            String responseMessage = con.getResponseMessage();
            if (statusCode >= SC_OK && statusCode < SC_MULTIPLE_CHOICES) {
                return true;
            }
            if (statusCode == SC_NOT_FOUND) {
                return false;
            }
            throw new IOException("status code: " + statusCode + " message: " + responseMessage);
        } finally {
            con.disconnect();
        }
    }

    @Override
    public ImmutableMap<String, BlobMetaData> listBlobs(URL url) throws IOException {
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        try {
            if (basicAuth != null) {
                con.setRequestProperty("Authorization", basicAuth);
            }
            setPropFindMethod(con);

            int statusCode = con.getResponseCode();
            String responseMessage = con.getResponseMessage();
            if (statusCode >= SC_OK && statusCode < SC_MULTIPLE_CHOICES) {
                return PropFindResponseParser.parse(con.getInputStream());
            }
            if (statusCode == SC_NOT_FOUND) {
                return emptyMap;
            }
            throw new IOException("status code: " + statusCode + " message: " + responseMessage);
        } finally {
            con.disconnect();
        }
    }

    private void setPropFindMethod(HttpURLConnection httpURLConnection) {
        try {
            final Class<?> httpURLConnectionClass = httpURLConnection.getClass();
            final Class<?> parentClass = httpURLConnectionClass.getSuperclass();
            final Field methodField;
            // If the implementation class is an HTTPS URL Connection, we
            // need to go up one level higher in the hierarchy to modify the
            // 'method' field.
            if (parentClass == HttpsURLConnection.class) {
                methodField = parentClass.getSuperclass().getDeclaredField("method");
            } else {
                methodField = parentClass.getDeclaredField("method");
            }
            methodField.setAccessible(true);
            methodField.set(httpURLConnection, "PROPFIND");
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public InputStream openInput(URL url) throws IOException {
        final HttpURLConnection con = (HttpURLConnection) url.openConnection();
        try {
            if (basicAuth != null) {
                con.setRequestProperty("Authorization", basicAuth);
            }
            con.setRequestMethod("GET");
            int statusCode = con.getResponseCode();
            String responseMessage = con.getResponseMessage();
            if (statusCode >= SC_OK && statusCode < SC_MULTIPLE_CHOICES) {
                return new BufferedInputStream(con.getInputStream(), 65536);
            }
            throw new IOException("status code: " + statusCode + " message: " + responseMessage);
        } catch (IOException e) {
            con.disconnect();
            throw e;
        }
    }

    @Override
    public OutputStream createOutput(URL url) throws IOException {
        final HttpURLConnection con = (HttpURLConnection) url.openConnection();
        try {
            if (basicAuth != null) {
                con.setRequestProperty("Authorization", basicAuth);
            }
            con.setDoOutput(true);
            con.setRequestMethod("PUT");
            final OutputStream outputStream = con.getOutputStream();
            // override all methods for better performance
            return new OutputStream() {

                @Override
                public void write(byte[] b) throws IOException {
                    outputStream.write(b);
                }

                @Override
                public void write(int b) throws IOException {
                    outputStream.write(b);
                }

                @Override
                public void write(byte[] b, int off, int len) throws IOException {
                    outputStream.write(b, off, len);
                }

                @Override
                public void flush() throws IOException {
                    outputStream.flush();
                }

                @Override
                public void close() throws IOException {
                    outputStream.close();
                    int statusCode = con.getResponseCode();
                    String responseMessage = con.getResponseMessage();
                    con.disconnect();
                    if (statusCode < SC_OK || statusCode >= SC_MULTIPLE_CHOICES) {
                        throw new IOException("status code: " + statusCode + " message: " + responseMessage);
                    }
                }
            };
        } catch (IOException e) {
            con.disconnect();
            throw e;
        }
    }
}
