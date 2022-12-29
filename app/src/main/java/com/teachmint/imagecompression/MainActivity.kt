package com.teachmint.imagecompression

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import com.teachmint.imagecompression.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var mBinding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        setClickListeners()
    }

    private fun setClickListeners() {
        mBinding.btnClear.setOnClickListener {
            clearImage()
        }

        mBinding.btnCompressImage.setOnClickListener {
            compressImage()
        }
    }

    private fun compressImage() {
        if (isValid()) {
            Glide.with(this).load(mBinding.etUrl.text.toString()).into(mBinding.ivOriginalImage)

            // image compress and resize
            ImageUtils.instance()
                .withContext(this)
                .customWidth(50).imageUrl(mBinding.etUrl.text.toString())
                .compressionQuality(mBinding.etCompressionPercent.text.toString().toInt())
                .compressImage { Glide.with(this).load(it).into(mBinding.ivCompressImage)}
                .resizeAndCompressImage{ mBinding.ivIconImage.setImageBitmap(it) }
        }
    }


    private fun clearImage() {
        mBinding.etUrl.setText("")
        mBinding.etCompressionPercent.setText("")
        Glide.with(this).load(R.color.white).into(mBinding.ivOriginalImage)
        Glide.with(this).load(R.color.white).into(mBinding.ivCompressImage)
        Glide.with(this).load(R.color.white).into(mBinding.ivIconImage)
    }

    private fun isValid(): Boolean {
        return if (mBinding.etUrl.text.toString().trim().isEmpty()) {
            Toast.makeText(this, "Please enter url", Toast.LENGTH_LONG).show()
            false
        } else if (mBinding.etCompressionPercent.text.toString().trim().isEmpty()) {
            Toast.makeText(this, "Please enter compression percent", Toast.LENGTH_LONG).show()
            false
        } else if (mBinding.etCompressionPercent.text.toString().isNotEmpty() && ((mBinding.etCompressionPercent.text.toString()
                .toInt() > 100) || (mBinding.etCompressionPercent.text.toString().toInt() < 10))) {
            Toast.makeText(this, "Please enter compression percent between 10 to 100", Toast.LENGTH_LONG).show()
            false
        } else true
    }

    
}