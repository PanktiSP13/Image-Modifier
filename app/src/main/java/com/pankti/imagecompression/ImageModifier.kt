package com.pankti.imagecompression

import android.content.Context
import java.io.File

object ImageModifier {

    /**
     * image modify using image url
     * */
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

    /**
     * image modification using image file
     * */
    fun compress(context: Context, imageFile : File, quality: Int, OnCompressCallback : (ImageUtils.ModifiedImageData) -> Unit) {
        ImageUtils.init().withContext(context).compressionQuality(quality)
            .imageFile(imageFile).compressImage { OnCompressCallback(it)}
    }

    fun resize(context: Context,file: File,newWidth:Int, OnResizeCallback : (ImageUtils.ModifiedImageData) -> Unit){
        ImageUtils.init().withContext(context).imageFile(file).customWidth(newWidth).resizeImage { OnResizeCallback(it) }
    }

    fun resizeAndCompress(context: Context,file: File,quality: Int,
                          newWidth: Int, OnImageModifiedCallback : (ImageUtils.ModifiedImageData) -> Unit){

        ImageUtils.init().withContext(context).imageFile(file)
            .compressionQuality(quality).customWidth(newWidth)
            .resizeAndCompressImage { OnImageModifiedCallback(it)}
    }


    fun compressTill1MB(context: Context,file: File,OnCompressCallback : (ImageUtils.ModifiedImageData) -> Unit){
        ImageUtils.init().withContext(context).imageFile(file).compressImgWithCertainSize { OnCompressCallback(it)}
    }

}