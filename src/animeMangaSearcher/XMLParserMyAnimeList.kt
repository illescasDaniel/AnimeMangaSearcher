package animeMangaSearcher

import org.w3c.dom.Element
import org.w3c.dom.Node
import java.io.BufferedReader
import java.io.ByteArrayInputStream
import java.io.InputStreamReader
import java.net.URL
import javax.xml.parsers.DocumentBuilderFactory

internal object XMLParserMyAnimeList {

	private fun getResultsFrom(xml: String, isAnime: Boolean): ArrayList<MyAnimeListResult> {

		val results = ArrayList<MyAnimeListResult>()

		if (xml.isEmpty() || xml.toByteArray().isEmpty()) {
			return results
		}

		catchAndLog {

			val stream = ByteArrayInputStream(xml.toByteArray(charset("UTF-8")))

			val dbFactory = DocumentBuilderFactory.newInstance()
			val dBuilder = dbFactory.newDocumentBuilder()

			val doc = dBuilder.parse(stream)

			doc.documentElement.normalize()

			val nList = doc.getElementsByTagName("entry")

			for (temp in 0 until nList.length) {

				val nNode = nList.item(temp)

				if (nNode.nodeType == Node.ELEMENT_NODE) {

					val eElement = nNode as Element

					val title: String
					val episodes: String
					val score: String
					val type: String
					val status: String
					val synopsis: String
					val imageURL: String

					title = eElement.getElementsByTagName("title").item(0).textContent.orEmpty()

					if (isAnime) {
						episodes = eElement.getElementsByTagName("episodes").item(0).textContent.orEmpty()
					} else {
						episodes = eElement.getElementsByTagName("chapters").item(0).textContent.orEmpty()
					}

					score = eElement.getElementsByTagName("score").item(0).textContent.orEmpty()
					type = eElement.getElementsByTagName("type").item(0).textContent.orEmpty()
					status = eElement.getElementsByTagName("status").item(0).textContent.orEmpty()
					synopsis = eElement.getElementsByTagName("synopsis").item(0).textContent.orEmpty()
					imageURL = eElement.getElementsByTagName("image").item(0).textContent.orEmpty()

					results.add(MyAnimeListResult(title, episodes, score, type, status, synopsis, imageURL))
				}
			}
		}

		return results
	}

	enum class SearchType(val value: String) {
		anime("anime"),
		manga("manga")
	}

	fun searchFor(type: SearchType, title: String, username: String, password: String): ArrayList<MyAnimeListResult> {

		val encodedURLString = "https://myanimelist.net/api/${type.value}/search.xml?q=${title.percentEncoded}"

		var xml = getWebContentFromURL(encodedURLString, username, password)
		while (xml == "-1") {
			xml = getWebContentFromURL(encodedURLString, username, password)
		}

		return getResultsFrom(xml, isAnime = (type == SearchType.anime))
	}

	fun credentialsAreValid(username: String, password: String): Boolean {

		if (username.isEmpty() || password.isEmpty()) {
			return false
		}

		try {

			val url = URL("https://myanimelist.net/api/account/verify_credentials.xml")

			val uc = url.openConnection()

			val userpass = username + ":" + password
			val basicAuth = "Basic " + javax.xml.bind.DatatypeConverter.printBase64Binary(userpass.toByteArray())

			uc.setRequestProperty("Authorization", basicAuth)

			val inputStream = uc.getInputStream()

			val buffReader = BufferedReader(InputStreamReader(inputStream))
			val response = StringBuilder()

			for (line in buffReader.lines()) {
				response.append(line)
				response.append('\n')
			}

			buffReader.close()

			return !response.toString().contains("Invalid credentials")

		} catch (e: Exception) {
			e.printStackTrace()
			return false
		}
	}

	private fun getWebContentFromURL(encodedURLString: String, username: String, password: String): String {

		try {

			val url = URL(encodedURLString)

			val uc = url.openConnection()

			val userpass = "$username:$password"
			val basicAuth = "Basic ${javax.xml.bind.DatatypeConverter.printBase64Binary(userpass.toByteArray())}"

			uc.setRequestProperty("Authorization", basicAuth)

			uc.connectTimeout = 5 * 1000
			uc.readTimeout = 5 * 1000
			val inputStream = uc.getInputStream()

			val buffReader = BufferedReader(InputStreamReader(inputStream))

			val response = StringBuilder()

			for (line in buffReader.lines()) {
				response.append(line)
				response.append('\n')
			}

			buffReader.close()

			return response.toString()

		} catch (e: Exception) {
			return "-1"
		}
	}
}
