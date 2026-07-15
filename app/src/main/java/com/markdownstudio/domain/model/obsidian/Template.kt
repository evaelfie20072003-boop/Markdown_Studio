package com.markdownstudio.domain.model.obsidian

data class Template(
    val uri: String,
    val name: String,
    val content: String = "",
    val isBuiltIn: Boolean = false
) {
    fun apply(variables: Map<String, String> = emptyMap()): String {
        var result = content
        variables.forEach { (key, value) ->
            result = result.replace("{{$key}}", value)
        }
        result = result.replace(
            "{{today}}",
            java.time.LocalDate.now().toString()
        )
        result = result.replace(
            "{{time}}",
            java.time.LocalTime.now().format(
                java.time.format.DateTimeFormatter.ofPattern("HH:mm")
            )
        )
        result = result.replace(
            "{{title}}",
            variables["title"] ?: "Untitled"
        )
        return result
    }
}
