package eu.basicairdata.graziano.gpslogger

import android.app.Activity.RESULT_OK
import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toFile
import androidx.fragment.app.Fragment
import eu.basicairdata.graziano.gpslogger.databinding.FragmentFuncTestBinding
import eu.basicairdata.graziano.gpslogger.manager.ImageManager
import java.io.ByteArrayOutputStream
import java.io.FileInputStream

/**
 * @author dspark( sha1028en )
 * @since 2023-08-11
 *
 * just TEST Func...
 */
class FuncTestFragment : Fragment() {
    private var mBinding: FragmentFuncTestBinding? = null

    private lateinit var requestCamera: ActivityResultLauncher<Intent>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?

    ): View {
        // Inflate the layout for this fragment
        this.mBinding = FragmentFuncTestBinding.inflate(inflater)

        this.requestCamera = registerForActivityResult(ActivityResultContracts.StartActivityForResult(), ActivityResultCallback<ActivityResult>() { result ->
            if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                val extras: Bundle? = result.getData()!!.getExtras()
                if (extras != null) {
                    val bitmap = extras["data"] as Bitmap?
                    ImageManager.saveImage(this.mBinding!!.root.context, "Parking", "Hiking/서울 강남구 대모산 무장애 나눔길", bitmap)
//                    this.saveImage("buffer", "Hiking/Image", bitmap)
                }
            }
        })

        this.mBinding!!.funcTestTextView.setOnClickListener {
//            this.requestTakePicture()
            this.requestCamera.launch(
                Intent(MediaStore.ACTION_IMAGE_CAPTURE).also {
                    it.putExtra(MediaStore.EXTRA_OUTPUT, ImageManager.getImagePath(this.mBinding!!.root.context, "Parking", "Hiking/서울 강남구 대모산 무장애 나눔길"))
                })
        }
        return this.mBinding!!.root
    }

    private fun getImagePath(fileName: String, filePath: String) : Uri? {
        if(this.mBinding == null) return null
        var imagePath: Uri

        val value = ContentValues()
        value.put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
        value.put(MediaStore.Images.Media.MIME_TYPE, "image/png")
        value.put(MediaStore.MediaColumns.RELATIVE_PATH, "DCIM/$filePath")

        val imageResolver = this.mBinding!!.root.context.contentResolver
        return imageResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, value)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        this.mBinding = null
    }

    private fun bitmapToByteArray(bitmap: Bitmap): ByteArray? {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
        return stream.toByteArray()
    }

    private fun saveImage(fileName: String, filePath: String, bitmap: Bitmap?) : Uri? {
        if(bitmap == null || this.mBinding == null) return null

        val imageAttr = ContentValues()
        imageAttr.put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
        imageAttr.put(MediaStore.Images.Media.MIME_TYPE, "image/*")
        imageAttr.put(MediaStore.MediaColumns.RELATIVE_PATH, "DCIM/$filePath")

        val imageResolver = this.mBinding!!.root.context.contentResolver
        val imageUri = imageResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, imageAttr) ?: return null
        val fileOutStream = imageResolver.openOutputStream(imageUri)

        if (fileOutStream != null) {
            val isRecord = bitmap.compress(Bitmap.CompressFormat.PNG, 75, fileOutStream)
            if(isRecord) {
                fileOutStream.flush()
            }
        }
        fileOutStream?.close() ?: return null
        return imageUri
    }

    private fun loadImage(fileName: String, filePath: String): Bitmap? {
        val image: Bitmap?
        val imageAttr = ContentValues()

        imageAttr.put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
        imageAttr.put(MediaStore.Images.Media.MIME_TYPE, "image/*")
        imageAttr.put(MediaStore.MediaColumns.RELATIVE_PATH, "DCIM/$filePath")

        val imageResolver = this.mBinding!!.root.context.contentResolver
        val imageUri = imageResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, imageAttr) ?: return null
        val bitmapOption = BitmapFactory.Options()
        bitmapOption.inPreferredConfig = Bitmap.Config.ARGB_8888
        image = BitmapFactory.decodeStream(FileInputStream(imageUri.toFile()), null, bitmapOption)

        return image
    }



//    private fun galleryAddPic() {
//        if(this.mBinding == null) return
//
//        Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE).also { mediaScanIntent ->
//            val f = File(currentPhotoPath)
//            mediaScanIntent.data = Uri.fromFile(f)
//            this.mBinding!!.root.context.sendBroadcast(mediaScanIntent)
//        }
//    }
//
//
//    private fun requestTakePicture() {
//        if(mBinding == null) return
//
//        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
//            takePictureIntent.resolveActivity(this.mBinding!!.root.context.packageManager)?.also {
//                val photoFile: File? = try {
//                    createImageFile()
//
//                } catch (ex: IOException) {
//                    null
//                }
//
//                photoFile?.also {
//                    val photoURI: Uri = FileProvider.getUriForFile(
//                        this.mBinding!!.root.context,
//                        "eu.basicairdata.graziano.gpslogger.fileprovider",
//                        it
//                    )
//                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
//                    startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO)
//                }
//            }
//        }
//    }
//
//    private fun createImageFile(): File {
//        // Create an image file name
//        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
////        val storageDir: File = this.mBinding!!.root.context.getExternalFilesDir(Environment.DIRECTORY_PICTURES) ?: File("EMPTY")
//        val storageDir: File = this.mBinding!!.root.context. ?: File("EMPTY")
//
//
//        return File.createTempFile(
//            "JPEG_${timeStamp}_",
//            ".jpg",
//            storageDir /* directory */
//
//        ).apply {
//            // Save a file: path for use with ACTION_VIEW intents
//            currentPhotoPath = absolutePath
//        }
//    }
}