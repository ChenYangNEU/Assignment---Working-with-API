import io.circe.parser.decode
import io.circe.generic.auto._

object WorkingAPI {
  case class Playlist(href: String, limit: Int, next: String, offset: Int, previous: Option[String], total: Int, items: List[Item])
  case class Item(track: Track)
  case class Track(artists: List[Artist], duration_ms: Long, name: String)
  case class Artist(id: String)
  case class PlaylistTrack(artists: List[String], songName: String, durationMs: Long)
  case class TokenResponse(access_token: String, token_type: String, expires_in: Int)
  case class ArtistDetails(name: String, popularity: Int)

  def getPlaylistAndArtists(playId: String, accessToken: String): List[(PlaylistTrack, Option[ArtistDetails])] = {
    val url = s"https://api.spotify.com/v1/playlists/$playId/tracks"
    val headers = Map("Authorization" -> s"Bearer $accessToken")
    val response = requests.get(url, headers = headers)
    val json = response.text()
    val playlist = decode[Playlist](json).getOrElse(Playlist("", 0, "", 0, None, 0, List.empty))
    playlist.items.map { item =>
      val artistsIds = item.track.artists.map(_.id)
      val playlistTrack = PlaylistTrack(artistsIds, item.track.name, item.track.duration_ms)
      val artistDetails = getArtistDetails(artistsIds.headOption.getOrElse(""), accessToken)
      (playlistTrack, artistDetails)
    }
  }

  def getArtistDetails(artistId: String, accessToken: String): Option[ArtistDetails] = {
    if (artistId.nonEmpty) {
      val url = s"https://api.spotify.com/v1/artists/$artistId"
      val headers = Map("Authorization" -> s"Bearer $accessToken")
      val response = requests.get(url, headers = headers)
      val json = response.text()
      decode[ArtistDetails](json).toOption
    } else None
  }

  def getToken(clientId: String, clientSecret: String): Option[String] = {
    val url = "https://accounts.spotify.com/api/token"
    val params = Map("grant_type" -> "client_credentials", "client_id" -> clientId, "client_secret" -> clientSecret)
    val response = requests.post(url, data = params)
    val json = response.text()
    decode[TokenResponse](json).toOption.map(_.access_token)
  }

  def printPlaylistAndArtists(playlistTracksWithArtists: List[(PlaylistTrack, Option[ArtistDetails])]): Unit = {
    println("Part 1:")
    playlistTracksWithArtists.foreach { case (track, _) => println(s"${track.songName} , ${track.durationMs}") }
    println("Part 2:")
    val artistDetailsMap = playlistTracksWithArtists
      .flatMap { case (_, artistDetailsOption) => artistDetailsOption }.groupBy(_.name)
      .mapValues(_.map(_.popularity).max)
      .toSeq.sortBy(-_._2)
    artistDetailsMap.foreach { case (artist, popularity) => println(s"$artist : $popularity") }
  }

  def main(args: Array[String]): Unit = {
    val Id = "60f5ea88d89c4032b8e49e4c36774562"
    val Secret = "d9f24178ca4b4bbab9f6181d65940e2d"
    val TokenOption = getToken(Id, Secret)
    TokenOption match {
      case Some(accessToken) =>
        val playId = "5Rrf7mqN8uus2AaQQQNdc1"
        val playlistTracksWithArtists = getPlaylistAndArtists(playId, accessToken)
        val top10Songs = playlistTracksWithArtists.sortBy { case (track, _) => -track.durationMs }.take(10)
        printPlaylistAndArtists(top10Songs)
      case None =>
        println("Failed connected")
    }
  }
}


