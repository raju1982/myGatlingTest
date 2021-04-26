package test

object Helper {

  def generatePayload(): String ={
    //read kafka payload from file or store them as val in the scala object
    //
    return "{\n  \"id\": \"%s\",\n  \"name\": \"testGame\",\n  \"releaseDate\": \"2021-04-25T01:29:39.617Z\",\n  \"reviewScore\": 0,\n  \"category\": \"test\",\n  \"rating\": \"5\"\n}";
  }


}
