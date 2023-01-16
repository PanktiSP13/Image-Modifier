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
import com.pankti.imagecompression.ImageUtils.ImageUtilsConstant.DEFAULT_FILE_NAME
import com.pankti.imagecompression.ImageUtils.ImageUtilsConstant.DEFAULT_RESIZE_MINIMUM_WIDTH
import com.pankti.imagecompression.ImageUtils.ImageUtilsConstant.TAG
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.*


class ImageUtils private constructor() {

    companion object {

        fun init(): ImageUtils {
            return ImageUtils()
        }
    }

    object ImageUtilsConstant {
        const val TAG = "ImageUtils"

        const val DEFAULT_COMPRESSION_QUALITY = 100
        const val DEFAULT_RESIZE_MINIMUM_WIDTH = 50
        val DEFAULT_FILE_NAME = "IMG_" + System.currentTimeMillis() + ".jpg"
    }

    private var context: Context? = null
    private var imageUrl = ""
    private var compressionQuality = DEFAULT_COMPRESSION_QUALITY
    private var customWidth: Int = DEFAULT_RESIZE_MINIMUM_WIDTH

    private var imageFile = File(DEFAULT_FILE_NAME)
    private var isImageFileAvailable = false

    fun withContext(context: Context): ImageUtils {
        this.context = context
        return this
    }

    fun imageUrl(url: String): ImageUtils {
        this.imageUrl = url
        return this@ImageUtils
    }

    fun imageFile(imageFile: File): ImageUtils {
        this.imageFile = imageFile
        isImageFileAvailable = true
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

    fun compressImage(onCompress: (ModifiedImageData) -> Unit) {
        if (isValid { _error -> if (_error.isNotEmpty()) onCompress(ModifiedImageData(errorMessage = _error)) }) {
            compress { _data -> onCompress(_data) }
        }
    }


    fun resizeImage(onResize: (ModifiedImageData) -> Unit) {
        if (isValid { _error -> if (_error.isNotEmpty()) onResize(ModifiedImageData(errorMessage = _error)) }) {
            resize { _data -> onResize(_data) }
        }
    }

    fun resizeAndCompressImage(onImageModified: (ModifiedImageData) -> Unit) {
        if (isValid { _error -> if (_error.isNotEmpty()) onImageModified(ModifiedImageData(errorMessage = _error)) }) {
            resizeAndCompress { _data -> onImageModified(_data) }
        }
    }


    fun compressImgWithCertainSize(onCompress: (ModifiedImageData) -> Unit) {
        createBitmap {
            val compressedBitmap = compressImageToCertainSize(it)
            onCompress(ModifiedImageData(isSuccessFullyModified = true, originalImage = it, compressedImage = compressedBitmap))
        }
    }

    private fun compress(onCompress: (ModifiedImageData) -> Unit) {
        createBitmap {
            val compressedBitmap = saveAndCompressBitmap(fileName(), it)
            onCompress(ModifiedImageData(isSuccessFullyModified = true, originalImage = it, compressedImage = compressedBitmap))
        }
    }

    private fun resize(onResize: (ModifiedImageData) -> Unit) {
        createBitmap {
            val newHeight = ((customWidth * it.height) / it.width)
            val scaled = Bitmap.createScaledBitmap(it, customWidth, newHeight, true)
            Log.e(TAG, "OnResizeImage width : " + scaled.width + " height : " + scaled.height)
            onResize(ModifiedImageData(originalImage = it, resizedImage = scaled, isSuccessFullyModified = true))
        }
    }

    private fun resizeAndCompress(onModifiedImage: (ModifiedImageData) -> Unit = {}) {
        createBitmap {

            val newHeight = ((customWidth * it.height) / it.width)
            val scaled = Bitmap.createScaledBitmap(it, customWidth, newHeight, true)
            val compressedBitmap = saveAndCompressBitmap(fileName(), scaled)

            onModifiedImage(
                ModifiedImageData(
                    isSuccessFullyModified = true,
                    originalImage = it,
                    resizedImage = scaled,
                    compressedImage = compressedBitmap,
                    resizedAndCompressedImage = compressedBitmap))
        }
    }

    private fun compressImageToCertainSize(bitmap: Bitmap): Bitmap {
        val compressedImageFile = compressImgToCertainSize(bitmap)
        return BitmapFactory.decodeFile(compressedImageFile.absolutePath) ?: bitmap
    }

    private fun compressImgToCertainSize(bitmap: Bitmap): File {
        val file = File(context?.filesDir, "IMG_" + System.currentTimeMillis() + ".jpg")
        if (file.exists()) {
            file.delete()
        }

        val maxImgSize = 1024 * 1024 // 1 mb
        var streamLength = maxImgSize
        var compressQuality = 105
        val bmpStream = ByteArrayOutputStream()

        while (streamLength >= maxImgSize && compressQuality > 5) {
            try {
                bmpStream.flush() //to avoid out of memory error
                bmpStream.reset()
            } catch (e: IOException) {
                e.printStackTrace()
            }
            compressQuality -= 5
            bitmap.compress(CompressFormat.JPEG, compressQuality, bmpStream)
            val bmpPicByteArray: ByteArray = bmpStream.toByteArray()
            streamLength = bmpPicByteArray.size
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "Quality: $compressQuality")
                Log.d(TAG, "Size: " + (streamLength.toFloat() / 1024) + " kb")
            }
        }

