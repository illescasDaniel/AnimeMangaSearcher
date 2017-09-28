package animeMangaSearcher

import java.net.URLDecoder
import java.net.URLEncoder

inline fun <T> catchAndLog(runnable: () -> T): T? {
	return try {
		runnable()
	} catch (e: Exception) {
		e.printStackTrace()
		null
	}
}

val String.percentEncoded: String
	get() = catchAndLog { URLEncoder.encode(this, "UTF-8") } ?: this

val String.percentDecoded: String
	get() = catchAndLog { URLDecoder.decode(this, "UTF-8") } ?:this
