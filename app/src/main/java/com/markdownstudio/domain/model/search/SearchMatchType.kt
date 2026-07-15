package com.markdownstudio.domain.model.search

enum class SearchMatchType(val displayName: String) {
    FILE_NAME("File Name"),
    FOLDER_NAME("Folder Name"),
    CONTENT("Content"),
    TAG("Tags"),
    WIKI_LINK("Wiki Links")
}
