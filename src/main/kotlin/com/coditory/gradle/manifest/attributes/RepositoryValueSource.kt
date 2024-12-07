package com.coditory.gradle.manifest.attributes

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.lib.Constants.HEAD
import org.gradle.api.provider.Property
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import java.io.File

data class Repostiory(
    val url: String? = null,
    val branch: String? = null,
    val commitMessage: String? = null,
    val commitHash: String? = null,
    val commitAuthorName: String? = null,
    val commitAuthorEmail: String? = null,
    val commitEpochSeconds: Long? = null,
) {
    companion object {
        private val EMPTY = Repostiory()

        fun empty() = EMPTY
    }
}

abstract class RepositoryValueSource : ValueSource<Repostiory, RepositoryValueSource.Params> {
    interface Params : ValueSourceParameters {
        val projectDir: Property<File>
    }

    override fun obtain(): Repostiory {
        return try {
            val repository = Git.open(parameters.projectDir.get()).repository
            val head = repository.parseCommit(repository.resolve(HEAD))
            return Repostiory(
                url = repository.config.getString("remote", "origin", "url"),
                branch = repository.fullBranch,
                commitMessage = head.shortMessage,
                commitHash = head.name(),
                commitAuthorName = head.authorIdent.name.trim(),
                commitAuthorEmail = head.authorIdent.emailAddress.trim(),
                commitEpochSeconds = head.authorIdent.whenAsInstant.epochSecond,
            )
        } catch (_: Throwable) {
            // passing logger via valueSource parameters does not work
            Repostiory.empty()
        }
    }
}
