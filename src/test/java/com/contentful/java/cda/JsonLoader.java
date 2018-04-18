package com.contentful.java.cda;

import com.contentful.java.cda.lib.TestResponse;
import com.sun.xml.internal.xsom.impl.scd.Iterators;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;

import static com.contentful.java.cda.Util.checkNotNull;

public class JsonLoader {

    public static final String DEFAULT_TOKEN = "test_token";
    public static final String DEFAULT_SPACE = "test_space";

    MockWebServer server;
    CDAClient client;

    public JsonLoader() {
        server = new MockWebServer();
        client = CDAClient
                .builder()
                .setSpace(DEFAULT_SPACE)
                .setToken(DEFAULT_TOKEN)
                .setEndpoint(serverUrl())
                .build();
        client.cacheLocales(false);
    }

    public String serverUrl() {
        return "http://" + server.getHostName() + ":" + server.getPort();
    }

    public CDAArray readFile(String filename, Class dataClass) {
//        readFileInt("arrays/locales.json", CDALocale.class);
        return readFileInt(filename, dataClass);
    }

    public CDAArray readFileInt(String filename, Class dataClass) {
        try{
            String [] headers = {};
            TestResponse response = new TestResponse(200,filename,headers);
            URL resource = getClass().getClassLoader().getResource(response.getFileName());
            checkNotNull(resource, "File not found: " + response.getFileName());
            server.enqueue(new MockResponse().setResponseCode(response.getCode())
                    .setBody(FileUtils.readFileToString(new File(resource.getFile()), Charset.defaultCharset()))
                    .setHeaders(response.headers()));
            return client.fetch(dataClass).all();
        }catch (Exception e){
            return null;
        }
    }




}
