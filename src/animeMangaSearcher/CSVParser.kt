package animeMangaSearcher

import org.apache.commons.lang3.StringUtils
import java.io.BufferedReader
import java.io.FileReader
import java.util.*

internal object CSVParser {

	var fileName = "/Users/Daniel/Documents/IntelliJ IDEA/AnimeMangaSearcher/Resources/gogoanimeV2.csv"
	private val streamings = ArrayList<Streaming>()

	private fun getAllStreamings() {

		val cvsSplitBy = ";"

		val fileReader = FileReader(CSVParser.fileName)
		val bufferedReader = BufferedReader(fileReader)

		for (line in bufferedReader.lines()) {
			val fields = line.split(cvsSplitBy.toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

			val title = fields[0]
			val link = fields[1]

			streamings.add(Streaming(title, link))
		}
	}

	fun searchForAnimeStreamingsWithName(name: String): PriorityQueue<Streaming> {

		if (streamings.isEmpty()) {
			getAllStreamings()
		}

		val streamingsSortedByDistance = PriorityQueue(10, Collections.reverseOrder<Streaming>())

		for (streaming in streamings) {
			val distance = StringUtils.getJaroWinklerDistance(name, streaming.title)

			if (distance >= 0.8) {
				streaming.JWDistance = distance
				streamingsSortedByDistance.add(streaming)
			}
		}

		return streamingsSortedByDistance
	}
}
