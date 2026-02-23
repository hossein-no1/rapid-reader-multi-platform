package com.util.rsvp

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.util.rsvp.component.InputPDF
import com.util.rsvp.model.PdfHistoryItem
import kotlinx.coroutines.launch

@Composable
fun GateScreen(
    modifier: Modifier = Modifier,
    onContinue: (String) -> Unit,
    onPdfPicked: (PdfHistoryItem) -> Unit,
) {

    val scope = rememberCoroutineScope()

    var fileText by remember { mutableStateOf("") }
    var urlText by remember { mutableStateOf("") }
    var pasteText by remember { mutableStateOf("") }

    var url by remember { mutableStateOf("") }
    var urlState by remember { mutableStateOf<UrlDownloadState>(UrlDownloadState.Idle) }

    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("File", "Link", "Paste")
    val currentText = when (selectedTabIndex) {
        0 -> fileText
        1 -> urlText
        else -> pasteText
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center,
        content = {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction = .9F)
                    .padding(horizontal = 16.dp)
                    .clip(shape = RoundedCornerShape(size = 8.dp))
                    .background(color = MaterialTheme.colorScheme.onBackground.copy(alpha = .05F)),
                contentAlignment = Alignment.Center,
                content = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(all = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(
                            space = 4.dp,
                            alignment = Alignment.Top
                        ),
                        horizontalAlignment = Alignment.Start,
                        content = {
                            Text(
                                text = "Start reading",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onBackground,
                                fontSize = 16.sp
                            )
                            Text(
                                text = "Drop a document or paste a link.\n" +
                                        "We'll present it word-by-word for focused reading.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray,
                                fontSize = 12.sp,
                                textAlign = TextAlign.Start
                            )
                            Spacer(modifier = Modifier.height(height = 12.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(shape = RoundedCornerShape(8.dp))
                                    .background(
                                        color = MaterialTheme.colorScheme.onBackground.copy(
                                            alpha = 0.02f
                                        )
                                    )
                                    .padding(all = 6.dp),
                                content = {
                                    SecondaryTabRow(
                                        selectedTabIndex = selectedTabIndex,
                                        modifier = Modifier.fillMaxWidth(),
                                        containerColor = Color.Transparent,
                                        contentColor = TabRowDefaults.primaryContentColor,
                                        tabs = {
                                            tabs.forEachIndexed { index, title ->
                                                val selected = selectedTabIndex == index
                                                Tab(
                                                    modifier = Modifier
                                                        .width(80.dp)
                                                        .clip(shape = RoundedCornerShape(6.dp))
                                                        .background(
                                                            color = if (selected) MaterialTheme.colorScheme.background else Color.Transparent
                                                        ),
                                                    selected = selected,
                                                    onClick = { selectedTabIndex = index },
                                                    selectedContentColor = MaterialTheme.colorScheme.onBackground,
                                                    unselectedContentColor = MaterialTheme.colorScheme.onBackground.copy(
                                                        alpha = 0.7f
                                                    ),
                                                    text = {
                                                        Text(
                                                            text = title,
                                                            color = if (selected) MaterialTheme.colorScheme.onBackground else Color.Gray,
                                                            style = MaterialTheme.typography.labelSmall,
                                                            fontSize = 12.sp
                                                        )
                                                    }
                                                )
                                            }
                                        },
                                        indicator = {},
                                        divider = {}
                                    )
                                }
                            )
                            Spacer(modifier = Modifier.height(height = 12.dp))

                            AnimatedContent(
                                targetState = selectedTabIndex,
                                transitionSpec = {
                                    val direction = if (targetState > initialState) 1 else -1
                                    ((
                                            slideInHorizontally(
                                                animationSpec = tween(220),
                                                initialOffsetX = { direction * (it / 10) }
                                            ) + fadeIn(animationSpec = tween(220))) togetherWith
                                            (slideOutHorizontally(
                                                animationSpec = tween(220),
                                                targetOffsetX = { -direction * (it / 10) }
                                            ) + fadeOut(animationSpec = tween(120)))
                                            ).using(SizeTransform(clip = false))
                                },
                                label = "GateTabsContent",
                                contentAlignment = Alignment.TopStart,
                                content = { tabIndex ->
                                    when (tabIndex) {
                                        0 -> InputPDF(
                                            modifier = Modifier.fillMaxWidth(),
                                            onPicked = onPdfPicked,
                                            onResult = { fileText = it },
                                        )

                                        1 -> InputUrl(
                                            modifier = Modifier.fillMaxWidth(),
                                            value = url,
                                            onValueChange = {
                                                url = it
                                                urlState = UrlDownloadState.Idle
                                                urlText = ""
                                            },
                                            downloadState = urlState,
                                            onDownloadClick = {
                                                val input = url.trim()
                                                if (!input.startsWith("http://") && !input.startsWith("https://")) {
                                                    urlState = UrlDownloadState.Error("Please enter a valid http(s) URL.")
                                                    urlText = ""
                                                    return@InputUrl
                                                }

                                                urlState = UrlDownloadState.Loading
                                                urlText = ""
                                                scope.launch {
                                                    val (result, failure) = runCatching { downloadTextFromUrl(input) }
                                                        .let { it.getOrNull().orEmpty() to it.exceptionOrNull() }
                                                    if (result.isBlank()) {
                                                        urlState = UrlDownloadState.Error(
                                                            failure?.message ?: "Couldn’t download or read this link."
                                                        )
                                                    } else {
                                                        urlText = result
                                                        urlState = UrlDownloadState.Success("Downloaded successfully.")
                                                        onPdfPicked(
                                                            PdfHistoryItem(
                                                                name = "Link",
                                                                uri = input,
                                                                text = result,
                                                                addedAtEpochMs = nowEpochMs(),
                                                            )
                                                        )
                                                    }
                                                }
                                            },
                                        )

                                        else -> InputPaste(
                                            modifier = Modifier.fillMaxWidth(),
                                            value = pasteText,
                                            onValueChange = { pasteText = it },
                                        )
                                    }
                                }
                            )
                            Spacer(modifier = Modifier.height(height = 4.dp))
                            Button(
                                modifier = Modifier.fillMaxWidth(),
                                enabled = currentText.isNotBlank(),
                                onClick = {
                                    if (selectedTabIndex == 2) {
                                        onPdfPicked(
                                            PdfHistoryItem(
                                                name = "Paste",
                                                uri = null,
                                                text = pasteText,
                                                preview = pasteText.singleLinePreview(),
                                                addedAtEpochMs = nowEpochMs(),
                                            )
                                        )
                                    }
                                    onContinue(currentText)
                                },
                                shape = RoundedCornerShape(size = 8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.onBackground,
                                    contentColor = MaterialTheme.colorScheme.background
                                )
                            ) {
                                Text(
                                    text = "Let's go",
                                    style = MaterialTheme.typography.labelLarge,
                                )
                            }
                        }
                    )
                }
            )
        }
    )

}

