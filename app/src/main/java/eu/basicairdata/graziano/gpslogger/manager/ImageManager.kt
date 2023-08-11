package eu.basicairdata.graziano.gpslogger.manager

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.MediaStore
import androidx.core.net.toFile
import java.io.ByteArrayOutputStream
import java.io.FileInputStream
import java.io.IOException
import java.lang.NullPointerException
import java.lang.ref.WeakReference

/**
 * @since 2023-08-11
 * @author dspark( sha1028en )
 *
 * to Handling Image, File.
 */
class ImageManager {
    companion object {

        /**
         * convert Bitmap to ByteArray
         *
         * @param bitmap to convert Bitmap Image
         * @param compressPercent 1 ~ 100 ( higher is better quality )
         *
         * @return converted bitmap ByteArray?
         * @throws NullPointerException wrong param state
         * @throws IllegalArgumentException wrong param value
         */
        @Throws(NullPointerException::class, IllegalArgumentException::class)
        fun bitmapToByteArray(bitmap: Bitmap, compressPercent: Int): ByteArray? {
            if(compressPercent < 1 || compressPercent > 100) throw IllegalArgumentException("ImageManager.bitmapToByteArray() wrong param \"compressPercent\"")
            val stream = ByteArrayOutputStream()

            bitmap.compress(Bitmap.CompressFormat.PNG, compressPercent, stream)
            return stream.toByteArray()
        }

        /**
         * get File PATH where image save
         *
         * @param context Android Context
         * @param fileName to save Image File Name
         * @param filePath to save Image File PATH
         *
         * @return to save Image Uri or Null ( failed )
         * @throws NullPointerException wrong param state
         * @throws IllegalArgumentException wrong param value
         */
        @Throws(NullPointerException::class, IllegalArgumentException::class)
        fun getImagePath(context: Context, fileName: String, filePath: String) : Uri? {
            var imagePath: Uri
            val localContext = WeakReference(context)
            val value = ContentValues()

            value.put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
            value.put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            value.put(MediaStore.MediaColumns.RELATIVE_PATH, "DCIM/$filePath")

            val imageResolver = localContext.get()!!.contentResolver
            return imageResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, value)
        }

        /**
         * save Image to File
         *
         * @param context Android Context
         * @param fileName to save Image File Name
         * @param filePath to save Image File PATH
         *
         * @return save Image Uri or Null ( failed! )
         * @throws NullPointerException wrong param state
         * @throws IllegalArgumentException wrong param value
         * @throws IOException fail to save Image File
         */
        @Throws(NullPointerException::class, IllegalArgumentException::class, IOException::class)
        fun saveImage(context: Context, fileName: String, filePath: String, bitmap: Bitmap?) : Uri? {
            if(bitmap == null) return null
            val localContext = WeakReference(context)

            val imageAttr = ContentValues()
            imageAttr.put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
            imageAttr.put(MediaStore.Images.Media.MIME_TYPE, "image/*")
            imageAttr.put(MediaStore.MediaColumns.RELATIVE_PATH, "DCIM/$filePath")

            val imageResolver = localContext.get()!!.contentResolver
            val imageUri = imageResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, imageAttr) ?: return null
            val fileOutStream = imageResolver.openOutputStream(imageUri)

            if (fileOutStream != null) {
                val isRecord = bitmap.compress(Bitmap.CompressFormat.PNG, 75, fileOutStream)
                if(isRecord) {
                    fileOutStream.flush()
                }
            }
            localContext.clear()
            fileOutStream?.close() ?: return null
            return imageUri
        }

        /**
         * load Image from File
         *
         * @param context Android Context
         * @param fileName to load Image File Name
         * @param filePath to load Image File PATH
         *
         * @return loaded Image Uri or Null ( failed! )
         * @throws NullPointerException wrong param state
         * @throws IllegalArgumentException wrong param value
         * @throws IOException fail to load Image File
         */
        @Throws(NullPointerException::class, IllegalArgumentException::class)
        fun loadImage(context: Context, fileName: String, filePath: String): Bitmap? {
            val image: Bitmap?
            val imageAttr = ContentValues()
            val localContext = WeakReference(context)

            imageAttr.put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
            imageAttr.put(MediaStore.Images.Media.MIME_TYPE, "image/*")
            imageAttr.put(MediaStore.MediaColumns.RELATIVE_PATH, "DCIM/$filePath")

            val imageResolver = localContext.get()!!.contentResolver
            val imageUri = imageResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, imageAttr) ?: return null
            val bitmapOption = BitmapFactory.Options()
            bitmapOption.inPreferredConfig = Bitmap.Config.ARGB_8888
            image = BitmapFactory.decodeStream(FileInputStream(imageUri.toFile()), null, bitmapOption)

            localContext.clear()
            return image
        }
    }
}