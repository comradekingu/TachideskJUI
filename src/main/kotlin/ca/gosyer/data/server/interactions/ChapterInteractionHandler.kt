/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.data.server.interactions

import ca.gosyer.data.models.Chapter
import ca.gosyer.data.models.Manga
import ca.gosyer.data.server.Http
import ca.gosyer.data.server.ServerPreferences
import ca.gosyer.data.server.requests.deleteDownloadChapterRequest
import ca.gosyer.data.server.requests.getChapterQuery
import ca.gosyer.data.server.requests.getMangaChaptersQuery
import ca.gosyer.data.server.requests.getPageQuery
import ca.gosyer.data.server.requests.queueDownloadChapterRequest
import ca.gosyer.data.server.requests.updateChapterMetaRequest
import ca.gosyer.data.server.requests.updateChapterRequest
import ca.gosyer.util.lang.withIOContext
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.parameter
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpMethod
import io.ktor.http.Parameters
import javax.inject.Inject

class ChapterInteractionHandler @Inject constructor(
    client: Http,
    serverPreferences: ServerPreferences
) : BaseInteractionHandler(client, serverPreferences) {

    suspend fun getChapters(mangaId: Long, refresh: Boolean = false) = withIOContext {
        client.getRepeat<List<Chapter>>(
            serverUrl + getMangaChaptersQuery(mangaId)
        ) {
            url {
                if (refresh) {
                    parameter("onlineFetch", true)
                }
            }
        }
    }

    suspend fun getChapters(manga: Manga, refresh: Boolean = false) = getChapters(manga.id, refresh)

    suspend fun getChapter(mangaId: Long, chapterIndex: Int) = withIOContext {
        client.getRepeat<Chapter>(
            serverUrl + getChapterQuery(mangaId, chapterIndex)
        )
    }

    suspend fun getChapter(chapter: Chapter) = getChapter(chapter.mangaId, chapter.index)

    suspend fun getChapter(manga: Manga, chapterIndex: Int) = getChapter(manga.id, chapterIndex)

    suspend fun getChapter(manga: Manga, chapter: Chapter) = getChapter(manga.id, chapter.index)

    suspend fun updateChapter(
        mangaId: Long,
        chapterIndex: Int,
        read: Boolean? = null,
        bookmarked: Boolean? = null,
        lastPageRead: Int? = null,
        markPreviousRead: Boolean? = null
    ) = withIOContext {
        client.submitFormRepeat<HttpResponse>(
            serverUrl + updateChapterRequest(mangaId, chapterIndex),
            formParameters = Parameters.build {
                if (read != null) {
                    append("read", read.toString())
                }
                if (bookmarked != null) {
                    append("bookmarked", bookmarked.toString())
                }
                if (lastPageRead != null) {
                    append("lastPageRead", lastPageRead.toString())
                }
                if (markPreviousRead != null) {
                    append("markPrevRead", markPreviousRead.toString())
                }
            }
        ) {
            method = HttpMethod.Patch
        }
    }

    suspend fun updateChapter(
        manga: Manga,
        chapterIndex: Int,
        read: Boolean? = null,
        bookmarked: Boolean? = null,
        lastPageRead: Int? = null,
        markPreviousRead: Boolean? = null
    ) = updateChapter(
        manga.id,
        chapterIndex,
        read,
        bookmarked,
        lastPageRead,
        markPreviousRead
    )

    suspend fun updateChapter(
        manga: Manga,
        chapter: Chapter,
        read: Boolean? = null,
        bookmarked: Boolean? = null,
        lastPageRead: Int? = null,
        markPreviousRead: Boolean? = null
    ) = updateChapter(
        manga.id,
        chapter.index,
        read,
        bookmarked,
        lastPageRead,
        markPreviousRead
    )

    suspend fun getPage(mangaId: Long, chapterIndex: Int, pageNum: Int, block: HttpRequestBuilder.() -> Unit) = withIOContext {
        imageFromUrl(
            client,
            serverUrl + getPageQuery(mangaId, chapterIndex, pageNum),
            block
        )
    }

    suspend fun getPage(chapter: Chapter, pageNum: Int, block: HttpRequestBuilder.() -> Unit) = getPage(chapter.mangaId, chapter.index, pageNum, block)

    suspend fun getPage(manga: Manga, chapterIndex: Int, pageNum: Int, block: HttpRequestBuilder.() -> Unit) = getPage(manga.id, chapterIndex, pageNum, block)

    suspend fun getPage(manga: Manga, chapter: Chapter, pageNum: Int, block: HttpRequestBuilder.() -> Unit) = getPage(manga.id, chapter.index, pageNum, block)

    suspend fun queueChapterDownload(mangaId: Long, chapterIndex: Int) = withIOContext {
        client.getRepeat<HttpResponse>(
            serverUrl + queueDownloadChapterRequest(mangaId, chapterIndex)
        )
    }

    suspend fun queueChapterDownload(chapter: Chapter) = queueChapterDownload(chapter.mangaId, chapter.index)

    suspend fun queueChapterDownload(manga: Manga, chapterIndex: Int) = queueChapterDownload(manga.id, chapterIndex)

    suspend fun queueChapterDownload(manga: Manga, chapter: Chapter) = queueChapterDownload(manga.id, chapter.index)

    suspend fun deleteChapterDownload(mangaId: Long, chapterIndex: Int) = withIOContext {
        client.deleteRepeat<HttpResponse>(
            serverUrl + deleteDownloadChapterRequest(mangaId, chapterIndex)
        )
    }

    suspend fun deleteChapterDownload(chapter: Chapter) = deleteChapterDownload(chapter.mangaId, chapter.index)

    suspend fun deleteChapterDownload(manga: Manga, chapterIndex: Int) = deleteChapterDownload(manga.id, chapterIndex)

    suspend fun deleteChapterDownload(manga: Manga, chapter: Chapter) = deleteChapterDownload(manga.id, chapter.index)

    suspend fun updateChapterMeta(mangaId: Long, chapterIndex: Int, key: String, value: String) = withIOContext {
        client.submitFormRepeat<HttpResponse>(
            serverUrl + updateChapterMetaRequest(mangaId, chapterIndex),
            formParameters = Parameters.build {
                append("key", key)
                append("value", value)
            }
        ) {
            method = HttpMethod.Patch
        }
    }

    suspend fun updateChapterMeta(chapter: Chapter, key: String, value: String) = updateChapterMeta(chapter.mangaId, chapter.index, key, value)

    suspend fun updateChapterMeta(manga: Manga, chapterIndex: Int, key: String, value: String) = updateChapterMeta(manga.id, chapterIndex, key, value)

    suspend fun updateChapterMeta(manga: Manga, chapter: Chapter, key: String, value: String) = updateChapterMeta(manga.id, chapter.index, key, value)
}
