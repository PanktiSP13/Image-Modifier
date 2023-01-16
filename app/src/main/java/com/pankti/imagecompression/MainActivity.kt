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
        binding.btnClear.setOnClickListener { clearImage() }
        binding.btnCompressImage.setOnClickListener { manipulateImage() }
    }

    private fun manipulateImage() {
//        if (isValid()) {

        val url = binding.etUrl.text.toString()
        val quality = binding.etCompressionPercent.text.toString()

        Glide.with(this).load(url).into(binding.ivOriginalImage)

        // compress image
        ImageModifier.compress(this, url, if (quality.isNotEmpty()) quality.toInt() else 75) {
            if (it.isSuccessFullyModified) binding.ivCompressImage.setImageBitmap(it.compressedImage)
            else showToast(it.errorMessage ?: "")
        }

        // resize image
        ImageModifier.resize(this, url, 100) {
            if (it.isSuccessFullyModified) binding.ivIconImage.setImageBitmap(it.resizedImage)
            else showToast(it.errorMessage ?: "")
        }


        // resize and compress image
        ImageModifier.resizeAndCompress(this, url, if (quality.isNotEmpty()) quality.toInt() else 75, 150) {
            if (it.isSuccessFullyModified) binding.ivCompressWithResizeImage.setImageBitmap(it.resizedAndCompressedImage)
            else showToast(it.errorMessage ?: "")
        }

        // compress till 1 mb
        ImageModifier.compressTill1MB(this, url) {
            if (it.isSuccessFullyModified) binding.ivCompressTill1MBImage.setImageBitmap(it.compressedImage)
            else showToast(it.errorMessage ?: "")
        }

//        }
    }


    private fun clearImage() {
        binding.etUrl.setText("")
        binding.etCompressionPercent.setText("")
        Glide.with(this).load(R.color.white).into(binding.ivOriginalImage)
        Glide.with(this).load(R.color.white).into(binding.ivCompressImage)
        Glide.with(this).load(R.color.white).into(binding.ivCompressWithResizeImage)
        Glide.with(this).load(R.color.white).into(binding.ivCompressTill1MBImage)
        Glide.with(this).load(R.color.white).into(binding.ivIconImage)
    }

    private fun isValid(): Boolean {
        return if (binding.etUrl.text.toString().trim().isEmpty()) {
            showToast(getString(R.string.enter_url_validation))
            false
        } else if (binding.etCompressionPercent.text.toString().trim().isEmpty()) {
            showToast(getString(R.string.enter_quality))
            false
        } else if (binding.etCompressionPercent.text.toString().isNotEmpty() && ((binding.etCompressionPercent.text.toString()
                .toInt() > 100) || (binding.etCompressionPercent.text.toString().toInt() < 10))) {
            showToast(getString(R.string.compression_quality_validation))
            false
        } else true
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}