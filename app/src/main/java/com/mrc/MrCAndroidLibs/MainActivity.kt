package com.mrc.MrCAndroidLibs

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mrc.MrCAndroidLibs.data.Post
import com.mrc.MrCAndroidLibs.data.ResolvedResult
import com.mrc.MrCAndroidLibs.ui.theme.MrCAndroidLibsTheme
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MrCAndroidLibsTheme {

                val uiState = viewModel.uiState.collectAsStateWithLifecycle()

                PostsScreen(
                    uiState = uiState.value, onLoad = { viewModel.getPosts() })
            }
        }
    }
}

@Composable
fun PostsScreen(
    uiState: UiState, onLoad: () -> Unit
) {
    LaunchedEffect(Unit) {
        onLoad()
    }

    when (val result = uiState.fetchPostsOperation) {

        is ResolvedResult.Idle -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Idle")
            }
        }

        is ResolvedResult.Loading -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        is ResolvedResult.Success -> {
            PostsList(posts = result.data)
        }

        is ResolvedResult.Error -> {
            ErrorView(
                message = result.message ?: "Unknown error", onRetry = onLoad
            )
        }

        is ResolvedResult.Fail -> {
            ErrorView(
                message = result.throwable.message ?: "Unexpected error", onRetry = onLoad
            )
        }
    }
}

@Composable
fun PostsList(posts: List<Post>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        items(posts) { post ->
            PostItem(post)
        }
    }
}

@Composable
fun PostItem(post: Post) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = post.title, style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "ID: ${post.userId}", style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
fun ErrorView(
    message: String, onRetry: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = message)
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = onRetry) {
                Text("Retry")
            }
        }
    }
}