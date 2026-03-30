package com.devnight.pagecurleffect

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.createBitmap
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.devnight.pagecurleffect.databinding.ActivityMainBinding
import java.io.IOException

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private var pdfRenderer: PdfRenderer? = null
    private var parcelFileDescriptor: ParcelFileDescriptor? = null
    private var currentPageIndex = 0

    private val selectPdfLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            result.data?.data?.let { uri ->
                openPdf(uri)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.btnSelectPdf.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "application/pdf"
            }
            selectPdfLauncher.launch(intent)
        }

        binding.glSurface.onPageFlipped = { isNext ->
            pdfRenderer?.let { renderer ->
                if (isNext && currentPageIndex < renderer.pageCount - 1) {
                    currentPageIndex++
                    updateRendererPages()
                } else if (!isNext && currentPageIndex > 0) {
                    currentPageIndex--
                    updateRendererPages()
                }
            }
        }
    }

    private fun openPdf(uri: Uri) {
        try {
            parcelFileDescriptor = contentResolver.openFileDescriptor(uri, "r")
            parcelFileDescriptor?.let {
                pdfRenderer = PdfRenderer(it)
                currentPageIndex = 0
                updateRendererPages()
            }
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(this, "Error opening PDF", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateRendererPages() {
        val current = renderPage(currentPageIndex)
        val next = if (currentPageIndex + 1 < (pdfRenderer?.pageCount ?: 0)) {
            renderPage(currentPageIndex + 1)
        } else {
            null
        }

        if (current != null) {
            binding.glSurface.setPages(current, next)
        }
    }

    private fun renderPage(index: Int): Bitmap? {
        return pdfRenderer?.let { renderer ->
            if (index < 0 || index >= renderer.pageCount) return null
            val page = renderer.openPage(index)
            val bitmap = createBitmap(page.width, page.height)
            val canvas = Canvas(bitmap)
            canvas.drawColor(Color.WHITE)
            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
            page.close()
            bitmap
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        pdfRenderer?.close()
        parcelFileDescriptor?.close()
    }
}