package com.coditory.gradle.manifest.base

import java.io.File
import java.io.FileOutputStream
import java.io.PrintStream
import java.util.UUID
import kotlin.io.path.createTempDirectory

internal class SystemOutputCapturer private constructor(
    private val outFile: File,
    private val errFile: File,
    private val out: PrintStream,
    private val err: PrintStream,
) : AutoCloseable {
    @Synchronized
    fun readSystemOut(): String {
        return outFile.readText()
    }

    @Synchronized
    fun readSystemErr(): String {
        return errFile.readText()
    }

    @Synchronized
    fun restoreSystemOutput() {
        if (System.out !== out || System.err != err) {
            throw IllegalStateException("System output was overridden")
        }
        System.setOut(initialOut)
        System.setErr(initialErr)
        out.close()
        err.close()
    }

    @Synchronized
    override fun close() {
        restoreSystemOutput()
    }

    companion object {
        private val initialOut = System.out
        private val initialErr = System.err

        @Synchronized
        @Suppress("EXPERIMENTAL_API_USAGE_ERROR")
        fun captureSystemOutput(): SystemOutputCapturer {
            if (System.out !== initialOut || System.err != initialErr) {
                throw IllegalStateException("System output was already overridden")
            }
            val tmpDir = createTempDirectory().toFile()
            val outFile = createTmpFile(tmpDir, "out.txt")
            val errFile = createTmpFile(tmpDir, "err.txt")
            val outStream = PrintStream(FileOutputStream(outFile))
            val errStream = PrintStream(FileOutputStream(errFile))
            System.setOut(outStream)
            System.setErr(errStream)
            return SystemOutputCapturer(
                outFile = outFile,
                errFile = errFile,
                out = outStream,
                err = errStream,
            )
        }

        private fun createTmpFile(tmpDir: File, suffix: String): File {
            val uuid = UUID.randomUUID().toString()
            val outFile = tmpDir.resolve("$uuid-$suffix")
            outFile.createNewFile()
            return outFile
        }
    }
}
