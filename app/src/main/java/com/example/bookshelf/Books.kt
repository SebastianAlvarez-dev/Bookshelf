package com.example.bookshelf

import retrofit2.Retrofit
import okhttp3.OkHttpClient
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

data class SearchResponse(val items: List<BookId>? = null)
data class BookId(val id: String)
data class Book(val id: String, val volumeInfo: VolumeInfo)
data class VolumeInfo(val title: String? = null, val imageLinks: ImageLinks? = null)
data class ImageLinks(val thumbnail: String? = null)

interface BooksService {
    @GET("volumes") suspend fun search(@Query("q") query: String): SearchResponse
    @GET("volumes/{id}") suspend fun getBook(@Path("id") id: String): Book
}

interface BooksRepository {
    suspend fun getBooks(query: String): List<Book>
}

class NetworkBooksRepository(private val service: BooksService) : BooksRepository {
    override suspend fun getBooks(query: String): List<Book> =
        service.search(query).items.orEmpty().map { service.getBook(it.id) }
}

fun booksRepository(): BooksRepository = NetworkBooksRepository(
    Retrofit.Builder().baseUrl("https://www.googleapis.com/books/v1/")
        .client(OkHttpClient.Builder().addInterceptor { chain ->
            val request = chain.request()
            val url = request.url.newBuilder().addQueryParameter("key", BuildConfig.BOOKS_API_KEY).build()
            chain.proceed(request.newBuilder().url(url).build())
        }.build())
        .addConverterFactory(GsonConverterFactory.create()).build().create(BooksService::class.java)
)
