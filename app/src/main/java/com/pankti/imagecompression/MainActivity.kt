package com.pankti.imagecompression

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import com.pankti.imagecompression.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        setClickListeners()
    }

    private fun setClickListeners() {
        binding.btnClear.setOnClickListener {
            clearImage()
        }

        binding.btnCompressImage.setOnClickListener {
            compressImage()
        }
    }

    private fun compressImage() {
        if (isValid()) {

            ImageUtils.instance()
                .withContext(this)
                .customWidth(150).imageUrl(binding.etUrl.text.toString())
                .compressionQuality(binding.etCompressionPercent.text.toString().toInt())
                .compressImage { _original, _compressed->
                    binding.ivOriginalImage.setImageBitmap(_original)
                    binding.ivCompressImage.setImageBitmap(_compressed)
                }
                .resizeImage {
                    binding.ivIconImage.setImageBitmap(it)
                }


        }
    }


    private fun clearImage() {
        binding.etUrl.setText("")
        binding.etCompressionPercent.setText("")
        binding.tvOriginalSize.text = ""
        binding.tvCompressedSize.text =""
        Glide.with(this).load(R.color.white).into(binding.ivOriginalImage)
        Glide.with(this).load(R.color.white).into(binding.ivCompressImage)
        Glide.with(this).load(R.color.white).into(binding.ivIconImage)
    }

    private fun isValid(): Boolean {
        return if (binding.etUrl.text.toString().trim().isEmpty()) {
            Toast.makeText(this, "Please enter url", Toast.LENGTH_LONG).show()
            false
        } else if (binding.etCompressionPercent.text.toString().trim().isEmpty()) {
            Toast.makeText(this, "Please enter compression percent", Toast.LENGTH_LONG).show()
            false
        } else if (binding.etCompressionPercent.text.toString().isNotEmpty() && ((binding.etCompressionPercent.text.toString()
                .toInt() > 100) || (binding.etCompressionPercent.text.toString().toInt() < 10))) {
            Toast.makeText(this, "Please enter compression percent between 10 to 100", Toast.LENGTH_LONG).show()
            false
        } else true
    }
}