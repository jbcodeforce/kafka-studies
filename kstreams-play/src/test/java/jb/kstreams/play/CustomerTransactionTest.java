package jb.kstreams.play;

import java.util.Properties;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.TestInputTopic;
import org.apache.kafka.streams.TestOutputTopic;
import org.apache.kafka.streams.TopologyTestDriver;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Printed;
import org.apache.kafka.streams.kstream.Produced;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import ibm.gse.eda.mpoc.domain.util.JSONSerde;
import jb.kstreams.play.domain.Purchase;


public class CustomerTransactionTest {

    private TopologyTestDriver testDriver;
    private static String inTopicName = "transactions";
    private static String outTopicName = "output";
    private TestInputTopic<String, Purchase> inTopic;
    private TestInputTopic<String, Purchase> testTopic;
    private TestOutputTopic<String, Purchase> outTopic;

    public Properties getStreamsConfig() {
        final Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "kstream-op1");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "dummmy:1234");
        return props;
    }


    @Before
    public void setup(){
        final StreamsBuilder builder = new StreamsBuilder();
        
        KStream<String,Purchase> purchaseStream = builder.stream(inTopicName, Consumed.with(Serdes.String(),
            new JSONSerde<Purchase>()))
            .mapValues(p -> Purchase.builder(p).maskCreditCard().build());

        purchaseStream.print(Printed.<String, Purchase>toSysOut().withLabel("encryptedPurchase"));

        purchaseStream.to(outTopicName, Produced.with(Serdes.String(), new JSONSerde<Purchase>()));

        testDriver = new TopologyTestDriver(builder.build(), getStreamsConfig());
        inTopic = testDriver.createInputTopic(inTopicName, new StringSerializer(), new JSONSerde<Purchase>());
        outTopic = testDriver.createOutputTopic(outTopicName,new StringDeserializer(), new JSONSerde<Purchase>());
    }

    @Test
    public void ccShouldGetEncrypted(){
        Purchase p = new Purchase();
        p.setCustomerId("CUST01");
        p.setCreditCardNumber("1234-3456789012");
        inTopic.pipeInput(p.getCustomerId(),p);
        Assert.assertTrue( ! outTopic.isEmpty()); 
        Assert.assertTrue("xxxx".equals(outTopic.readKeyValue().value.getCreditCardNumber()));
    }
}