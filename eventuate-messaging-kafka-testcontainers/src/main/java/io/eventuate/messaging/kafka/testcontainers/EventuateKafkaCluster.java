package io.eventuate.messaging.kafka.testcontainers;

import io.eventuate.common.testcontainers.EventuateZookeeperContainer;
import io.eventuate.common.testcontainers.ReusableNetworkFactory;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.Wait;

public class EventuateKafkaCluster {

    public final Network network;

    public final EventuateZookeeperContainer zookeeper;

    public final EventuateKafkaContainer kafka;

    public EventuateKafkaCluster() {
        this("foofoo");
    }

    public EventuateKafkaCluster(String name) {
        network = ReusableNetworkFactory.createNetwork(name);
        zookeeper = new EventuateZookeeperContainer().withReuse(true)
                .withNetwork(network)
                .withNetworkAliases("zookeeper")
                .waitingFor(Wait.forHealthcheck());
        kafka = new EventuateKafkaContainer("zookeeper:2181")
                .waitingFor(Wait.forHealthcheck())
                .withNetwork(network)
                .withNetworkAliases("kafka")
                .withReuse(true);
    }
}