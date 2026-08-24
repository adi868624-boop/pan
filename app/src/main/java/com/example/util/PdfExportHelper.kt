package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.model.PanApplicationEntity
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfExportHelper {

    fun generateAndShareDossier(
        context: Context,
        app: PanApplicationEntity,
        onComplete: (File?) -> Unit
    ) {
        try {
            val pdfDocument = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 Size (595 x 842 pt)
            val page = pdfDocument.startPage(pageInfo)
            val canvas: Canvas = page.canvas

            val paint = Paint()
            val titlePaint = Paint().apply {
                color = Color.rgb(15, 48, 87) // Deep Navy
                textSize = 18f
                isFakeBoldText = true
            }
            val subTitlePaint = Paint().apply {
                color = Color.rgb(100, 116, 139)
                textSize = 10f
            }
            val headerPaint = Paint().apply {
                color = Color.rgb(230, 81, 0) // Saffron Accent
                textSize = 12f
                isFakeBoldText = true
            }
            val textPaint = Paint().apply {
                color = Color.rgb(30, 41, 59)
                textSize = 10f
            }
            val boldTextPaint = Paint().apply {
                color = Color.rgb(15, 23, 42)
                textSize = 10f
                isFakeBoldText = true
            }

            // Top Header Banner
            paint.color = Color.rgb(244, 247, 251)
            canvas.drawRect(0f, 0f, 595f, 75f, paint)

            paint.color = Color.rgb(15, 48, 87)
            paint.strokeWidth = 3f
            canvas.drawLine(0f, 75f, 595f, 75f, paint)

            canvas.drawText("PAN CARD APPLICATION DOSSIER", 20f, 32f, titlePaint)
            val dateFormatted = SimpleDateFormat("dd-MMM-yyyy hh:mm a", Locale.getDefault()).format(Date(app.createdAt))
            canvas.drawText("Ref ID: ${app.applicationRef}  |  Submitted: $dateFormatted", 20f, 50f, subTitlePaint)
            canvas.drawText("Retailer: ${app.retailerName} (${app.retailerShopName} - ${app.retailerMobile})", 20f, 65f, subTitlePaint)

            // Status Badge
            val statusColor = when (app.status) {
                "COMPLETED" -> Color.rgb(46, 125, 50)
                "REJECTED" -> Color.rgb(198, 40, 40)
                "ACK_GENERATED" -> Color.rgb(2, 136, 209)
                else -> Color.rgb(245, 124, 0)
            }
            paint.color = statusColor
            val statusRect = RectF(460f, 18f, 575f, 42f)
            canvas.drawRoundRect(statusRect, 8f, 8f, paint)
            
            val statusTextPaint = Paint().apply {
                color = Color.WHITE
                textSize = 9f
                isFakeBoldText = true
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText(app.status, 517.5f, 33f, statusTextPaint)

            // Section 1: Applicant Details Card
            paint.color = Color.rgb(250, 250, 250)
            paint.style = Paint.Style.FILL
            val detailsRect = RectF(20f, 85f, 430f, 235f)
            canvas.drawRoundRect(detailsRect, 6f, 6f, paint)
            
            paint.color = Color.rgb(226, 232, 240)
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 1f
            canvas.drawRoundRect(detailsRect, 6f, 6f, paint)

            canvas.drawText("APPLICANT PARTICULARS", 32f, 105f, headerPaint)

            var y = 125f
            drawLabelValue(canvas, "Customer Name:", app.customerName, 32f, y, boldTextPaint, textPaint)
            y += 18f
            drawLabelValue(canvas, "Father's Name:", app.fatherName, 32f, y, boldTextPaint, textPaint)
            y += 18f
            drawLabelValue(canvas, "Mother's Name:", app.motherName.ifEmpty { "N/A" }, 32f, y, boldTextPaint, textPaint)
            y += 18f
            drawLabelValue(canvas, "Date of Birth:", app.dob.ifEmpty { "N/A" }, 32f, y, boldTextPaint, textPaint)
            y += 18f
            drawLabelValue(canvas, "Mobile Number:", app.mobile, 32f, y, boldTextPaint, textPaint)
            y += 18f
            drawLabelValue(canvas, "Email Address:", app.email.ifEmpty { "N/A" }, 32f, y, boldTextPaint, textPaint)
            y += 18f
            drawLabelValue(canvas, "Application Type:", "${app.applicationType} (${if (app.physicalCardRequired) "Physical Card" else "e-PAN Only"})", 32f, y, boldTextPaint, textPaint)

            // Passport Photo Frame (Top Right)
            val photoRect = RectF(450f, 85f, 575f, 235f)
            paint.color = Color.rgb(241, 245, 249)
            paint.style = Paint.Style.FILL
            canvas.drawRoundRect(photoRect, 6f, 6f, paint)
            paint.color = Color.rgb(203, 213, 225)
            paint.style = Paint.Style.STROKE
            canvas.drawRoundRect(photoRect, 6f, 6f, paint)

            if (app.passportPhotoPath.isNotEmpty()) {
                val passportBm = loadSampledBitmap(app.passportPhotoPath, 200, 200)
                if (passportBm != null) {
                    canvas.drawBitmap(passportBm, null, Rect(455, 90, 570, 205), null)
                }
            }
            val photoLabelPaint = Paint().apply {
                color = Color.rgb(100, 116, 139)
                textSize = 8f
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText("Passport Photo", 512.5f, 222f, photoLabelPaint)

            // Section 2: Documents Grid
            val docHeaderY = 255f
            canvas.drawText("ATTACHED DOCUMENTS & VERIFICATION", 20f, docHeaderY, headerPaint)

            // Row 1: Aadhaar Front & Aadhaar Back
            drawDocumentBox(canvas, "Aadhaar Card (Front)", app.aadharFrontPath, 20f, 265f, 270f, 150f)
            drawDocumentBox(canvas, "Aadhaar Card (Back)", app.aadharBackPath, 305f, 265f, 270f, 150f)

            // Row 2: Voter ID Front & Voter ID Back (or PAN Photo)
            val voterLabel1 = if (app.voterFrontPath.isNotEmpty()) "Voter ID (Front)" else "Old PAN Card"
            val voterPath1 = if (app.voterFrontPath.isNotEmpty()) app.voterFrontPath else app.panPhotoPath
            val voterLabel2 = if (app.voterBackPath.isNotEmpty()) "Voter ID (Back)" else "Supporting ID"
            val voterPath2 = app.voterBackPath

            drawDocumentBox(canvas, voterLabel1, voterPath1, 20f, 430f, 270f, 150f)
            drawDocumentBox(canvas, voterLabel2, voterPath2, 305f, 430f, 270f, 150f)

            // Row 3: Signature & Old PAN (or Ack details)
            drawDocumentBox(canvas, "Applicant Signature / Thumb", app.signaturePath, 20f, 595f, 270f, 130f)
            
            // Admin Note / Status Box
            val noteRect = RectF(305f, 595f, 575f, 725f)
            paint.color = Color.rgb(248, 250, 252)
            paint.style = Paint.Style.FILL
            canvas.drawRoundRect(noteRect, 6f, 6f, paint)
            paint.color = Color.rgb(203, 213, 225)
            paint.style = Paint.Style.STROKE
            canvas.drawRoundRect(noteRect, 6f, 6f, paint)

            canvas.drawText("ADMIN DISPOSITION", 318f, 615f, headerPaint)
            drawLabelValue(canvas, "Ack No:", app.ackNumber.ifEmpty { "Pending Generation" }, 318f, 638f, boldTextPaint, textPaint)
            drawLabelValue(canvas, "PAN Number:", app.panNumber.ifEmpty { "Pending Allotment" }, 318f, 658f, boldTextPaint, textPaint)
            drawLabelValue(canvas, "Remarks:", app.adminRemarks.ifEmpty { "No special remarks" }, 318f, 678f, boldTextPaint, textPaint)
            
            // Footer Note
            val footerPaint = Paint().apply {
                color = Color.rgb(148, 163, 184)
                textSize = 8f
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText("PAN Mitra Data Collection System • Generated on $dateFormatted", 297.5f, 825f, footerPaint)

            pdfDocument.finishPage(page)

            // Save PDF to cache or files folder
            val exportsDir = File(context.cacheDir, "pan_exports")
            if (!exportsDir.exists()) exportsDir.mkdirs()
            val pdfFile = File(exportsDir, "PAN_Dossier_${app.customerName.replace(" ", "_")}_${app.applicationRef}.pdf")
            
            FileOutputStream(pdfFile).use { out ->
                pdfDocument.writeTo(out)
            }
            pdfDocument.close()

            onComplete(pdfFile)
        } catch (e: Exception) {
            e.printStackTrace()
            onComplete(null)
        }
    }

    private fun drawLabelValue(canvas: Canvas, label: String, value: String, x: Float, y: Float, labelPaint: Paint, valuePaint: Paint) {
        canvas.drawText(label, x, y, labelPaint)
        canvas.drawText(value, x + 95f, y, valuePaint)
    }

    private fun drawDocumentBox(
        canvas: Canvas,
        label: String,
        filePath: String,
        x: Float,
        y: Float,
        width: Float,
        height: Float
    ) {
        val rect = RectF(x, y, x + width, y + height)
        val bgPaint = Paint().apply {
            color = Color.rgb(248, 250, 252)
            style = Paint.Style.FILL
        }
        canvas.drawRoundRect(rect, 6f, 6f, bgPaint)

        val borderPaint = Paint().apply {
            color = Color.rgb(203, 213, 225)
            style = Paint.Style.STROKE
            strokeWidth = 1f
        }
        canvas.drawRoundRect(rect, 6f, 6f, borderPaint)

        if (filePath.isNotEmpty() && File(filePath).exists()) {
            val bm = loadSampledBitmap(filePath, 350, 220)
            if (bm != null) {
                val imgRect = Rect(
                    (x + 5).toInt(),
                    (y + 5).toInt(),
                    (x + width - 5).toInt(),
                    (y + height - 20).toInt()
                )
                canvas.drawBitmap(bm, null, imgRect, null)
            }
        } else {
            val placeholderPaint = Paint().apply {
                color = Color.rgb(148, 163, 184)
                textSize = 9f
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText("Not Provided", x + (width / 2f), y + (height / 2f), placeholderPaint)
        }

        val labelBgPaint = Paint().apply {
            color = Color.rgb(241, 245, 249)
            style = Paint.Style.FILL
        }
        val labelRect = RectF(x, y + height - 18f, x + width, y + height)
        canvas.drawRect(labelRect, labelBgPaint)

        val labelPaint = Paint().apply {
            color = Color.rgb(71, 85, 105)
            textSize = 8.5f
            isFakeBoldText = true
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(label, x + (width / 2f), y + height - 6f, labelPaint)
    }

    private fun loadSampledBitmap(filePath: String, reqWidth: Int, reqHeight: Int): Bitmap? {
        return try {
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeFile(filePath, options)

            var inSampleSize = 1
            if (options.outHeight > reqHeight || options.outWidth > reqWidth) {
                val halfHeight: Int = options.outHeight / 2
                val halfWidth: Int = options.outWidth / 2
                while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                    inSampleSize *= 2
                }
            }

            val decodeOptions = BitmapFactory.Options().apply {
                inSampleSize = inSampleSize
            }
            BitmapFactory.decodeFile(filePath, decodeOptions)
        } catch (e: Exception) {
            null
        }
    }

    fun sharePdfFile(context: Context, pdfFile: File, title: String) {
        try {
            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                pdfFile
            )
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, title)
                putExtra(Intent.EXTRA_TEXT, "PAN Card Application Dossier for $title")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "Share PAN Dossier PDF"))
        } catch (e: Exception) {
            Toast.makeText(context, "Could not share PDF: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }
}
