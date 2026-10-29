package com.teknosolvo.absensi.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Typeface
import android.text.TextPaint
import android.text.TextUtils
import androidx.exifinterface.media.ExifInterface
import java.io.File

/** Menulis watermark (nama, waktu, koordinat, alamat) ke foto absen. */
object PhotoStamper {
    private const val MAX_SIZE = 1600

    fun stamp(file: File, lines: List<String>) {
        val path = file.absolutePath
        val rotation = when (
            ExifInterface(path).getAttributeInt(
                ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL
            )
        ) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90f
            ExifInterface.ORIENTATION_ROTATE_180 -> 180f
            ExifInterface.ORIENTATION_ROTATE_270 -> 270f
            else -> 0f
        }

        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(path, bounds)
        var sample = 1
        while (maxOf(bounds.outWidth, bounds.outHeight) / sample > MAX_SIZE) sample *= 2
        val raw = BitmapFactory.decodeFile(path, BitmapFactory.Options().apply { inSampleSize = sample })
            ?: return

        val matrix = Matrix().apply { postRotate(rotation) }
        val bitmap = Bitmap.createBitmap(raw, 0, 0, raw.width, raw.height, matrix, true)
            .copy(Bitmap.Config.ARGB_8888, true)

        val canvas = Canvas(bitmap)
        val textSize = bitmap.width / 28f
        val paint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            this.textSize = textSize
            typeface = Typeface.DEFAULT_BOLD
        }
        val pad = textSize * 0.6f
        val lineHeight = textSize * 1.3f
        val boxTop = bitmap.height - (lines.size * lineHeight + pad * 2)
        canvas.drawRect(
            0f, boxTop, bitmap.width.toFloat(), bitmap.height.toFloat(),
            Paint().apply { color = Color.argb(150, 0, 0, 0) }
        )
        val maxWidth = bitmap.width - pad * 2
        lines.forEachIndexed { i, line ->
            val text = TextUtils.ellipsize(line, paint, maxWidth, TextUtils.TruncateAt.END).toString()
            canvas.drawText(text, pad, boxTop + pad + textSize + i * lineHeight, paint)
        }

        // Re-encode tanpa EXIF orientation karena piksel sudah diputar.
        file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.JPEG, 85, it) }
        raw.recycle()
        bitmap.recycle()
    }
}
