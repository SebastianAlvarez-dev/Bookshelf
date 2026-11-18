package com.example.bookshelf

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import java.io.IOException
import retrofit2.HttpException

sealed interface BooksState {
    data object Loading : BooksState
    data class Success(val books: List<Book>) : BooksState
    data object Error : BooksState
}

class BooksViewModel(private val repository: BooksRepository) : ViewModel() {
    var state: BooksState by mutableStateOf(BooksState.Loading)
        private set
    init { load() }
    fun load() {
        viewModelScope.launch {
            state = BooksState.Loading
            state = try { BooksState.Success(repository.getBooks("jazz history")) }
            catch (_: IOException) { BooksState.Error }
            catch (_: HttpException) { BooksState.Error }
        }
    }
    companion object {
        val Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                BooksViewModel(booksRepository()) as T
        }
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MaterialTheme { BookshelfScreen() } }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookshelfScreen(viewModel: BooksViewModel = viewModel(factory = BooksViewModel.Factory)) {
    Scaffold(topBar = { TopAppBar(title = { Text("Bookshelf") }) }) { padding ->
        Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
            when (val state = viewModel.state) {
                BooksState.Loading -> CircularProgressIndicator()
                BooksState.Error -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("No se pudieron cargar los libros.")
                    Button(onClick = viewModel::load) { Text("Reintentar") }
                }
                is BooksState.Success -> if (state.books.isEmpty()) {
                    Text("No se encontraron libros.")
                } else LazyVerticalGrid(
                    columns = GridCells.Adaptive(150.dp),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(state.books, key = { it.id }) { book ->
                        Column {
                            AsyncImage(
                                model = book.volumeInfo.imageLinks?.thumbnail?.replace("http://", "https://"),
                                contentDescription = "Portada de ${book.volumeInfo.title ?: "libro"}",
                                contentScale = ContentScale.Fit,
                                modifier = Modifier.fillMaxWidth().height(210.dp)
                            )
                            Text(book.volumeInfo.title ?: "Sin título", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }
    }
}
