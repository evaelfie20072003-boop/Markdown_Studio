package com.markdownstudio.ui.search

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FindReplace
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.outlined.AutoFixHigh
import androidx.compose.material.icons.outlined.FindReplace
import androidx.compose.material.icons.outlined.TextFields
import kotlin.text.Regex
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.markdownstudio.domain.model.search.SearchMatchType
import com.markdownstudio.domain.model.search.SearchResult

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onOpenEditor: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Column(modifier = modifier.fillMaxSize()) {
        TopAppBar(title = { Text("Search") })

        SearchBar(
            query = state.query, useRegex = state.useRegex,
            caseSensitive = state.caseSensitive, showReplace = state.showReplace,
            replaceText = state.replaceText,
            onQueryChanged = viewModel::onQueryChanged,
            onToggleRegex = viewModel::toggleRegex,
            onToggleCaseSensitive = viewModel::toggleCaseSensitive,
            onToggleReplace = viewModel::toggleReplace,
            onReplaceTextChanged = viewModel::updateReplaceText,
            onClear = viewModel::clearSearch
        )

        SearchTypeFilters(
            selectedTypes = state.searchTypes,
            onToggleType = viewModel::toggleSearchType
        )

        if (state.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    modifier = Modifier.semantics { contentDescription = "Searching" }
                )
            }
        } else if (state.hasSearched && state.results.isEmpty()) {
            EmptySearchResult(query = state.query, useRegex = state.useRegex, modifier = Modifier.fillMaxSize())
        } else if (state.results.isNotEmpty()) {
            ResultSummary(count = state.results.size, totalMatches = state.totalMatchCount)

            AnimatedVisibility(
                visible = state.showReplace,
                enter = slideInVertically { -it } + fadeIn(),
                exit = slideOutVertically { -it } + fadeOut()
            ) {
                ReplaceBar(
                    replaceText = state.replaceText,
                    isInProgress = state.replaceInProgress,
                    onReplaceTextChanged = viewModel::updateReplaceText,
                    onReplaceAll = viewModel::replaceAll
                )
            }

            LazyColumn(Modifier.fillMaxSize()) {
                items(state.results, key = { "${it.uri}-${it.matchType}-${it.lineNumber}" }) { result ->
                    SearchResultItem(
                        result = result,
                        isSelected = result == state.selectedResult,
                        onClick = { viewModel.selectResult(result); onOpenEditor(result.uri) },
                        onReplace = if (state.showReplace) ({ viewModel.replaceSingle(result) }) else null
                    )
                }
            }
        } else if (!state.hasSearched) {
            SearchPrompt(modifier = Modifier.fillMaxSize())
        }

        state.error?.let {
            Text(
                text = it, color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun SearchBar(
    query: String, useRegex: Boolean, caseSensitive: Boolean, showReplace: Boolean, replaceText: String,
    onQueryChanged: (String) -> Unit, onToggleRegex: () -> Unit,
    onToggleCaseSensitive: () -> Unit, onToggleReplace: () -> Unit,
    onReplaceTextChanged: (String) -> Unit, onClear: () -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        OutlinedTextField(
            value = query, onValueChange = onQueryChanged,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Search files, content, tags...") },
            singleLine = true,
            trailingIcon = {
                Row {
                    IconButton(onClick = onToggleCaseSensitive) {
                        Icon(
                            Icons.Default.TextFields,
                            contentDescription = if (caseSensitive) "Case sensitive on" else "Case sensitive off",
                            tint = if (caseSensitive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(onClick = onToggleRegex) {
                        Icon(
                            Icons.Default.FindReplace,
                            contentDescription = if (useRegex) "Regex on" else "Regex off",
                            tint = if (useRegex) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(onClick = onToggleReplace) {
                        Icon(
                            Icons.Default.FindReplace,
                            contentDescription = if (showReplace) "Replace mode on" else "Replace mode off",
                            tint = if (showReplace) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    if (query.isNotEmpty()) {
                        IconButton(onClick = onClear) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear search",
                                modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SearchTypeFilters(
    selectedTypes: Set<SearchMatchType>,
    onToggleType: (SearchMatchType) -> Unit
) {
    FlowRow(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        SearchMatchType.entries.forEach { type ->
            FilterChip(
                selected = type in selectedTypes,
                onClick = { onToggleType(type) },
                label = { Text(type.displayName, style = MaterialTheme.typography.labelSmall) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    }
}

@Composable
private fun ResultSummary(count: Int, totalMatches: Int) {
    Text(
        text = "$count files matched ($totalMatches matches)",
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
    )
}

@Composable
private fun ReplaceBar(
    replaceText: String, isInProgress: Boolean,
    onReplaceTextChanged: (String) -> Unit, onReplaceAll: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = replaceText, onValueChange = onReplaceTextChanged,
            modifier = Modifier.weight(1f),
            placeholder = { Text("Replace with...") },
            singleLine = true, enabled = !isInProgress
        )
        Spacer(Modifier.width(8.dp))
        Button(
            onClick = onReplaceAll,
            enabled = replaceText.isNotEmpty() && !isInProgress
        ) {
            if (isInProgress) {
                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
            } else {
                Text("Replace All")
            }
        }
    }
}

@Composable
private fun SearchResultItem(
    result: SearchResult, isSelected: Boolean,
    onClick: () -> Unit, onReplace: (() -> Unit)?
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .background(if (isSelected) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surface)
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .semantics {
                contentDescription = "Search result: ${result.fileName}, type: ${result.matchType.displayName}"
            }
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            MatchTypeLabel(result.matchType)
            Spacer(Modifier.width(8.dp))
            Text(
                text = result.fileName,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1, overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            if (onReplace != null) {
                IconButton(onClick = onReplace, modifier = Modifier.size(28.dp)) {
                    Icon(
                        Icons.Default.AutoFixHigh, contentDescription = "Replace in this file",
                        modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        when (result.matchType) {
            SearchMatchType.CONTENT -> {
                result.snippet?.let { snippet ->
                    Text(
                        text = "Line ${result.lineNumber ?: ""}: $snippet",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2, overflow = TextOverflow.Ellipsis,
                        fontFamily = FontFamily.Monospace, fontSize = 12.sp,
                        modifier = Modifier.padding(top = 2.dp, start = 4.dp)
                    )
                }
            }
            SearchMatchType.TAG -> Text(
                text = "#${result.contextLine ?: ""}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 2.dp, start = 4.dp)
            )
            SearchMatchType.WIKI_LINK -> Text(
                text = "→ [[${result.contextLine ?: ""}]]",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.padding(top = 2.dp, start = 4.dp)
            )
            else -> {}
        }
    }
    HorizontalDivider()
}

@Composable
private fun MatchTypeLabel(type: SearchMatchType) {
    val color = when (type) {
        SearchMatchType.FILE_NAME -> MaterialTheme.colorScheme.primary
        SearchMatchType.FOLDER_NAME -> MaterialTheme.colorScheme.secondary
        SearchMatchType.CONTENT -> MaterialTheme.colorScheme.tertiary
        SearchMatchType.TAG -> MaterialTheme.colorScheme.error
        SearchMatchType.WIKI_LINK -> MaterialTheme.colorScheme.outline
    }
    AssistChip(
        onClick = {},
        label = {
            Text(
                when (type) {
                    SearchMatchType.FILE_NAME -> "F"
                    SearchMatchType.FOLDER_NAME -> "D"
                    SearchMatchType.CONTENT -> "C"
                    SearchMatchType.TAG -> "T"
                    SearchMatchType.WIKI_LINK -> "W"
                },
                style = MaterialTheme.typography.labelSmall
            )
        },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = color.copy(alpha = 0.15f), labelColor = color
        ),
        border = null,
        modifier = Modifier.size(width = 32.dp, height = 24.dp)
    )
}

@Composable
private fun EmptySearchResult(
    query: String, useRegex: Boolean, modifier: Modifier = Modifier
) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "No results found",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = if (useRegex) "No matches for regex: $query" else "No matches for \"$query\"",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
private fun SearchPrompt(modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Search across your markdown files",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Search file names, content, tags, and wiki links",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}
