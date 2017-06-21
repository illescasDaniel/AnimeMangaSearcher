package animeMangaSearcher

internal class Streaming constructor(var title: String, var link: String): Comparable<Streaming> {

	var JWDistance = 0.0

	override fun compareTo(other: Streaming): Int {
		return if (JWDistance < other.JWDistance) -1 else if (JWDistance > other.JWDistance) 1 else 0
	}
}
