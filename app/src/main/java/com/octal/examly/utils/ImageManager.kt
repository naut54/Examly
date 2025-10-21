package com.octal.examly.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.core.content.FileProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ImageManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val imagesDir: File by lazy {
        File(context.filesDir, "images").apply {
            if (!exists()) mkdirs()
        }
    }

    fun saveImage(sourceUri: Uri, fileName: String): Result<String> {
        return try {
            val inputStream = context.contentResolver.openInputStream(sourceUri)
                ?: return Result.failure(IOException("No se pudo abrir el archivo"))

            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream.close()

            val file = File(imagesDir, fileName)
            val outputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
            outputStream.flush()
            outputStream.close()

            Result.success(file.absolutePath)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun loadImage(filePath: String): Result<Bitmap> {
        return try {
            val file = File(filePath)
            if (!file.exists()) {
                return Result.failure(IOException("El archivo no existe"))
            }

            val bitmap = BitmapFactory.decodeFile(filePath)
            Result.success(bitmap)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun deleteImage(filePath: String): Result<Boolean> {
        return try {
            val file = File(filePath)
            val deleted = file.delete()
            Result.success(deleted)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getImageUri(filePath: String): Uri? {
        return try {
            val file = File(filePath)
            if (!file.exists()) return null

            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
        } catch (e: Exception) {
            null
        }
    }

    fun cleanOldImages(daysOld: Int = 30): Int {
        val cutoffTime = System.currentTimeMillis() - (daysOld * 24 * 60 * 60 * 1000L)
        var deletedCount = 0

        imagesDir.listFiles()?.forEach { file ->
            if (file.lastModified() < cutoffTime) {
                if (file.delete()) deletedCount++
            }
        }

        return deletedCount
    }

    fun getTotalImageSize(): Long {
        return imagesDir.walkTopDown()
            .filter { it.isFile }
            .map { it.length() }
            .sum()
    }
}