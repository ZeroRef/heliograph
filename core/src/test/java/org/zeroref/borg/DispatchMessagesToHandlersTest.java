package org.zeroref.borg;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.zeroref.borg.directions.MessageDestinations;
import org.zeroref.borg.pipeline.HandleMessages;
import org.zeroref.borg.pipeline.MessageHandlerTable;
import org.zeroref.borg.pipeline.MessagePipeline;
import org.zeroref.borg.runtime.EndpointId;
import org.zeroref.borg.sagas.SagaPersistence;
import org.zeroref.borg.transport.KafkaMessageSender;

import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.mockito.Mockito.mock;

public class DispatchMessagesToHandlersTest {

    MessageEnvelope envelope = new MessageEnvelope(UUID.randomUUID(), "", new Ping());
    KafkaMessageSender sender = mock(KafkaMessageSender.class);
    MessageBus bus = mock(UnicastMessageBus.class);
    EndpointId endpointId = new EndpointId("");
    MessageDestinations router = mock(MessageDestinations.class);
    SagaPersistence sagaPersistence = mock(SagaPersistence.class);

    @Test
    public void when_no_handler_registered_will_noop(){
        MessageHandlerTable table = new MessageHandlerTable();
        MessagePipeline pipeline = new MessagePipeline(table, sender, endpointId, router, sagaPersistence);

        pipeline.dispatch(envelope);
    }

    @Test
    public void will_invoke_associated_hadler(){
        AtomicInteger cnt = new AtomicInteger(0);

        MessageHandlerTable table = new MessageHandlerTable();
        table.registerHandler(Ping.class, messageBus -> message -> cnt.incrementAndGet());
        MessagePipeline pipeline = new MessagePipeline(table, sender, endpointId, router, sagaPersistence);

        pipeline.dispatch(envelope);

        Assert.assertEquals(cnt.get(), 1);
    }

    @Test
    public void no_handler_will_give_null(){
        MessageHandlerTable table = new MessageHandlerTable();
        HandleMessages<Object> hndl = table.getHandlers(bus, new Ping());

        Assert.assertNull(hndl);
    }

    @Test
    public void will_get_registered_handler(){
        MessageHandlerTable table = new MessageHandlerTable();
        table.registerHandler(Ping.class, messageBus -> new PingHandler());

        HandleMessages<Object> hndl = table.getHandlers(bus, new Ping());

        Assert.assertNotNull(hndl);
    }

    @Test
    public void factory_check(){
        MessageHandlerTable table = new MessageHandlerTable();
        table.registerHandler(Ping.class, messageBus ->  new PingHandler());

        Ping ping = new Ping();

        Assert.assertNotEquals(table.getHandlers(bus, ping), table.getHandlers(bus, ping));
    }

    public class Ping{
    }

    public class PingHandler implements HandleMessages<Ping>{
        @Override
        public void handle(Ping message) {

        }
    }
}
