package com.pankti.imagecompression

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Bitmap.CompressFormat
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.util.Log
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.pankti.imagecompression.ImageUtils.ImageUtilsConstant.DEFAULT_COMPRESSION_QUALITY
import com.pankti.imagecompression.ImageUtils.ImageUtilsConstant.DEFAULT_RESIZE_MINIMUM_WIDTH
import com.pankti.imagecompression.ImageUtils.ImageUtilsConstant.TAG
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.*


class ImageUtils private constructor() {

    companion object {

        fun instance(): ImageUtils {
            return ImageUtils()
        }
    }

    object ImageUtilsConstant {
        const val TAG = "ImageUtils"

        const val DEFAULT_COMPRESSION_QUALITY = 10
        const val DEFAULT_RESIZE_MINIMUM_WIDTH = 50
    }

    private lateinit var context: Context
    private var imageUrl = ""
    private var compressionQuality = DEFAULT_COMPRESSION_QUALITY
    private var customWidth: Int = DEFAULT_RESIZE_MINIMUM_WIDTH

    fun withContext(context: Context): ImageUtils {
        this.context = context
        return this@ImageUtils
    }

    fun imageUrl(url: String): ImageUtils {
        this.imageUrl = url
        return this@ImageUtils
    }

    fun compressionQuality(compressionQualityPercent: Int): ImageUtils {
        this.compressionQuality = compressionQualityPercent
        return this@ImageUtils
    }

    fun customWidth(width: Int): ImageUtils {
        this.customWidth = width
        return this@ImageUtils
    }

    fun compressImage(onCompress: (Bitmap, Bitmap) -> Unit = { _originalBitmap, _compressedBitmap -> }): ImageUtils {
        try {
            val fileName = "compressed-image-${compressionQuality}-" + imageUrl.substring(imageUrl.lastIndexOf('/') + 1, imageUrl.length)

            context.let {
                Glide.with(it).asBitmap().load(imageUrl).into(object : CustomTarget<Bitmap>() {
                    override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                        val compressedBitmap = saveAndCompressBitmap(fileName, resource)
                        Log.i(TAG, "OnCompressed : "+sizeOfBitmapInMB(resource) + " \t\t" +sizeOfBitmapInMB(compressedBitmap))
                        onCompress(resource, compressedBitmap)
                    }
                    override fun onLoadCleared(placeholder: Drawable?) {}
                })
            }
        } catch (e: Exception) {
            Log.e(TAG, "compressImage: " + e.message.toString())
            throw e
        }
        return this
    }

    fun resizeImage(onResize: (Bitmap) -> Unit = {}): ImageUtils {
        try {
            context.let {
                Glide.with(it).asBitmap().load(imageUrl).into(object : CustomTarget<Bitmap>() {
                    override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                        val newHeight = ((customWidth * resource.height) / resource.width)
                        val scaled = Bitmap.createScaledBitmap(resource, customWidth, newHeight, true)
                        Log.i(TAG, "OnResizeImage width : ${scaled.width} height : ${scaled.height}")
                        onResize(scaled)
                    }
                    override fun onLoadCleared(placeholder: Drawable?) {}
                })
            }
        } catch (e: java.lang.Exception) {
            Log.e(TAG, "resizeImage: " + e.message.toString())
            throw e
        }
        return this
    }

    fun resizeAndCompressImage(onResizeAndCompressImage: (Bitmap) -> Unit = {}) {
        try {
            context.let {
                Glide.with(it).asBitmap().load(imageUrl).into(object : CustomTarget<Bitmap>() {
                    override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {

                        val newHeight = ((customWidth * resource.height) / resource.width)
                        val scaled = Bitmap.createScaledBitmap(resource, customWidth, newHeight, true)

                        val fileName = "compressed-icon-${compressionQuality}-" + imageUrl.substring(imageUrl.lastIndexOf('/') + 1, imageUrl.length)
                        val compressedBitmap = saveAndCompressBitmap(fileName, scaled)

                        onResizeAndCompressImage(compressedBitmap)
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {}
                })
            }
        } catch (e: java.lang.Exception) {
            Log.e(TAG, "resizeImage: " + e.message.toString())
            throw e
        }
    }


    private fun saveAndCompressBitmap(fileName: String, bitmap: Bitmap): Bitmap {
        val compressedImageFile = saveBitmapToFile(fileName, bitmap)
        return BitmapFactory.decodeFile(compressedImageFile.absolutePath) ?: bitmap
    }

    private fun saveBitmapToFile(fileName: String, finalBitmap: Bitmap): File {
        val myDir = File(context.filesDir, "CompressedImage")
        myDir.mkdirs()
        val file = File(myDir, fileName)

        try {
            val out = FileOutputStream(file)
            finalBitmap.config = Bitmap.Config.ARGB_8888
            finalBitmap.compress(file.compressFormat(), compressionQuality, out)
            out.close()
        } catch (e: Exception) {
            Log.e(TAG, "saveBitmapToFile: " + e.message)
        }
        return file
    }

    private fun File.compressFormat() = when (extension.lowercase(Locale.getDefault())) {
        "png" -> CompressFormat.PNG
        "webp" -> CompressFormat.WEBP
        "jpeg", "jpg" -> CompressFormat.JPEG
        else -> CompressFormat.PNG
    }

    private fun sizeOfBitmapInMB(bitmap: Bitmap): String {
        val stream = ByteArrayOutputStream()
        bitmap.compress(CompressFormat.JPEG, 100, stream)
        val imageInByte = stream.toByteArray()
        val length = imageInByte.size.toLong()
        return ("Byte : $length  \n converted : " + (length / ( 1024 * 1024)).toString() + " mb")
    }
}