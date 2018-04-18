package com.contentful.java.cda;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class SomeTest {
    @Test
    public void testSomething(){
        JsonLoader loader = new JsonLoader();
        CDAArray data = loader.readFile("demo/entries.json", CDAEntry.class);
        assertTrue(data != null);
    }
}
