package com.github.vivekkothari.kafka

import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.serialization.StringDeserializer
import java.time.Duration
import java.util.*

const val TOPIC = "test-topic"
const val BOOTSTRAP_SERVERS = "localhost:29092"

class KafkaConsumerExample {
  private val props =
      Properties().apply {
        put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS)
        put(ConsumerConfig.GROUP_ID_CONFIG, "kotlin-consumer-group")
        put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer::class.java.name)
        put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer::class.java.name)
        put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
        put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false")
      }

  private val consumer = KafkaConsumer<String, String>(props)

  fun startConsuming(topic: String) {
    consumer.subscribe(listOf(topic))

    while (true) {
      val records = consumer.poll(Duration.ofMillis(100))
      for (record in records) {
        println("Received message: ${record.value()} from partition ${record.partition()}")
      }
      consumer.commitAsync()
    }
  }
}

fun main() {
  val consumer = KafkaConsumerExample()
  consumer.startConsuming(TOPIC)
}
