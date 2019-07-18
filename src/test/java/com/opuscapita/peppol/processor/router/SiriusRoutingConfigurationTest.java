package com.opuscapita.peppol.processor.router;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@Ignore
@SpringBootTest
@RunWith(SpringJUnit4ClassRunner.class)
@EnableAutoConfiguration
public class SiriusRoutingConfigurationTest {

    @Autowired
    private SiriusRoutingConfiguration routingConfiguration;

    @Test
    public void testRoutingConfiguration() {
        Assert.assertTrue(routingConfiguration.isSiriusReceiver("0007:5564064748"));
        Assert.assertFalse(routingConfiguration.isSiriusReceiver("9908:999999999"));
    }
}
