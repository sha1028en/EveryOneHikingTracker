package eu.basicairdata.graziano.gpslogger.management

import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.view.WindowManager
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream
import java.lang.NumberFormatException
import java.lang.ref.WeakReference
import java.util.LinkedList


/**
 * @since 2023-08-11
 * @author dspark( sha1028en )
 *
 * to Handling Image n File.
 */
class ImageManager {
    companion object {

        /**
         * get File PATH where created EMPTY tmp Files
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
        fun createTmpFile(context: Context, fileName: String, filePath: String) : Uri? {
            val localContext = WeakReference(context)
            val value = ContentValues()
//            val fileDir = localContext.get()!!.getExternalFilesDir(Environment.DIRECTORY_PICTURES)

            value.put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
            value.put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            value.put(MediaStore.MediaColumns.RELATIVE_PATH, "DCIM/$filePath")

            val imageResolver = localContext.get()!!.contentResolver
            return imageResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, value)
        }

        fun parseNameFromUri(context: Context, uri: Uri?): String {
            if(uri == null) return ""
            var fileName = ""
            val localContext = WeakReference(context)

            if(uri.scheme.equals("content")) {
                try {
                    val cursor = localContext.get()!!.contentResolver.query(uri, null, null, null, null)
                    cursor.use {
                        it!!.moveToFirst()
                        val index = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        if(index > 0) {
                            fileName = cursor!!.getString(index)
                        }
                    }

                } catch (e: NullPointerException) {
                    fileName = ""

                } catch (e: IndexOutOfBoundsException) {
                    fileName = ""

                } finally {
                    localContext.clear()
                }
            }
            return fileName
        }

        /**
         * Compress Image and Save to File
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
            imageAttr.put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            imageAttr.put(MediaStore.MediaColumns.RELATIVE_PATH, "DCIM/$filePath")

            val imageResolver = localContext.get()!!.contentResolver
            val imageUri = imageResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, imageAttr) ?: return null
            val fileOutStream = imageResolver.openOutputStream(imageUri)
            localContext.clear()
            var isRecord = false

            fileOutStream?.use {
                isRecord = bitmap.compress(Bitmap.CompressFormat.PNG, commonImageCompressPercent, fileOutStream)
                if(isRecord) {
                    fileOutStream.flush()
                }
            }
            return if(isRecord) imageUri else null
        }

        /**
         * load Images from File
         *
         * @param context Android Context
         * @param fileName to load Image File Name
         *
         * @return ImageList or FileNotFoundException
         * @throws NullPointerException wrong param state
         * @throws IllegalArgumentException wrong param value
         * @throws FileNotFoundException fail to Open File or Load Images
         */
        @Throws(NullPointerException::class, IllegalArgumentException::class, FileNotFoundException::class)
        fun loadImageList(context: Context, fileName: String, filePath: String): LinkedList<Bitmap?> {
            val localContext = WeakReference(context)
            var imageBuffer: Bitmap?
            val imageList = LinkedList<Bitmap?>()

            val projection = arrayOf(
                MediaStore.MediaColumns._ID,
                MediaStore.MediaColumns.TITLE)
            val path = "${Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).path}/$filePath"

            val where = "${MediaStore.Images.Media.DATA} LIKE ? AND ${MediaStore.MediaColumns.TITLE} LIKE ?"
            val whereArg = arrayOf("$path%", "$fileName%")
            val orderBy = "${MediaStore.Images.Media.TITLE} ASC"

            val imageCursor = localContext.get()!!.contentResolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection,
                where,
                whereArg,
                orderBy)

            imageCursor.use {
                while (it?.moveToNext() == true) {
                    val index = it.getColumnIndex(MediaStore.Images.ImageColumns._ID)
                    imageBuffer = if(index > -1) {
                        val contentUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, it.getLong(index))
                        val imageStream = localContext.get()!!.contentResolver.openInputStream(contentUri)
                        BitmapFactory.decodeStream(imageStream)

                    } else {
                        null
                    }
                    imageList.add(imageBuffer)
                }
            }
            localContext.clear()

