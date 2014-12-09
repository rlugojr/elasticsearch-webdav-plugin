package org.elasticsearch.webdav.url;

import org.elasticsearch.common.blobstore.BlobMetaData;
import org.elasticsearch.common.blobstore.support.PlainBlobMetaData;
import org.elasticsearch.common.collect.ImmutableMap;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

import javax.xml.XMLConstants;
import java.io.IOException;
import java.io.InputStream;

public class PropFindResponseParser {

    public static ImmutableMap<String, BlobMetaData> parse(InputStream stream) throws IOException {
        try {
            XMLReader reader = XMLReaderFactory.createXMLReader();
            try {
                reader.setFeature(
                    "http://xml.org/sax/features/external-general-entities", Boolean.FALSE);
            } catch (SAXException e) {
                ; //Not all parsers will support this attribute
            }
            try {
                reader.setFeature(
                    "http://xml.org/sax/features/external-parameter-entities", Boolean.FALSE);
            } catch (SAXException e) {
                ; //Not all parsers will support this attribute
            }
            try {
                reader.setFeature(
                    "http://apache.org/xml/features/nonvalidating/load-external-dtd", Boolean.FALSE);
            } catch (SAXException e) {
                ; //Not all parsers will support this attribute
            }
            try {
                reader.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, Boolean.TRUE);
            } catch (SAXException e) {
                ; //Not all parsers will support this attribute
            }

            final ImmutableMap.Builder<String, BlobMetaData> builder = ImmutableMap.builder();

            reader.setContentHandler(new DefaultHandler() {

                private final StringBuffer buffer = new StringBuffer(256);
                private String name;
                private long length;

                @Override
                public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
                    if (localName.equals("response")) {
                        name = null;
                        length = 0;
                    }
                    if (localName.equals("displayname")) {
                        buffer.setLength(0);
                    }
                    if (localName.equals("getcontentlength")) {
                        buffer.setLength(0);
                    }
                }

                @Override
                public void endElement(String uri, String localName, String qName) throws SAXException {
                    if (localName.equals("response")) {
                        if (name != null) {
                            builder.put(name, new PlainBlobMetaData(name, length));
                        }
                    }
                    if (localName.equals("displayname")) {
                        if (buffer.length() == 0) {
                            name = null;
                        } else {
                            name = buffer.toString();
                            System.out.println("name: " + buffer.toString());
                            buffer.setLength(0);
                        }
                    }
                    if (localName.equals("getcontentlength")) {
                        length = Long.valueOf(buffer.toString());
                        System.out.println("length: " + buffer.toString());
                        buffer.setLength(0);
                    }
                }

                @Override
                public void characters(char[] ch, int start, int length) {
                    buffer.append(ch, start, length);
                }

            });

            reader.parse(new InputSource(stream));

            return builder.build();

        } catch (SAXException e) {
            throw new IOException(e);
        }
    }
}
