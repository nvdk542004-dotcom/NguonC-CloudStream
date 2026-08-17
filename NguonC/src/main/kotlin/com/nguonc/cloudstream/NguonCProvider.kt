package com.nguonc.cloudstream

import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.loadExtractor
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.net.URLEncoder

class NguonCProvider : MainAPI() {
    override var mainUrl = "https://phim.nguonc.com"
    override var name = "Nguồn C"
    override val lang = "vi"
    override val hasQuickSearch = true

    override val supportedTypes = setOf(
        TvType.Movie,
        TvType.TvSeries,
    )

    override val mainPage = mainPageOf(
        "$mainUrl/api/films/phim-moi-cap-nhat?page=" to "Phim mới cập nhật",
    )

    private val json = Json { ignoreUnknownKeys = true }

    private fun string(obj: JsonObject, key: String): String? =
        obj[key]?.jsonPrimitive?.contentOrNull

    private fun int(obj: JsonObject, key: String): Int? =
        obj[key]?.jsonPrimitive?.intOrNull

    private fun parseItems(text: String): List<JsonObject> {
        val root = json.parseToJsonElement(text).jsonObject
        return root["items"]?.jsonArray?.mapNotNull { it.jsonObject } ?: emptyList()
    }

    private fun itemToSearch(item: JsonObject): SearchResponse? {
        val title = string(item, "name")
            ?: string(item, "original_name")
            ?: return null

        val slug = string(item, "slug") ?: return null
        val poster = string(item, "poster_url") ?: string(item, "thumb_url")
        val year = int(item, "year")
        val totalEpisodes = int(item, "total_episodes")

        return if (totalEpisodes != null && totalEpisodes > 1) {
            newTvSeriesSearchResponse(title, "$mainUrl/$slug", TvType.TvSeries) {
                this.posterUrl = poster
                this.year = year
            }
        } else {
            newMovieSearchResponse(title, "$mainUrl/$slug", TvType.Movie) {
                this.posterUrl = poster
                this.year = year
            }
        }
    }

    override suspend fun search(query: String): List<SearchResponse> {
        val encoded = URLEncoder.encode(query, "UTF-8")
        return app.get("$mainUrl/api/films/search?keyword=$encoded")
            .let { parseItems(it.text) }
            .mapNotNull(::itemToSearch)
    }

    override suspend fun getMainPage(
        page: Int,
        request: MainPageRequest
    ): HomePageResponse {
        val url = "${request.data}${page}"
        val items = app.get(url)
            .let { parseItems(it.text) }
            .mapNotNull(::itemToSearch)

        return newHomePageResponse(request.name, items, items.isNotEmpty())
    }

    override suspend fun load(url: String): LoadResponse {
        val slug = url.trimEnd('/').substringAfterLast('/')
        if (slug.isBlank()) {
            throw ErrorLoadingException("Nguồn C: slug phim không hợp lệ")
        }

        val response = app.get("$mainUrl/api/film/$slug")
        val root = json.parseToJsonElement(response.text).jsonObject
        val movie = root["movie"]?.jsonObject
            ?: throw ErrorLoadingException("Nguồn C: không tìm thấy dữ liệu phim")

        val title = string(movie, "name") ?: slug
        val poster = string(movie, "poster_url") ?: string(movie, "thumb_url")

        val year = movie["category"]?.jsonObject
            ?.get("3")?.jsonObject
            ?.get("list")?.jsonArray
            ?.firstOrNull()?.jsonObject
            ?.let { string(it, "name")?.toIntOrNull() }

        val description = string(movie, "description")
            ?.replace(Regex("<[^>]*>"), "")
            ?.replace(Regex("\\s+"), " ")
            ?.trim()

        val totalEpisodes = int(movie, "total_episodes") ?: 0
        val servers = movie["episodes"]?.jsonArray ?: emptyList()

        val episodes = buildList {
            for (serverElement in servers) {
                val server = serverElement.jsonObject
                val serverName = string(server, "server_name") ?: "Server"
                val items = server["items"]?.jsonArray ?: continue

                for (itemElement in items) {
                    val item = itemElement.jsonObject
                    val episodeName = string(item, "name") ?: continue
                    val embed = string(item, "embed") ?: continue

                    add(
                        newEpisode(embed) {
                            name = "$serverName • Tập $episodeName"
                            episode = episodeName.toIntOrNull()
                        }
                    )
                }
            }
        }

        if (totalEpisodes <= 1) {
            val first = episodes.firstOrNull()?.data
                ?: throw ErrorLoadingException("Nguồn C: không tìm thấy link phát")

            return newMovieLoadResponse(title, url, TvType.Movie, first) {
                this.posterUrl = poster
                this.year = year
                this.plot = description
            }
        }

        return newTvSeriesLoadResponse(title, url, TvType.TvSeries, episodes) {
            this.posterUrl = poster
            this.year = year
            this.plot = description
        }
    }

    override suspend fun loadLinks(
        data: String,
        isCasting: Boolean,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ): Boolean {
        loadExtractor(
            data,
            mainUrl,
            subtitleCallback,
            callback,
        )
        return true
    }
}
