package mq_005;

import org.apache.activemq.broker.BrokerService;

/**
 * 嵌入到Java中的activeMQ
 */
public class EmbedBroker {
    public static void main(String[] args) throws Exception {

        BrokerService brokerService = new BrokerService();
        brokerService.setUseJmx(true);
        brokerService.addConnector("tcp://localhost:61616");
        brokerService.start();

    }
}