        val fo: FileOutputStream
        try {
            fo = FileOutputStream(file)
            fo.write(bmpStream.toByteArray())
            fo.flush()
            fo.close()
        } catch (e: IOException) {
            Log.e(TAG, "compressImageToCertainSize: " + e.message)
        }

        return file
    }

    private fun saveAndCompressBitmap(fileName: String, bitmap: Bitmap): Bitmap {
        val compressedImageFile = saveBitmapToFile(fileName, bitmap)
        return BitmapFactory.decodeFile(compressedImageFile.absolutePath) ?: bitmap
    }

    private fun saveBitmapToFile(fileName: String, finalBitmap: Bitmap): File {
        val myDir = File(context?.filesDir, "CompressedImage")
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

    private fun isValid(result: (String) -> Unit = { _errorMessage -> }): Boolean {
        return if (context == null) {
            result("Context required")
            false
        } else if (!isImageFileAvailable && imageUrl.isEmpty()) {
            result("Image url not found")
            false
        } else if (compressionQuality > 100 || compressionQuality < 10) {
            result("Quality must be between 10 to 100")
            false
        } else {
            result("")
            true
        }
    }

    inner class ModifiedImageData(
        val originalImage: Bitmap? = null, val compressedImage: Bitmap? = null, val resizedImage: Bitmap? = null,
        val resizedAndCompressedImage: Bitmap? = null, val isSuccessFullyModified: Boolean = false, val errorMessage: String? = "")

    private fun createBitmap(onSuccess: (Bitmap) -> Unit = {}) {
        try {
            val image = if (isImageFileAvailable) imageFile else imageUrl
            Glide.with(context!!).asBitmap().load(image).into(object : CustomTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    onSuccess(resource)
                }

                override fun onLoadCleared(placeholder: Drawable?) {}
            })

        } catch (e: Exception) {
            Log.e(TAG, "createBitmap: " + e.message.toString())
        }
    }


    /**
     *  Check extension and create file name
     * */
    private fun fileName(): String {
        return when (if (isImageFileAvailable) { imageFile.extension } else { File(imageUrl).extension }) {
            "png" -> "IMG_" + System.currentTimeMillis() + ".png"
            "jpeg" -> "IMG_" + System.currentTimeMillis() + ".jpeg"
            else -> DEFAULT_FILE_NAME
        }
    }
}