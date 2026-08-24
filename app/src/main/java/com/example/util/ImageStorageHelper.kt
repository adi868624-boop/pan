package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.core.content.FileProvider
import android.media.ExifInterface
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ImageStorageHelper {

    fun createTempImageUri(context: Context): Pair<Uri, String> {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = File(context.cacheDir, "camera_captures")
        if (!storageDir.exists()) {
            storageDir.mkdirs()
        }
        val imageFile = File.createTempFile("CAPTURED_${timeStamp}_", ".jpg", storageDir)
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            imageFile
        )
        return Pair(uri, imageFile.absolutePath)
    }

    fun saveBitmapToFile(context: Context, bitmap: Bitmap, folderName: String, fileNamePrefix: String): String {
        val docsDir = File(context.filesDir, "pan_records/$folderName")
        if (!docsDir.exists()) {
            docsDir.mkdirs()
        }
        val destFile = File(docsDir, "${fileNamePrefix}_${System.currentTimeMillis()}.jpg")
        FileOutputStream(destFile).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
            out.flush()
        }
        return destFile.absolutePath
    }

    fun copyUriToAppStorage(context: Context, sourceUri: Uri, folderName: String, fileNamePrefix: String): String {
        val docsDir = File(context.filesDir, "pan_records/$folderName")
        if (!docsDir.exists()) {
            docsDir.mkdirs()
        }
        val destFile = File(docsDir, "${fileNamePrefix}_${System.currentTimeMillis()}.jpg")
        
        context.contentResolver.openInputStream(sourceUri)?.use { input ->
            val bitmap = BitmapFactory.decodeStream(input)
            if (bitmap != null) {
                val orientedBitmap = fixOrientation(context, sourceUri, bitmap)
                FileOutputStream(destFile).use { out ->
                    orientedBitmap.compress(Bitmap.CompressFormat.JPEG, 88, out)
                    out.flush()
                }
            } else {
                // fallback copy direct stream
                context.contentResolver.openInputStream(sourceUri)?.use { rawIn ->
                    FileOutputStream(destFile).use { out ->
                        rawIn.copyTo(out)
                    }
                }
            }
        }
        return destFile.absolutePath
    }

    private fun fixOrientation(context: Context, uri: Uri, bitmap: Bitmap): Bitmap {
        return try {
            val input: InputStream? = context.contentResolver.openInputStream(uri)
            if (input != null) {
                val exif = ExifInterface(input)
                val orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL
                )
                input.close()
                when (orientation) {
                    ExifInterface.ORIENTATION_ROTATE_90 -> rotateBitmap(bitmap, 90f)
                    ExifInterface.ORIENTATION_ROTATE_180 -> rotateBitmap(bitmap, 180f)
                    ExifInterface.ORIENTATION_ROTATE_270 -> rotateBitmap(bitmap, 270f)
                    else -> bitmap
                }
            } else {
                bitmap
            }
        } catch (e: Exception) {
            bitmap
        }
    }

    private fun rotateBitmap(bitmap: Bitmap, degrees: Float): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(degrees)
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    fun getFileUri(context: Context, filePath: String): Uri? {
        val file = File(filePath)
        if (!file.exists()) return null
        return try {
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
        } catch (e: Exception) {
            null
        }
    }
}
