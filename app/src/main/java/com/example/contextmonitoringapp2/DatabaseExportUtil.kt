package com.example.contextmonitoringapp2

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import java.io.File
import java.io.FileInputStream
import java.io.IOException

object DatabaseExportUtil {

    fun exportDatabaseUsingMediaStore(context: Context, databasePath: String): Uri? {
        val sourceFile = File(databasePath)
        val contentResolver = context.contentResolver

        val contents = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, "HealthData_Exported.db")
            put(MediaStore.MediaColumns.MIME_TYPE, "application/octet-stream")
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOCUMENTS)
        }

        val uri = contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contents)

        uri?.let {
            try {
                contentResolver.openOutputStream(it)?.use { outputStream ->
                    FileInputStream(sourceFile).use { inputStream ->
                        val buff = ByteArray(1024)
                        var len: Int
                        while (inputStream.read(buff).also { len = it } > 0) {
                            outputStream.write(buff, 0, len)
                        }
                    }
                }
                Log.d("DB Export", "Database exported to: $uri")
                return uri
            } catch (e: IOException) {
                e.printStackTrace()
                Log.e("DB Export", "Export failed: ${e.message}")
            }
        }

        return null
    }
}
