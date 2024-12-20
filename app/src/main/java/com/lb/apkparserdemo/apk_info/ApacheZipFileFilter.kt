package com.lb.apkparserdemo.apk_info

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry
import java.io.Closeable
import java.util.*

/**Note seems to perform worse than the built in one of Android. Use only if the built in one has some issues*/
class ApacheZipFileFilter(private val zipFile: org.apache.commons.compress.archivers.zip.ZipFile) :
    AbstractZipFilter(), Closeable {
    private var entries: Enumeration<out ZipArchiveEntry>? = null

    @Suppress("MemberVisibilityCanBePrivate")
    var currentEntry: ZipArchiveEntry? = null

    init {
        try {
            this.entries = zipFile.entries
        } catch (e: Exception) {
            close()
        }
    }

    override fun getByteArrayForEntries(
        mandatoryEntriesNames: Set<String>,
        extraEntriesNames: Set<String>?
    ): HashMap<String, ByteArray>? {
        try {
            val totalItemsCount = mandatoryEntriesNames.size + (extraEntriesNames?.size ?: 0)
            val result = HashMap<String, ByteArray>(totalItemsCount)
            for (name in mandatoryEntriesNames) {
                val entry: ZipArchiveEntry = zipFile.getEntry(name) ?: return null
                result[name] = zipFile.getInputStream(entry).readBytes()
            }
            if (extraEntriesNames != null)
                for (name in extraEntriesNames) {
                    val entry: ZipArchiveEntry = zipFile.getEntry(name) ?: continue
                    result[name] = zipFile.getInputStream(entry).readBytes()
                }
            return result
        } catch (e: Exception) {
            return null
        }
    }


    override fun getNextEntryName(): String? {
        entries.let {
            if (it == null)
                return null
            try {
                it.nextElement().let { zipEntry: ZipArchiveEntry? ->
                    if (zipEntry == null) {
//                        close()
                        currentEntry = null
                        entries = null
                        return null
                    }
                    currentEntry = zipEntry
                    return zipEntry.name
                }
            } catch (e: Exception) {
                currentEntry = null
                entries = null
//                close()
                return null
            }
        }
    }

    override fun getBytesFromCurrentEntry(): ByteArray? {
        currentEntry.let { zipEntry: ZipArchiveEntry? ->
            if (zipEntry == null)
                return null
            return try {
                zipFile.getInputStream(zipEntry).readBytes()
            } catch (e: Exception) {
                close()
                null
            }
        }
    }

    override fun close() {
        entries = null
        currentEntry = null
        try {
            zipFile.close()
        } catch (_: Exception) {
        }
    }

}
