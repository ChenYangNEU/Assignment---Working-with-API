import io.circe.parser.decode
import io.circe.generic.auto._

object WorkingAPI {
  case class Playlist(href:String, limit:Int, next:String, offset:Int, previous: Option[String], total:Int, items: List[Item])
  case class Item(track: Track)
  case class Track(artists: List[Artist],duration_ms: Long,name: String )
  case class Artist(id:String)
  case class PlaylistTrack(artists: List[String],songName: String, durationMs: Long)
  case class tokenResponse(access_token: String, token_type: String, expires_in: Int)
  case class ArtistDetails(name: String, popularity: Int)

  def getPlaylist(playId: String, accessToken: String): List[PlaylistTrack] = {
    val url = s"https://api.spotify.com/v1/playlists/$playId/tracks"
    val headers = Map("Authorization" -> s"Bearer $accessToken")
    val response = requests.get(url, headers = headers)
    val json = response.text()
    decode[Playlist](json).getOrElse(Playlist("", 0, "", 0, None, 0, List.empty)).items.map(item =>
      PlaylistTrack(item.track.artists.map(_.id), item.track.name, item.track.duration_ms)
    )
  }

  def getArtist(artistId: String, accessToken: String): Option[ArtistDetails] = {
    val url = s"https://api.spotify.com/v1/artists/$artistId"
    val headers = Map("Authorization" -> s"Bearer $accessToken")
    val response = requests.get(url, headers = headers)
    val json = response.text()
    decode[ArtistDetails](json).toOption
  }

  def getToken(clientId: String, clientSecret: String): Option[String] = {
    val url = "https://accounts.spotify.com/api/token"
    val params = Map("grant_type" -> "client_credentials", "client_id" -> clientId, "client_secret" -> clientSecret)
    val response = requests.post(url, data = params)
    val json = response.text()
    decode[tokenResponse](json).toOption.map(_.access_token)
  }

  def main(args: Array[String]): Unit = {
    val Id = "60f5ea88d89c4032b8e49e4c36774562"
    val Secret = "d9f24178ca4b4bbab9f6181d65940e2d"
    val TokenOption = getToken(Id, Secret)
    TokenOption match {
      case Some(accessToken) =>
        val playId = "5Rrf7mqN8uus2AaQQQNdc1"
        val playlistTracks = getPlaylist(playId, accessToken)
        val top10Songs = playlistTracks.sortBy(-_.durationMs).take(10)
        println("Part 1:")
        top10Songs.foreach(track => println(s"${track.songName} , ${track.durationMs}"))
        println("Part 2:")
        val followerMap = top10Songs
          .flatMap(_.artists)
          .distinct
          .flatMap { artistId => getArtist(artistId, accessToken).map(artistDetails => (artistDetails.name, artistDetails.popularity))}
          .groupBy(_._1)
          .mapValues(_.map(_._2).max)
          .toSeq.sortBy(-_._2)
        followerMap.foreach { case (artist, popularity) => println(s"$artist : $popularity")}
      case None =>
        println("Failed connected")
    }
  }
}

