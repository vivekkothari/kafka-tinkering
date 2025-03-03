package com.github.vivekkothari.kafka

import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.IntegerSerializer
import org.apache.kafka.common.serialization.UUIDSerializer
import java.time.Duration
import java.util.*
import kotlin.random.Random

class KafkaIntStreamProducer : AutoCloseable {
  private val producerConfig =
      Properties().apply {
        put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS)
        put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, UUIDSerializer::class.java.name)
        put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, IntegerSerializer::class.java.name)
        put(ProducerConfig.ACKS_CONFIG, "all")
        put(ProducerConfig.RETRY_BACKOFF_MS_CONFIG, "5000")
      }

  private val producer = KafkaProducer<UUID, Int>(producerConfig)

  fun sendMessage(topic: String, num: Int) {
    val record = ProducerRecord(topic, UUID.randomUUID(), num)
    producer.send(record) { metadata, exception ->
      if (exception == null) {
        log.info(
            "Sent message ${record.value()} to ${metadata.topic()} [partition ${metadata.partition()}]")
      } else {
        log.error("Error sending message: ${exception.message}", exception)
      }
    }
    producer.flush()
  }

  override fun close() {
    producer.close()
  }
}

fun main() {
  KafkaIntStreamProducer().use {
    for (i in 1..100) {
      Thread.sleep(Duration.ofMillis(Random.nextLong(100, 500)))
      it.sendMessage("square-calculator", i)
    }
  }
}
