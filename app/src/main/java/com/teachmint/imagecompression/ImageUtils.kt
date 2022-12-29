package com.teachmint.imagecompression

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Bitmap.CompressFormat
import android.graphics.drawable.Drawable
import android.util.Log
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.teachmint.imagecompression.ImageUtils.ImageUtilsConstant.DEFAULT_COMPRESSION_QUALITY
import com.teachmint.imagecompression.ImageUtils.ImageUtilsConstant.DEFAULT_RESIZE_MINIMUM_WIDTH
import com.teachmint.imagecompression.ImageUtils.ImageUtilsConstant.TAG
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
    private var compressedBitmap: Bitmap? = null
    private var resizedBitmap: Bitmap? = null

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

    fun compressImage(onCompress: (Bitmap) -> Unit = {}) : ImageUtils {
        try {
            val fileName = "compressed-image-${compressionQuality}-" + imageUrl.substring(imageUrl.lastIndexOf('/') + 1, imageUrl.length)

            context.let {
                Glide.with(it).asBitmap().load(imageUrl).into(object : CustomTarget<Bitmap>() {
                    override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {

                        Log.i(TAG, "Original width: ${resource.width} " +
                                "height: ${resource.height} size : ${sizeOfBitmapInKB(resource)} kb")

                        val compressedBitmap = saveAndCompressBitmap(fileName,resource)
                        Log.i(TAG, "onCompressImage width: ${compressedBitmap.width} " +
                                "height: ${compressedBitmap.height} size : ${sizeOfBitmapInKB(compressedBitmap)} kb")

                        onCompress(compressedBitmap)

                    }

                    override fun onLoadCleared(placeholder: Drawable?) {}
                })
            }


        } catch (e:Exception){
            Log.e(TAG, "compressImage: "+e.message.toString())
            throw e
        }
        return this
    }

    fun resizeImage(onResize: (Bitmap) -> Unit = {}) : ImageUtils {
        try {

            context.let {
                Glide.with(it).asBitmap().load(imageUrl).into(object : CustomTarget<Bitmap>() {
                    override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                        val newHeight = ((customWidth * resource.height) / resource.width)
                        val scaled = Bitmap.createScaledBitmap(resource, customWidth, newHeight, true)
                        Log.i(TAG, "OnResizeImage width : ${scaled.width} height : ${scaled.height}")
                        Log.i(TAG, "onResourceReady: ")
                        onResize(scaled)
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {}
                })
            }
        }
        catch (e:java.lang.Exception){
            Log.e(TAG, "resizeImage: "+e.message.toString() )
            throw e
        }
        return this
    }

    fun resizeAndCompressImage(onResizeAndCompressImage: (Bitmap) -> Unit = {})  {
        try {
            val fileName = "compressed-icon-${compressionQuality}-" + imageUrl.substring(imageUrl.lastIndexOf('/') + 1, imageUrl.length)

            context.let {
                Glide.with(it).asBitmap().load(imageUrl).into(object : CustomTarget<Bitmap>() {
                    override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {

                        val newHeight = ((customWidth * resource.height) / resource.width)
                        val scaled = Bitmap.createScaledBitmap(resource, customWidth, newHeight, true)
                        Log.i(TAG, "OnResizeImage width : ${scaled.width} height : ${scaled.height} size : ${sizeOfBitmapInKB(scaled)} kb")

                        val compressedBitmap = saveAndCompressBitmap(fileName,scaled)
                        Log.i(TAG, "onResizeAndCompressImage width: ${compressedBitmap.width} " +
                                "height: ${compressedBitmap.height} size : ${sizeOfBitmapInKB(compressedBitmap)} kb")

                        onResizeAndCompressImage(compressedBitmap)

                    }

                    override fun onLoadCleared(placeholder: Drawable?) {}
                })
            }
        }
        catch (e:java.lang.Exception){
            Log.e(TAG, "resizeImage: "+e.message.toString() )
            throw e
        }
    }


    private fun saveAndCompressBitmap(fileName: String, bitmap: Bitmap): Bitmap {
        try {
            val file = File(context.filesDir,fileName)
            Log.e(TAG, "saveAndCompressBitmap: file: ${file.absolutePath}")
            val fileOutputStream = FileOutputStream(file)
            bitmap.config = Bitmap.Config.ARGB_8888
            bitmap.compress(file.compressFormat(), compressionQuality, fileOutputStream)
            fileOutputStream.flush()
            fileOutputStream.close()
        } catch (e: Exception) {
            Log.e(TAG, "saveAndCompressBitmap: "+e.message.toString())
        }
        return bitmap
    }

    private fun File.compressFormat() = when (extension.lowercase(Locale.getDefault())) {
        "png" -> CompressFormat.PNG
        "webp" -> CompressFormat.WEBP
        "jpeg" , "jpg" -> CompressFormat.JPEG
        else -> CompressFormat.PNG
    }

    private fun sizeOfBitmapInKB(data: Bitmap?): Int {
        return data?.let { it.allocationByteCount / 1000 } ?: 0
    }
}