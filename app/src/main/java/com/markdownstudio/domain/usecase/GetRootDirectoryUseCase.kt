package com.markdownstudio.domain.usecase

import com.markdownstudio.domain.repository.FileRepository
import javax.inject.Inject

class GetRootDirectoryUseCase @Inject constructor(
    private val repository: FileRepository
) {
    fun execute(): String? = repository.getRootDirectoryUri()
}
