package com.markdownstudio.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.markdownstudio.domain.model.search.SearchMatchType
import com.markdownstudio.domain.model.search.SearchQuery
import com.markdownstudio.domain.model.search.SearchResult
import com.markdownstudio.domain.repository.GlobalSearchRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SearchUiState(
    val query: String = "",
    val results: List<SearchResult> = emptyList(),
    val isLoading: Boolean = false,
    val hasSearched: Boolean = false,
    val searchTypes: Set<SearchMatchType> = SearchMatchType.entries.toSet(),
    val useRegex: Boolean = false,
    val caseSensitive: Boolean = false,
    val selectedResult: SearchResult? = null,
    val showReplace: Boolean = false,
    val replaceText: String = "",
    val replaceInProgress: Boolean = false,
    val replaceCount: Int = 0,
    val totalMatchCount: Int = 0,
    val error: String? = null
)

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val globalSearchRepository: GlobalSearchRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    fun onQueryChanged(query: String) {
        _uiState.value = _uiState.value.copy(query = query)
        searchJob?.cancel()

        if (query.isBlank()) {
            _uiState.value = _uiState.value.copy(
                results = emptyList(),
                hasSearched = false,
                isLoading = false,
                totalMatchCount = 0
            )
            return
        }

        searchJob = viewModelScope.launch {
            delay(300)
            performSearch()
        }
    }

    fun toggleSearchType(type: SearchMatchType) {
        val current = _uiState.value.searchTypes.toMutableSet()
        if (type in current && current.size > 1) {
            current.remove(type)
        } else {
            current.add(type)
        }
        _uiState.value = _uiState.value.copy(searchTypes = current)
        if (_uiState.value.query.isNotBlank()) {
            searchJob?.cancel()
            searchJob = viewModelScope.launch {
                delay(150)
                performSearch()
            }
        }
    }

    fun toggleRegex() {
        _uiState.value = _uiState.value.copy(
            useRegex = !_uiState.value.useRegex,
            error = null
        )
        if (_uiState.value.query.isNotBlank()) {
            searchJob?.cancel()
            searchJob = viewModelScope.launch {
                delay(150)
                performSearch()
            }
        }
    }

    fun toggleCaseSensitive() {
        _uiState.value = _uiState.value.copy(caseSensitive = !_uiState.value.caseSensitive)
        if (_uiState.value.query.isNotBlank()) {
            searchJob?.cancel()
            searchJob = viewModelScope.launch {
                delay(150)
                performSearch()
            }
        }
    }

    fun selectResult(result: SearchResult) {
        _uiState.value = _uiState.value.copy(selectedResult = result)
    }

    fun toggleReplace() {
        _uiState.value = _uiState.value.copy(
            showReplace = !_uiState.value.showReplace,
            replaceText = "",
            replaceCount = 0
        )
    }

    fun updateReplaceText(text: String) {
        _uiState.value = _uiState.value.copy(replaceText = text)
    }

    fun replaceSingle(result: SearchResult) {
        viewModelScope.launch {
            globalSearchRepository.replaceInFile(
                result.uri,
                _uiState.value.query,
                _uiState.value.replaceText,
                _uiState.value.useRegex
            ).onSuccess { count ->
                _uiState.value = _uiState.value.copy(replaceCount = count)
                performSearch()
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(error = "Replace failed: ${e.message}")
            }
        }
    }

    fun replaceAll() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(replaceInProgress = true)
            globalSearchRepository.replaceAll(
                _uiState.value.results,
                _uiState.value.query,
                _uiState.value.replaceText,
                _uiState.value.useRegex
            ).onSuccess { count ->
                _uiState.value = _uiState.value.copy(
                    replaceCount = count,
                    replaceInProgress = false
                )
                performSearch()
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(
                    replaceInProgress = false,
                    error = "Replace all failed: ${e.message}"
                )
            }
        }
    }

    fun clearSearch() {
        _uiState.value = SearchUiState()
        searchJob?.cancel()
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    private suspend fun performSearch() {
        val q = _uiState.value.query
        if (q.isBlank()) return

        _uiState.value = _uiState.value.copy(isLoading = true, hasSearched = true)

        val searchQuery = SearchQuery(
            text = q,
            matchTypes = _uiState.value.searchTypes,
            useRegex = _uiState.value.useRegex,
            caseSensitive = _uiState.value.caseSensitive
        )

        try {
            val results = globalSearchRepository.search(searchQuery)
            val totalMatches = results.sumOf { it.matchCount }
            _uiState.value = _uiState.value.copy(
                results = results,
                isLoading = false,
                totalMatchCount = totalMatches,
                error = null
            )
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                error = "Search failed: ${e.message}"
            )
        }
    }
}