            if(imageList.size < 1) throw FileNotFoundException("cant load File from $path")
            return imageList
        }

        /**
         * load image which image file exists
         *
         * @param context Android Context
         * @param fileName to load Image File Name
         * @param filePath to load Image File where is
         * @param fileSuffix to load Image File Suffix ( png, jpg... )
         *
         * @throws FileNotFoundException cant Find File or Fail to load Image
         * @throws IllegalArgumentException wrong Param value
         */
        @Throws(IllegalArgumentException::class, FileNotFoundException::class, NumberFormatException::class)
        fun loadImage(context: Context, fileName: String, filePath: String, fileSuffix: String): Bitmap {
            var image: Bitmap? = null
            val path = "${Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).path}/$filePath/$fileName.$fileSuffix"
            val verifyFile = File(path)

            if(!verifyFile.exists()) throw FileNotFoundException("can not find $fileName where $path")
            val localContext = WeakReference(context)
            val imageResolver = localContext.get()!!.contentResolver

            val selection = MediaStore.Images.Media.DATA
            val args = arrayOf(verifyFile.absolutePath)
            val projetion = arrayOf(MediaStore.Images.Media._ID, MediaStore.Images.Media.DATA)
            val orderBy = "${MediaStore.Images.Media.TITLE} ASC"
            val queryMeidaCursor = imageResolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projetion, "$selection = ?", args, orderBy)
            var imageStream: InputStream

            queryMeidaCursor.use {
                try {
                    if (it?.moveToNext() == true) {
                        val mediaIdIndex = it.getColumnIndex(MediaStore.Images.Media._ID)
                        val mediaId = it.getString(mediaIdIndex).toLong()

                        image = if (mediaId > -1) {
                            val contentUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, mediaId)
                            imageStream = localContext.get()!!.contentResolver.openInputStream(contentUri)!!
                            imageStream.use {
                                BitmapFactory.decodeStream(imageStream)
                            }

                        } else {
                            null
                        }
                    }

                } catch (e: IOException) {
                    e.printStackTrace()
                    image = null

                } finally {
                    localContext.clear()
                }
            }
            if(image == null) throw FileNotFoundException("can not load $fileName where $path")
            return image as Bitmap
        }

        /**
         * remove latest Image if File exists
         *
         * @param context Android Context
         * @param fileName to remove Image File Name
         *
         * @throws IllegalArgumentException wrong params
         */
        @Throws(IllegalArgumentException::class)
        fun removeLastImage(context: Context, fileName: String): Boolean {
            var hasRemove = false
            val localContext = WeakReference(context)
            val projection = arrayOf(
                MediaStore.MediaColumns._ID,
                MediaStore.MediaColumns.TITLE)

            val where = "${MediaStore.MediaColumns.TITLE} LIKE ?"
            val whereArg = arrayOf("$fileName%")
            val orderBy = "${MediaStore.Images.Media._ID} ASC"

            val imageCursor = localContext.get()!!.contentResolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection,
                where,
                whereArg,
                orderBy)

            try {
                imageCursor.use {
                    it!!.moveToLast()
                    val index = it.getColumnIndex(MediaStore.Images.ImageColumns._ID)
                    if (index > -1) {
                        val contentUri = ContentUris.withAppendedId(
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                            it.getLong(index)
                        )
                        hasRemove = localContext.get()!!.contentResolver.delete(contentUri, null, null) > 0
                    }
                }

            } catch (e: IndexOutOfBoundsException) {
                e.printStackTrace()
                hasRemove = false

            } finally {
                localContext.clear()
            }
            return hasRemove
        }


        /**
         * remove File if exist
         *
         * @param context Android Context
         * @param fileName to remove File name
         * @param filePath to remove File path
         * @param fileSuffix to remove File Suffix
         *
         * @return file has removed
         * @throws FileNotFoundException cant find File
         */
        @Throws(FileNotFoundException::class)
        fun removeImage(context: Context, fileName: String, filePath: String, fileSuffix: String) : Boolean  {
            val isRemoved: Boolean

            val path = "${Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).path}/$filePath/$fileName.$fileSuffix"
            val verifyFile = File(path)

            if(!verifyFile.exists()) throw FileNotFoundException("can not find $fileName where $path")
            val localContext = WeakReference(context)
            val imageResolver = localContext.get()!!.contentResolver

            val selection = MediaStore.Images.Media.DATA
            val args = arrayOf(verifyFile.absolutePath)
            isRemoved = imageResolver.delete(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,"$selection = ?", args) > 0

            return isRemoved
        }

        /** Bitmap **/
        private const val commonImageCompressPercent = 75

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
        private fun bitmapToByteArray(bitmap: Bitmap, compressPercent: Int): ByteArray? {
            if(compressPercent < 1 || compressPercent > 100) throw IllegalArgumentException("ImageManager.bitmapToByteArray() wrong param \"compressPercent\"")
            val stream = ByteArrayOutputStream()

            bitmap.compress(Bitmap.CompressFormat.PNG, compressPercent, stream)
            return stream.toByteArray()
        }

        /**
         * Compress Bitmap
         *
         * @param context Android Context
         * @param source to compress Bitmap
         * @param compressPercent how many compress Bitmap?
         */
        private fun compressBitmap(context: Context, source: Bitmap, compressPercent: Int) : Bitmap?{
            val display = (context.getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay
            val displayWidth = display.width
            val displayHeight = display.height

            val options = BitmapFactory.Options()
            options.inPreferredConfig = Bitmap.Config.RGB_565
            options.inJustDecodeBounds = true

            val widthScale = (options.outWidth / displayWidth).toFloat()
            val heightScale = (options.outHeight / displayHeight).toFloat()
            val scale = if (widthScale > heightScale) widthScale else heightScale

            if (scale >= 8) {
                options.inSampleSize = 8

            } else if (scale >= 6) {
                options.inSampleSize = 6

            } else if (scale >= 4) {
                options.inSampleSize = 4

//            } else if (scale >= 2) {
//                options.inSampleSize = 2

            } else {
                options.inSampleSize = 2 // 1
            }
            options.inJustDecodeBounds = false

            val imageByteArray = this.bitmapToByteArray(source, compressPercent)

            return BitmapFactory.decodeByteArray(imageByteArray, 0, imageByteArray!!.size,  options)
        }
    }
}