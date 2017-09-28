package animeMangaSearcher

import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.commons.lang3.StringUtils
import java.io.File
import java.net.URL
import java.util.*

internal object JSONParserMangaEden {

	private var outputMangas = ArrayList<MangaEDEN>()

	private fun getAllMangas() {

		val mapper = ObjectMapper()
		val jsonLocation = "/Users/Daniel/Documents/IntelliJ IDEA/AnimeMangaSearcher/Resources/mangaEden.json"

		catchAndLog {
			val outputObject = mapper.readValue(File(jsonLocation), MangaEDEN::class.java)
			outputMangas = outputObject.mangas
		}
	}

	fun searchForMangasWithName(name: String): PriorityQueue<MangaEDEN> {

		if (outputMangas.isEmpty()) {
			getAllMangas()
		}

		val mangasSortedByDistance = PriorityQueue(10, Collections.reverseOrder<MangaEDEN>())

		for (manga in outputMangas) {

			val distance = StringUtils.getJaroWinklerDistance(name, manga.title)

			if (distance >= 0.8) {
				manga.JWDistance = distance
				mangasSortedByDistance.add(manga)
			}
		}

		return mangasSortedByDistance
	}

	@Throws(Exception::class)
	private fun getText(url: String): String {
		return Scanner(URL(url).openStream(), "UTF-8").useDelimiter("\\A").next()
	}

	fun getURLFromManga(manga: MangaEDEN): String {

		val idOutput = catchAndLog { getText(url = "http://www.mangaeden.com/api/manga/${manga.id}") }.orEmpty()

		val urlStringPrefix = "\"url\": \""
		val index = idOutput.indexOf(urlStringPrefix)

		val url: String

		url = if (index > -1) {
			idOutput.substring(index + urlStringPrefix.length, idOutput.length - 4)
		} else {
			val encodedTitle = manga.title.percentEncoded.toLowerCase().replace("+", "-")
			"http://www.mangaeden.com/en-manga/$encodedTitle"
		}

		return url
	}
}