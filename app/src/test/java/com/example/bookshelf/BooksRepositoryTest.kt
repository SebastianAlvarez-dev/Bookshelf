package com.example.bookshelf

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class BooksRepositoryTest {
    @Test fun searchFetchesDetailsForEachId() = runBlocking {
        val calls = mutableListOf<String>()
        val service = object : BooksService {
            override suspend fun search(query: String): SearchResponse {
                assertEquals("jazz", query)
                return SearchResponse(listOf(BookId("1"), BookId("2")))
            }
            override suspend fun getBook(id: String): Book {
                calls.add(id)
                return Book(id, VolumeInfo("Book $id"))
            }
        }
        val books = NetworkBooksRepository(service).getBooks("jazz")
        assertEquals(listOf("1", "2"), calls)
        assertEquals(listOf("Book 1", "Book 2"), books.map { it.volumeInfo.title })
    }
}
