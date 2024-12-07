package com.coditory.gradle.manifest.base

import org.gradle.api.Project
import org.gradle.internal.impldep.org.eclipse.jgit.api.Git
import org.gradle.internal.impldep.org.eclipse.jgit.lib.Constants
import org.gradle.internal.impldep.org.eclipse.jgit.lib.PersonIdent
import org.gradle.internal.impldep.org.eclipse.jgit.revwalk.RevCommit
import org.gradle.internal.impldep.org.eclipse.jgit.transport.RemoteConfig
import org.gradle.internal.impldep.org.eclipse.jgit.transport.URIish
import java.time.ZonedDateTime
import java.util.Date
import java.util.TimeZone
import java.util.UUID

class TestRepository private constructor(
    private val project: Project,
    private val git: Git,
) {

    fun withRemote(
        name: String = REMOTE_NAME,
        url: String = REMOTE_URL,
    ): TestRepository {
        val config = git.repository.config
        val remoteConfig = RemoteConfig(config, name)
        remoteConfig.addURI(URIish(url))
        remoteConfig.update(config)
        config.save()
        return this
    }

    fun withCommit(
        message: String = COMMIT_MESSAGE,
        authorName: String = AUTHOR_NAME,
        authorEmail: String = AUTHOR_EMAIL,
        authorTime: ZonedDateTime = AUTHOR_TIME,
    ): TestRepository {
        val uuid = UUID.randomUUID().toString()
        val filePath = project.rootDir.resolve("samples/$uuid")
        filePath.parentFile.mkdirs()
        filePath.createNewFile()
        filePath.writeText(uuid)

        val personId = PersonIdent(authorName, authorEmail, Date.from(authorTime.toInstant()), TimeZone.getTimeZone(authorTime.zone))
        git.add().addFilepattern(filePath.absolutePath).call()
        git.commit()
            .setSign(false)
            .setMessage(message)
            .setAuthor(personId)
            .call()
        return this
    }

    fun getLastCommit(): RevCommit {
        val id = git.repository.resolve(Constants.HEAD)
        return git.repository.parseCommit(id)
    }

    companion object {
        const val REMOTE_NAME = "origin"
        const val REMOTE_URL = "git@github.com:coditory/gradle-manifest-plugin.git"
        const val COMMIT_MESSAGE = "Very important commit"
        const val AUTHOR_NAME = "John Doe"
        const val AUTHOR_EMAIL = "john.doe@acme.com"
        val AUTHOR_TIME: ZonedDateTime = ZonedDateTime.parse("2020-03-24T20:46:03.242102+01:00")

        fun repository(project: Project): TestRepository {
            val git = Git.init()
                .setDirectory(project.rootDir)
                .setInitialBranch("main")
                .call()
            return TestRepository(project, git)
        }
    }
}
