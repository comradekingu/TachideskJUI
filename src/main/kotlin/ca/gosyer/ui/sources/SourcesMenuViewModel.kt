/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.ui.sources

import ca.gosyer.data.catalog.CatalogPreferences
import ca.gosyer.data.models.Source
import ca.gosyer.data.server.ServerPreferences
import ca.gosyer.data.server.interactions.SourceInteractionHandler
import ca.gosyer.ui.base.vm.ViewModel
import ca.gosyer.util.lang.throwIfCancellation
import ca.gosyer.util.system.CKLogger
import com.github.zsoltk.compose.savedinstancestate.Bundle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

class SourcesMenuViewModel @Inject constructor(
    private val bundle: Bundle,
    private val sourceHandler: SourceInteractionHandler,
    serverPreferences: ServerPreferences,
    catalogPreferences: CatalogPreferences
) : ViewModel() {
    val serverUrl = serverPreferences.serverUrl().stateIn(scope)

    private val _languages = catalogPreferences.languages().asStateFlow()
    val languages = _languages.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    private var installedSources = emptyList<Source>()

    private val _sources = MutableStateFlow(emptyList<Source>())
    val sources = _sources.asStateFlow()

    private val _sourceTabs = MutableStateFlow<List<Source?>>(listOf(null))
    val sourceTabs = _sourceTabs.asStateFlow()

    private val _selectedSourceTab = MutableStateFlow<Source?>(null)
    val selectedSourceTab = _selectedSourceTab.asStateFlow()

    private val _sourceSearchEnabled = MutableStateFlow(false)
    val sourceSearchEnabled = _sourceSearchEnabled.asStateFlow()

    private val _sourceSearchQuery = MutableStateFlow<String?>(null)
    val sourceSearchQuery = _sourceSearchQuery.asStateFlow()

    private var searchSource: ((String?) -> Unit)? = null

    init {
        _sourceTabs.drop(1)
            .onEach { sources ->
                bundle.putLongArray(SOURCE_TABS_KEY, sources.mapNotNull { it?.id }.toLongArray())
            }
            .launchIn(scope)

        _selectedSourceTab.drop(1)
            .onEach {
                if (it != null) {
                    bundle.putLong(SELECTED_SOURCE_TAB, it.id)
                } else {
                    bundle.remove(SELECTED_SOURCE_TAB)
                }
            }
            .launchIn(scope)

        getSources()
    }

    private fun setSources(langs: Set<String>) {
        _sources.value = installedSources.filter { it.lang in langs }
    }

    private fun getSources() {
        scope.launch {
            try {
                installedSources = sourceHandler.getSourceList()
                setSources(_languages.value)
                info { _sources.value }
            } catch (e: Exception) {
                e.throwIfCancellation()
            } finally {
                val sourceTabs = bundle.getLongArray(SOURCE_TABS_KEY)
                if (sourceTabs != null) {
                    _sourceTabs.value = listOf(null) + sourceTabs.toList()
                        .mapNotNull { sourceId ->
                            _sources.value.find { it.id == sourceId }
                        }
                    _selectedSourceTab.value = bundle.getLong(SELECTED_SOURCE_TAB, -1).let { id ->
                        if (id != -1L) {
                            _sources.value.find { it.id == id }
                        } else null
                    }
                }
                _isLoading.value = false
            }
        }
    }

    fun selectTab(source: Source?) {
        _selectedSourceTab.value = source
    }

    fun addTab(source: Source) {
        if (source !in _sourceTabs.value) {
            _sourceTabs.value += source
        }
        selectTab(source)
    }

    fun closeTab(source: Source) {
        _sourceTabs.value -= source
        if (selectedSourceTab.value?.id == source.id) {
            _selectedSourceTab.value = null
        }
        bundle.remove(source.id.toString())
    }

    fun setSearch(block: (String?) -> Unit) {
        searchSource = block
    }

    fun enableSearch(enabled: Boolean) {
        _sourceSearchEnabled.value = enabled
    }

    fun search(query: String) {
        _sourceSearchQuery.value = query.takeUnless { it.isBlank() }
    }

    fun submitSearch() {
        searchSource?.invoke(sourceSearchQuery.value)
    }

    fun getSourceLanguages(): Set<String> {
        return installedSources.map { it.lang }.toSet()
    }

    fun setEnabledLanguages(langs: Set<String>) {
        info { langs }
        _languages.value = langs
        setSources(langs)
    }

    private companion object : CKLogger({}) {
        const val SOURCE_TABS_KEY = "source_tabs"
        const val SELECTED_SOURCE_TAB = "selected_tab"
    }
}
