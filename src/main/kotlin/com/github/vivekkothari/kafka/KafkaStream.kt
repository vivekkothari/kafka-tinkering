package com.github.vivekkothari.kafka

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import com.github.vivekkothari.kafka.KafkaStream.Companion.consumerStreamConfig
import kotlinx.serialization.Serializable
import org.apache.kafka.common.serialization.Serde
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.KeyValue
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.StreamsConfig
import org.apache.kafka.streams.kstream.Consumed
import org.apache.kafka.streams.kstream.KTable
import org.apache.kafka.streams.kstream.Materialized
import org.apache.kafka.streams.state.Stores
import org.slf4j.LoggerFactory
import java.util.*
import java.util.concurrent.CountDownLatch

class KafkaStream {

  @Serializable
  data class PlayEvent(val id: String, val artist: String, val song: String, val timestamp: Long)

  private val consumerStreamConfig: Properties = Properties()

  init {
    consumerStreamConfig[StreamsConfig.BOOTSTRAP_SERVERS_CONFIG] = BOOTSTRAP_SERVERS
    consumerStreamConfig[StreamsConfig.APPLICATION_ID_CONFIG] = "music-charts"
    consumerStreamConfig[StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG] =
        Serdes.UUID().javaClass.name
    consumerStreamConfig[StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG] =
        Serdes.Integer().javaClass.name
  }

  companion object {
    val consumerStreamConfig = KafkaStream().consumerStreamConfig
  }
}

val log = LoggerFactory.getLogger(KafkaStream::class.java)

fun main() {
  val streamsBuilder = StreamsBuilder()

  val stream =
      streamsBuilder
          .stream("square-calculator", Consumed.with(Serdes.UUID(), Serdes.Integer()))
          .map { key, value -> KeyValue(key, value * value) }

  // Group the stream by UUID (key)
  val groupedStream = stream.groupByKey()

  // Aggregate sum and count using a Pair<Int, Long> (sum, count)
  // Aggregate sum and count using the RunningSumAndCount class
  val runningAvg: KTable<UUID, RunningSumAndCount> =
      groupedStream.aggregate(
          { RunningSumAndCount(0, 0, 0.0, 0) }, // Initializer
          { _, newValue, currentState ->
            val sum = currentState.sum + newValue
            val count = currentState.count + 1
            RunningSumAndCount(sum, count, sum.toDouble() / count, newValue)
          }, // Aggregation logic
          Materialized.`as`<UUID, RunningSumAndCount>(
                  Stores.persistentKeyValueStore("running-average-store"))
              .withKeySerde(Serdes.UUID())
              .withValueSerde(CustomSerdes.jsonSerde(RunningSumAndCount::class.java)))

  // Compute the running average
  //  val averageStream: KTable<UUID, Double> =
  //      runningAvg.mapValues { (sum, count) -> sum.toDouble() / count }

  runningAvg.toStream().foreach { _, value -> log.info("Average is {}", value) }

  //      .foreach { _, value -> log.info("Square is {}", value) }
  //          .groupBy { key, value -> key }
  //          .windowedBy(TimeWindows.ofSizeWithNoGrace(Duration.ofSeconds(10)))
  //          .count()

  val streams = KafkaStreams(streamsBuilder.build(), consumerStreamConfig)
  val latch = CountDownLatch(1)

  streams.start()

  // attach shutdown handler to catch control-c
  Runtime.getRuntime()
      .addShutdownHook(
          object : Thread("streams-pipe-shutdown-hook") {
            override fun run() {
              log.info("Shutting down Kafka Streams pipe application...")
              streams.close()
              latch.countDown()
            }
          })

  latch.await()
  System.exit(0)
}

data class RunningSumAndCount
@JsonCreator
constructor(
    @JsonProperty("sum") val sum: Int,
    @JsonProperty("count") val count: Long,
    @JsonProperty("avg") val avg: Double,
    @JsonProperty("value") val value: Int
)

object CustomSerdes {
  private val objectMapper = ObjectMapper()

  fun <T> jsonSerde(clazz: Class<T>): Serde<T> {
    return Serdes.serdeFrom(
        { _, data -> objectMapper.writeValueAsBytes(data) },
        { _, bytes -> objectMapper.readValue(bytes, clazz) })
  }
}
