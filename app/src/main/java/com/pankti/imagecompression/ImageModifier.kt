package com.pankti.imagecompression

import android.content.Context

object ImageModifier {

    fun compress(context: Context,url : String,quality: Int, OnCompressCallback : (ImageUtils.ModifiedImageData) -> Unit) {
        ImageUtils.init().withContext(context).compressionQuality(quality)
            .imageUrl(url).compressImage { OnCompressCallback(it)}
    }

    fun resize(context: Context,url: String,newWidth:Int, OnResizeCallback : (ImageUtils.ModifiedImageData) -> Unit){
        ImageUtils.init().withContext(context).imageUrl(url).customWidth(newWidth).resizeImage { OnResizeCallback(it) }
    }

    fun resizeAndCompress(context: Context,url: String,quality: Int,
                          newWidth: Int, OnImageModifiedCallback : (ImageUtils.ModifiedImageData) -> Unit){
        ImageUtils.init().withContext(context).imageUrl(url)
            .compressionQuality(quality).customWidth(newWidth)
            .resizeAndCompressImage { OnImageModifiedCallback(it)}
    }

    fun compressTill1MB(context: Context,url: String,OnCompressCallback : (ImageUtils.ModifiedImageData) -> Unit){
        ImageUtils.init().withContext(context).imageUrl(url).compressImgWithCertainSize { OnCompressCallback(it)}
    }

}