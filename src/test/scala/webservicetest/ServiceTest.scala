package webservicetest

import io.gatling.core.Predef._
import io.gatling.http.Predef._

import scala.concurrent.duration.{DurationDouble, DurationInt}

class ServiceTest extends Simulation {

  //define logger
  val logger = org.slf4j.LoggerFactory.getLogger("ServiceTest")

  //read tps and other parameters from frontline
  val userPerSec = System.getProperty("userPerSec", "1")
  val durationRampUPInMinute = System.getProperty("durationRampUPInMinute", "1")
  val durationSteadyStateInMinute = System.getProperty("durationSteadyStateInMinute", "1")

  //read environment from frontline
  val ENV = System.getProperty("env", "prf")

  //based on environment choose input file
  val dataFile = "user_" + ENV + ".csv"
  //val data = csv(dataFile).random

  var BASE_URL = ""

  //data file for authentication
  val auth_data = csv("Auth.csv").random

  //default http header
  val httpConf = http
    .baseUrl("http://localhost:8080/app/")
    .header("Accept", "application/json")

  var userID = ""

  val ticket_scenario = scenario("ticket_Scenario").
    exec(session => {session.set("env", ENV)}).
    feed(auth_data).exec(session => {
    userID = session("userID").as[String]
    session})

  //data file for webservice call
  val game_data = csv("Game.csv").random

  val apiCallSCN = scenario("service_Call").
    exec(session => {session.set("userID", userID)}).
    feed(game_data).
    exec(http("Get Last Posted Game")
      .get("videogames/${gameId}").check(status.is(200)))


  setUp(
    ticket_scenario.inject(constantUsersPerSec(userPerSec.toInt) during(1 seconds)
    ), apiCallSCN.inject(nothingFor(10 seconds), rampUsersPerSec(1) to userPerSec.toInt during (durationRampUPInMinute.toDouble minute),
      constantUsersPerSec(userPerSec.toInt) during (durationSteadyStateInMinute.toDouble minute))
  ).protocols(httpConf)

}




/*
package finalSimulation

import io.gatling.core.Predef._
import io.gatling.http.Predef._

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import scala.concurrent.duration._
import scala.util.Random

class VideoGameFullTest extends Simulation {

  val httpConf = http
    .baseUrl("http://video-game-db.eu-west-2.elasticbeanstalk.com/app/")
    .header("Accept", "application/json")

  /*** Variables ***/
  // runtime variables
  def userCount: Int = getProperty("USERS", "3").toInt
  def rampDuration: Int = getProperty("RAMP_DURATION", "10").toInt
  def testDuration: Int = getProperty("DURATION", "60").toInt

  // other variables
  var idNumbers = (20 to 1000).iterator
  val rnd = new Random()
  val now = LocalDate.now()
  val pattern = DateTimeFormatter.ofPattern("yyyy-MM-dd")

  /*** Helper Methods ***/
  private def getProperty(propertyName: String, defaultValue: String) = {
    Option(System.getenv(propertyName))
      .orElse(Option(System.getProperty(propertyName)))
      .getOrElse(defaultValue)
  }

  def randomString(length: Int) = {
    rnd.alphanumeric.filter(_.isLetter).take(length).mkString
  }

  def getRandomDate(startDate: LocalDate, random: Random): String = {
    startDate.minusDays(random.nextInt(30)).format(pattern)
  }

  /*** Custom Feeder ***/
  val customFeeder = Iterator.continually(Map(
    "gameId" -> idNumbers.next(),
    "name" -> ("Game-" + randomString(5)),
    "releaseDate" -> getRandomDate(now, rnd),
    "reviewScore" -> rnd.nextInt(100),
    "category" -> ("Category-" + randomString(6)),
    "rating" -> ("Rating-" + randomString(4))
  ))

  /*** Before ***/
  before {
    println(s"Running test with ${userCount} users")
    println(s"Ramping users over ${rampDuration} seconds")
    println(s"Total Test duration: ${testDuration} seconds")
  }

  /*** HTTP Calls ***/
  def getAllVideoGames() = {
    exec(
      http("Get All Video Games")
        .get("videogames")
        .check(status.is(200)))
  }

  def postNewGame() = {
    feed(customFeeder).
      exec(http("Post New Game")
        .post("videogames")
        .body(ElFileBody("bodies/NewGameTemplate.json")).asJson //template file goes in gating/resources/bodies
        .check(status.is(200)))
  }

  def getLastPostedGame() = {
    exec(http("Get Last Posted Game")
      .get("videogames/${gameId}")
      .check(jsonPath("$.name").is("${name}"))
      .check(status.is(200)))
  }

  def deleteLastPostedGame() = {
    exec(http("Delete Last Posted Game")
      .delete("videogames/${gameId}")
      .check(status.is(200)))
  }

  /*** Scenario Design ***/
  val scn = scenario("Video Game DB")
    .forever() {
      exec(getAllVideoGames())
        .pause(2)
        .exec(postNewGame())
        .pause(2)
        .exec(getLastPostedGame())
        .pause(2)
        .exec(deleteLastPostedGame())
    }

  /*** Setup Load Simulation ***/
  setUp(
    scn.inject(
      nothingFor(5.seconds),
      rampUsers(userCount) during (rampDuration.seconds))
  )
    .protocols(httpConf)
    .maxDuration(testDuration.seconds)

  /*** After ***/
  after {
    println("Stress test completed")
  }

}

 */