@Composable
private fun InputUrl(
    modifier: Modifier = Modifier,
    value: String,
    onValueChange: (String) -> Unit,
    downloadState: UrlDownloadState,
    onDownloadClick: () -> Unit,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(space = 8.dp),
        content = {
            OutlinedTextField(
                value = value,
                singleLine = true,
                onValueChange = onValueChange,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(size = 8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.onBackground,
                    unfocusedBorderColor = MaterialTheme.colorScheme.onBackground,
                    focusedPlaceholderColor = Color.Gray.copy(alpha = .2F),
                    unfocusedPlaceholderColor = Color.Gray.copy(alpha = .2F),
                    cursorColor = MaterialTheme.colorScheme.onBackground
                ),
                placeholder = {
                    Text(
                        text = "Paste article or PDF link...",
                        style = MaterialTheme.typography.bodyMedium,
                        fontSize = 14.sp
                    )
                }
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Spacer(modifier = Modifier.weight(1f))
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onDownloadClick,
                    enabled = value.isNotBlank() && downloadState !is UrlDownloadState.Loading,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.onBackground,
                        contentColor = MaterialTheme.colorScheme.background,
                        disabledContainerColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f),
                        disabledContentColor = MaterialTheme.colorScheme.background.copy(alpha = 0.7f),
                    ),
                ) {
                    Text(
                        text = if (downloadState is UrlDownloadState.Loading) "Downloading…" else "Download",
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }

            when (downloadState) {
                UrlDownloadState.Idle -> Unit
                UrlDownloadState.Loading -> Text(
                    text = "Downloading…",
                    color = Color.Gray,
                    fontSize = 12.sp,
                    style = MaterialTheme.typography.bodySmall,
                )

                is UrlDownloadState.Success -> Text(
                    text = downloadState.message,
                    color = Color(0xFF2E7D32),
                    fontSize = 12.sp,
                    style = MaterialTheme.typography.bodySmall,
                )

                is UrlDownloadState.Error -> Text(
                    text = downloadState.message,
                    color = Color(0xFFB00020),
                    fontSize = 12.sp,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    )
}

private sealed interface UrlDownloadState {
    data object Idle : UrlDownloadState
    data object Loading : UrlDownloadState
    data class Success(val message: String) : UrlDownloadState
    data class Error(val message: String) : UrlDownloadState
}

private fun String.singleLinePreview(
    maxChars: Int = 80,
): String {
    val single = lineSequence().joinToString(" ") { it.trim() }.trim().replace(Regex("\\s+"), " ")
    if (single.length <= maxChars) return single
    return single.take(maxChars).trimEnd() + "…"
}

@Composable
private fun InputPaste(
    modifier: Modifier = Modifier,
    value: String,
    onValueChange: (String) -> Unit,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(space = 8.dp),
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            minLines = 6,
            maxLines = 6,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.onBackground,
                unfocusedBorderColor = MaterialTheme.colorScheme.onBackground,
                focusedPlaceholderColor = Color.Gray.copy(alpha = .2F),
                unfocusedPlaceholderColor = Color.Gray.copy(alpha = .2F),
            ),
            placeholder = {
                Text(
                    text = "Paste text…",
                    style = MaterialTheme.typography.bodyMedium,
                    fontSize = 14.sp
                )
            }
        )
    }
}