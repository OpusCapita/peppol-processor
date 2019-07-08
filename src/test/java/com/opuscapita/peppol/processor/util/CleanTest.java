package com.opuscapita.peppol.processor.util;

import com.opuscapita.peppol.commons.storage.StorageException;
import com.opuscapita.peppol.commons.storage.blob.BlobStorage;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

//@Ignore
@SpringBootTest
@RunWith(SpringJUnit4ClassRunner.class)
@EnableAutoConfiguration
public class CleanTest {

    @Autowired
    private BlobStorage storage;

    @Before
    public void testClean() throws StorageException {
        storage.remove("/private/peppol/");
    }

    @Test
    public void testPut() throws StorageException {
        String content = "akjmgalp";
        String folder = "/private/peppol/hot/dev/";
        String destFolder = "/private/peppol/cold/dev/";
        String filename = "test.xml";

        InputStream inputStream = new ByteArrayInputStream(content.getBytes());
        String putResponse = storage.put(inputStream, folder, filename);
        assertEquals(folder + filename, putResponse);

        String moveResponse = storage.move(putResponse, destFolder);
        assertEquals(destFolder + filename, moveResponse);

        List<String> listResponse1 = storage.list(folder);
        assertTrue(listResponse1.isEmpty());

        List<String> listResponse2 = storage.list(destFolder);
        assertTrue(listResponse2.contains(moveResponse));

        storage.remove(moveResponse);

        List<String> listResponse3 = storage.list(destFolder);
        assertTrue(listResponse3.isEmpty());
    }
}
