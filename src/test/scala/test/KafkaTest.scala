package test

import com.github.mnogu.gatling.kafka.Predef._
import io.gatling.core.Predef._
import org.apache.kafka.clients.producer.ProducerConfig

import scala.concurrent.duration._
import scala.language.postfixOps

class KafkaTest extends Simulation {

  val logger = org.slf4j.LoggerFactory.getLogger("KafkaTest")
  val userPerSec = System.getProperty("userPerSec", "1")
  val durationRampUPInMinute = System.getProperty("durationRampUPInMinute", "1")
  val durationSteadyStateInMinute = System.getProperty("durationSteadyStateInMinute", "1")

  val kafkaConf = kafka
    // Kafka topic name
    .topic("quickstart-events")
    // Kafka producer configs
    .properties(
      Map(
        ProducerConfig.ACKS_CONFIG -> "1",
        // list of Kafka broker hostname and port pairs
        ProducerConfig.BOOTSTRAP_SERVERS_CONFIG -> "localhost:9092",
        // in most cases, StringSerializer or ByteArraySerializer
        ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG ->
          "org.apache.kafka.common.serialization.StringSerializer",
        ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG ->
          "org.apache.kafka.common.serialization.StringSerializer"))


  val scn = scenario("Kafka Test")
    .exec(session => {
      val kafkaMessage = Helper.generatePayload()
      logger.warn(kafkaMessage)
      session.set("kafkaMessage", kafkaMessage)
    })
    .exec(
      kafka("request")
        .send[String]("${kafkaMessage}"))

 setUp(
    scn
      .inject(rampUsersPerSec(1) to userPerSec.toInt during (durationRampUPInMinute.toDouble minute),
        constantUsersPerSec(userPerSec.toInt) during (durationSteadyStateInMinute.toDouble minute))
  )
    .protocols(kafkaConf)

}