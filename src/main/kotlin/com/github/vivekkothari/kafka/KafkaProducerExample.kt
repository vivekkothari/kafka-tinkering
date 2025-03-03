package com.github.vivekkothari.kafka

import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringSerializer
import java.time.Duration
import java.util.*

class KafkaProducerExample {
  private val props =
      Properties().apply {
        put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS)
        put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer::class.java.name)
        put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer::class.java.name)
        put(ProducerConfig.ACKS_CONFIG, "all")
        put(ProducerConfig.RETRY_BACKOFF_MS_CONFIG, "5000")
      }

  private val producer = KafkaProducer<String, String>(props)

  fun sendMessage(topic: String, message: String) {
    val record = ProducerRecord<String, String>(topic, message)
    producer.send(record) { metadata, exception ->
      if (exception == null) {
        println("Sent message to ${metadata.topic()} [partition ${metadata.partition()}]")
      } else {
        println("Error sending message: ${exception.message}")
      }
    }
  }

  fun close() {
    producer.close()
  }
}

fun main() {
  val producer = KafkaProducerExample()
  for (i in 1..100) {
    Thread.sleep(Duration.ofSeconds(1))
    producer.sendMessage(TOPIC, "$i")
  }
  producer.close()
}
