package animeMangaSearcher

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.util.*

@JsonIgnoreProperties(ignoreUnknown = true)
internal class MangaEDEN: Comparable<MangaEDEN> {

	var title = ""
	var id = ""

	var mangas = ArrayList<MangaEDEN>()

	//

	var JWDistance = 0.0

	override fun compareTo(other: MangaEDEN): Int {
		return if (JWDistance < other.JWDistance) -1 else if (JWDistance > other.JWDistance) 1 else 0
	}
}
