package animeMangaSearcher

import java.util.*
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class MainActivity: HttpServlet() {

	public override fun doPost(req: HttpServletRequest, resp: HttpServletResponse) { catchAndLog {

		resp.contentType = "text/jsp"

		val username = req.getParameter("username")?.trim().orEmpty()
		val password = req.getParameter("password")?.trim().orEmpty()

		if (XMLParserMyAnimeList.credentialsAreValid(username, password)) {

			req.session.setAttribute("username", username)
			req.session.setAttribute("password", password)

			req.getRequestDispatcher("/WEB-INF/search.jsp").forward(req, resp)

		} else {
			req.setAttribute("errorMessage", "Invalid credentials. Try again.")
			req.getRequestDispatcher("index.jsp").forward(req, resp)
		}
	}}

	public override fun doGet(req: HttpServletRequest, resp: HttpServletResponse) { catchAndLog {

		resp.contentType = "text/jsp"

		val username = req.session.getAttribute("username") as String?
		val password = req.session.getAttribute("password") as String?

		if (username.isNullOrEmpty() || password.isNullOrEmpty()) {
			req.setAttribute("errorMessage", "You need to log in.")
			req.getRequestDispatcher("index.jsp").forward(req, resp)
			return
		}

		val titleName = req.getParameter("title")

		if (titleName != null) {
			displaySearchResultsFrom(titleName, req, resp, username!!, password!!)
		} else {
			displayFullResult(req, resp, username!!, password!!)
		}
	}}

	// Convenience

	private fun displaySearchResultsFrom(titleName: String, req: HttpServletRequest, resp: HttpServletResponse, username: String, password: String) {

		val trimmedTitle = titleName.trim()

		if (trimmedTitle.isNotEmpty()) {

			val animeResults = XMLParserMyAnimeList.searchFor(XMLParserMyAnimeList.SearchType.anime, trimmedTitle, username, password)
			val mangaResults = XMLParserMyAnimeList.searchFor(XMLParserMyAnimeList.SearchType.manga, trimmedTitle, username, password)

			val results = intermixArrays(animeResults, mangaResults)

			if (results.isNotEmpty()) {

				val result = StringBuilder()
				for (aResult in results) {
					result.append(imageLinkToResult(aResult.imageURL, aResult.title, aResult.type))
				}

				req.setAttribute("mangas", result.toString())
				req.getRequestDispatcher("/WEB-INF/search.jsp").forward(req, resp)

				return
			}
		}
		req.setAttribute("mangas", "Could not find anything. Try again.")
		req.getRequestDispatcher("/WEB-INF/search.jsp").forward(req, resp)
	}

	private fun imageLinkToResult(imageURL: String, title: String, type: String): String {

		val form = """ <div class="gallery"><form class="centeredBody" id="myForm" action="search" method="get">"""
		val titleAndType = title + "&" + type
		val imageLink = """<input name="titleAndType" value="${titleAndType.percentEncoded}" type="image" src="$imageURL"width="300" />"""
		val theTitle = """<div class="desc">$title ($type)</div>"""
		val endForm = "</form></div>"

		return form + imageLink + theTitle + endForm
	}

	private fun intermixArrays(animeResults: ArrayList<MyAnimeListResult>, mangaResults: ArrayList<MyAnimeListResult>): ArrayList<MyAnimeListResult> {

		val results = ArrayList<MyAnimeListResult>()

		if (animeResults.isEmpty() && mangaResults.isEmpty()) { return results }

		val smallerSize = if (animeResults.size < mangaResults.size) animeResults.size else mangaResults.size
		val animeIsBigger = animeResults.size > mangaResults.size

		var j = 0
		var k = 0
		for (i in 0 until smallerSize * 2) {

			val result: MyAnimeListResult?

			if (i % 2 == 0) {
				result = animeResults[j]
				j++
			} else {
				result = mangaResults[k]
				k++
			}

			results.add(result)
		}

		val rest = mangaResults.size - smallerSize

		for (i in 0 until rest) {

			val result: MyAnimeListResult?

			if (animeIsBigger) {
				result = animeResults[i + smallerSize]
			} else {
				result = mangaResults[i + smallerSize]
			}

			results.add(result)
		}

		return results
	}

	private fun displayFullResult(req: HttpServletRequest, resp: HttpServletResponse, username: String, password: String) {

		val titleAndType = req.getParameter("titleAndType") ?: return

		val parts = titleAndType.percentDecoded.split("&")

		val requestedTitle = parts[0]
		val requestedType = parts[1]

		var typeStr = "Manga"

		val results: ArrayList<MyAnimeListResult>?

		if (requestedType == "Manga" || requestedType == "Novel" || requestedType == "One-shot") {
			results = XMLParserMyAnimeList.searchFor(XMLParserMyAnimeList.SearchType.manga, requestedTitle, username, password)
		} else {
			results = XMLParserMyAnimeList.searchFor(XMLParserMyAnimeList.SearchType.anime, requestedTitle, username, password)
			typeStr = "Anime"
		}

		if (results.isNotEmpty()) {

			val bestResult = if (typeStr == "Anime") results.first() else results.filter { it.type == requestedType }.first()
			var externalLink = ""

			if (typeStr == "Anime") {

				val streamings = CSVParser.searchForAnimeStreamingsWithName(requestedTitle)

				if (streamings.isNotEmpty()) {
					externalLink = streamings.poll().link
				}
			} else {

				val mangas = JSONParserMangaEden.searchForMangasWithName(requestedTitle)

				if (mangas.isNotEmpty()) {
					externalLink = JSONParserMangaEden.getURLFromManga(mangas.poll())
				}
			}

			req.setAttribute("result", outputFullResultFrom(bestResult, externalLink, typeStr))
			req.getRequestDispatcher("/WEB-INF/result.jsp").forward(req, resp)
		}
	}

	private fun outputFullResultFrom(bestResult: MyAnimeListResult, externalLink: String, typeStr: String): String {

		val title = """<h2 class="bold">${bestResult.title}</h2>"""
		val imageURL = """<img class="resultImageStyle" src="${bestResult.imageURL}">"""

		val titleAndImage = """<div class="centeredBody">$title$imageURL</div>"""

		val contentBetweenBrackets = """\[[^\[\]]+\]"""
		val sourceLabel = """\(Source: \w+\)"""
		val synopsisWithoutExtraStuff = bestResult.synopsis.replace("$contentBetweenBrackets|$sourceLabel".toRegex(), "")

		val info = """<p class="bold">Synopsis: </p>"""
		val synopsis = if (synopsisWithoutExtraStuff.isNotEmpty()) "$info\n<p>$synopsisWithoutExtraStuff</p>" else ""
		val type = """<p><span class="bold">Type: </span><span>${bestResult.type}</span></p>"""

		var episodes = ""
		if (bestResult.episodes != "0") {
			val episodesOrChapters = if (typeStr == "Anime") "Episodes: " else "Chapters: "
			episodes = """<p><span class="bold">$episodesOrChapters</span><span>${bestResult.episodes}</span></p>"""
		}

		var score = ""
		if (bestResult.score != "0.00") {
			score = """<p><span class="bold">Score: </span><span>${bestResult.score}</span></p>"""
		}

		val status = if (bestResult.status.isNotEmpty()) """<p><span class="bold">Status: </span><span>${bestResult.status}</span></p>""" else ""

		var external = ""
		if (externalLink.isNotEmpty()) {
			if (typeStr == "Anime") {
				external = """<p><span class="bold">Streaming: </span><a href="$externalLink">$externalLink</a></p>"""
			} else {
				external = """<p><span class="bold">Read Online: </span><a href="$externalLink">$externalLink</a></p>"""
			}
		}

		return "$titleAndImage\n$synopsis\n$type\n$episodes\n$score\n$status\n$external"
	}
}
