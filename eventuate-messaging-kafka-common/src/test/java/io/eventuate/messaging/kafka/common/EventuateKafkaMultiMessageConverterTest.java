package io.eventuate.messaging.kafka.common;

import io.eventuate.messaging.kafka.common.sbe.MessageHeaderEncoder;
import io.eventuate.messaging.kafka.common.sbe.MultiMessageEncoder;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EventuateKafkaMultiMessageConverterTest {

  private static final List<EventuateKafkaMultiMessagesHeader> HEADERS =
          new ArrayList<>(Arrays.asList(new EventuateKafkaMultiMessagesHeader("commonheader1key", "commonheader1value"), new EventuateKafkaMultiMessagesHeader("commonheader2key", "commonheader2value")));

  private static final List<EventuateKafkaMultiMessage> TWO_BYTE_CHARACTER_MESSAGES = Arrays.asList(new EventuateKafkaMultiMessage("ключ", "значение"));

  private static final String MESSAGE_0_KEY = "key1";
  private static final String MESSAGE_0_VALUE = "value1";
  private static final String MESSAGE_1_KEY = "key2";
  private static final String MESSAGE_1_VALUE = "value2";

  private static final List<EventuateKafkaMultiMessage> SIMPLE_MESSAGES =
          Arrays.asList(new EventuateKafkaMultiMessage(MESSAGE_0_KEY, MESSAGE_0_VALUE), new EventuateKafkaMultiMessage(MESSAGE_1_KEY, MESSAGE_1_VALUE));

  private static final List<EventuateKafkaMultiMessage> MESSAGES_WITH_HEADERS =
          Arrays.asList(new EventuateKafkaMultiMessage(MESSAGE_0_KEY, MESSAGE_0_VALUE, new ArrayList<>(Arrays.asList(new EventuateKafkaMultiMessageHeader("header1key", "header1value"), new EventuateKafkaMultiMessageHeader("header2key", "header2value")))),
                  new EventuateKafkaMultiMessage(MESSAGE_1_KEY, MESSAGE_1_VALUE, new ArrayList<>(Arrays.asList(new EventuateKafkaMultiMessageHeader("header3key", "header3value"), new EventuateKafkaMultiMessageHeader("header4key", "header4value")))));

  private static final List<EventuateKafkaMultiMessage> EMPTY_MESSAGES = Arrays.asList(new EventuateKafkaMultiMessage("", ""));

  private static final List<EventuateKafkaMultiMessage> NULL_MESSAGES = Arrays.asList(new EventuateKafkaMultiMessage(null, null));

  private byte[] serializedMessages;
  private int estimatedSize;

  @Test
  public void testMessageBuilderSimpleMessages() {
    testMessageBuilder(new EventuateKafkaMultiMessages(SIMPLE_MESSAGES));
  }

  @Test
  public void testMessageBuilder2ByteCharacterMessages() {
    testMessageBuilder(new EventuateKafkaMultiMessages(TWO_BYTE_CHARACTER_MESSAGES));
    Assert.assertEquals(serializedMessages.length, estimatedSize);
  }

  @Test
  public void testMessageBuilderMessagesWithHeaders() {
    testMessageBuilder(new EventuateKafkaMultiMessages(HEADERS, MESSAGES_WITH_HEADERS));
  }

  @Test
  public void testMessageBuilderNullMessages() {
    testMessageBuilder(new EventuateKafkaMultiMessages(NULL_MESSAGES), new EventuateKafkaMultiMessages(EMPTY_MESSAGES));
  }

  @Test
  public void testMessageBuilderEmptyMessages() {
    testMessageBuilder(new EventuateKafkaMultiMessages(EMPTY_MESSAGES), new EventuateKafkaMultiMessages(EMPTY_MESSAGES));
  }

  @Test
  public void testMessageBuilderSizeCheck() {
    int sizeOfHeaderAndFirstMessage = MessageHeaderEncoder.ENCODED_LENGTH + MultiMessageEncoder.MessagesEncoder.HEADER_SIZE + MultiMessageEncoder.HeadersEncoder.HEADER_SIZE
            + SIMPLE_MESSAGES.get(0).estimateSize();

    EventuateKafkaMultiMessageConverter.MessageBuilder messageBuilder =
            new EventuateKafkaMultiMessageConverter.MessageBuilder(sizeOfHeaderAndFirstMessage);

    Assert.assertTrue(messageBuilder.addMessage(SIMPLE_MESSAGES.get(0)));
    Assert.assertFalse(messageBuilder.addMessage(SIMPLE_MESSAGES.get(1)));
  }

  @Test
  public void testMessageBuilderHeaderSizeCheck() {
    int sizeOfFirstMessage = 2 * 4
            + MESSAGE_0_KEY.length() * 2
            + MESSAGE_0_VALUE.length() * 2;

    EventuateKafkaMultiMessageConverter.MessageBuilder messageBuilder =
            new EventuateKafkaMultiMessageConverter.MessageBuilder(sizeOfFirstMessage);

    Assert.assertFalse(messageBuilder.addMessage(SIMPLE_MESSAGES.get(0)));
  }

  @Test
  public void testMessageConverterSimpleMessages() {
    testMessageConverter(new EventuateKafkaMultiMessages(SIMPLE_MESSAGES));
  }

  @Test
  public void testMessageConverterNullMessages() {
    testMessageConverter(new EventuateKafkaMultiMessages(NULL_MESSAGES), new EventuateKafkaMultiMessages(EMPTY_MESSAGES));
  }

  @Test
  public void testMessageConverterEmptyMessages() {
    testMessageConverter(new EventuateKafkaMultiMessages(EMPTY_MESSAGES), new EventuateKafkaMultiMessages(EMPTY_MESSAGES));
  }

  public void testMessageBuilder(EventuateKafkaMultiMessages messages) {
    testMessageBuilder(messages, messages);
  }

  public void testMessageBuilder(EventuateKafkaMultiMessages original, EventuateKafkaMultiMessages result) {
    EventuateKafkaMultiMessageConverter.MessageBuilder messageBuilder = new EventuateKafkaMultiMessageConverter.MessageBuilder(1000000);
    EventuateKafkaMultiMessageConverter eventuateMultiMessageConverter = new EventuateKafkaMultiMessageConverter();

    Assert.assertTrue(messageBuilder.setHeaders(original.getHeaders()));
    Assert.assertTrue(original.getMessages().stream().allMatch(messageBuilder::addMessage));

    serializedMessages = messageBuilder.toBinaryArray();
    estimatedSize = EventuateKafkaMultiMessageConverter.HEADER_SIZE + original.estimateSize();

    Assert.assertTrue(estimatedSize >= serializedMessages.length);

    EventuateKafkaMultiMessages deserializedMessages = eventuateMultiMessageConverter.convertBytesToMessages(serializedMessages);

    Assert.assertEquals(result, deserializedMessages);
  }

  public void testMessageConverter(EventuateKafkaMultiMessages messages) {
    testMessageConverter(messages, messages);
  }

  public void testMessageConverter(EventuateKafkaMultiMessages original, EventuateKafkaMultiMessages result) {
    EventuateKafkaMultiMessageConverter eventuateMultiMessageConverter = new EventuateKafkaMultiMessageConverter();

    byte[] serializedMessages = eventuateMultiMessageConverter.convertMessagesToBytes(original);

    EventuateKafkaMultiMessages deserializedMessages = eventuateMultiMessageConverter.convertBytesToMessages(serializedMessages);

    Assert.assertEquals(result, deserializedMessages);
  }
}