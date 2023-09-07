package eu.basicairdata.graziano.gpslogger.management

import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.ExifInterface
import android.net.Uri
import android.os.Environment
import android.os.Environment.DIRECTORY_DCIM
import android.provider.MediaStore
import android.provider.OpenableColumns
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream
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

        fun createEmptyDirectory(directoryPath: String, directoryName: String): String {
            var dir: File
            for (i in 0..2) {
                dir = File(Environment.getExternalStoragePublicDirectory(DIRECTORY_DCIM).absolutePath + "/" + directoryPath + "/" + directoryName + i.toString())
                if (!dir.exists()) {
                    dir.mkdirs()
                    return directoryName + i.toString()
                }
            }
            return ""
        }

        fun parsePathFromUri(context: Context, uri: Uri?): String {
            if(uri == null) return ""
            var filePath = ""
            val localContext = WeakReference(context)

            if(uri.scheme.equals("content")) {
                try {
                    val cursor = localContext.get()!!.contentResolver.query(uri, null, null, null, null)
                    cursor.use {
                        it!!.moveToFirst()
                        val index = it.getColumnIndex(MediaStore.MediaColumns.DATA)
                        if(index > 0) {
                            filePath = cursor!!.getString(index)
                        }
                    }

                } catch (e: NullPointerException) {
                    filePath = ""

                } catch (e: IndexOutOfBoundsException) {
                    filePath = ""

                } finally {
                    localContext.clear()
                }
            }
            return filePath;
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
            val path = "${Environment.getExternalStoragePublicDirectory(DIRECTORY_DCIM).path}/$filePath"

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

        @Throws(FileNotFoundException::class)
        fun loadImageUriList(context: Context, fileName: String, filePath: String): LinkedList<Uri?> {
            val localContext = WeakReference(context)
            var uriBuffer: Uri?
            val imageUriList = LinkedList<Uri?>()

            val projection = arrayOf(
                MediaStore.MediaColumns._ID,
                MediaStore.MediaColumns.TITLE)
            val path = "${Environment.getExternalStoragePublicDirectory(DIRECTORY_DCIM).path}/$filePath"

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
                    uriBuffer = if(index > -1) {
                        ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, it.getLong(index))

                    } else {
                        null
                    }
                    imageUriList.add(uriBuffer)
                }
            }
            localContext.clear()

            if(imageUriList.size < 1) throw FileNotFoundException("cant load File from $path")
            return imageUriList
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
            val path = "${Environment.getExternalStoragePublicDirectory(DIRECTORY_DCIM).path}/$filePath/$fileName.$fileSuffix"
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

            val path = "${Environment.getExternalStoragePublicDirectory(DIRECTORY_DCIM).path}/$filePath/$fileName.$fileSuffix"
            val verifyFile = File(path)

            if(!verifyFile.exists()) throw FileNotFoundException("can not find $fileName where $path")
            val localContext = WeakReference(context)
            val imageResolver = localContext.get()!!.contentResolver

            val selection = MediaStore.Images.Media.DATA
            val args = arrayOf(verifyFile.absolutePath)
            isRemoved = imageResolver.delete(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,"$selection = ?", args) > 0

            return isRemoved
        }

        @Throws(IllegalArgumentException::class, IOException::class)
        fun addLocationIntoImage(image: File, lat: Double, lng: Double) {
            if(lat <= 0.0f || lng <= 0.0f) throw IllegalArgumentException("wrong param value: lat, lng")

            val imageExif = ExifInterface(image.absolutePath)
            imageExif.setAttribute(ExifInterface.TAG_GPS_LATITUDE, convert(lat))
            imageExif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE, convert(lng))

            imageExif.setAttribute(ExifInterface.TAG_GPS_LATITUDE_REF, if (lat > 0.0f) "N" else "S")
            imageExif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF, if(lng > 0.0f) "E" else "W")

            imageExif.saveAttributes()
        }

        // Convert latitude/longitude to exif format
        private fun convert(coord: Double): String? {
            var coord = coord
            coord = Math.abs(coord)
            val degrees = coord.toInt()
            coord = (coord - degrees) * 60
            val minutes = coord.toInt()
            coord = (coord - minutes) * 60
            val seconds = (coord * 1000).toInt()
            return "$degrees/1,$minutes/1,$seconds/1000"
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

        @Throws(IllegalArgumentException::class)
        fun compressBitmap(source: Bitmap, compressPercent: Int) : Bitmap {
            if(compressPercent < 1 || compressPercent > 100) throw IllegalArgumentException("ImageManager.bitmapToByteArray() wrong param \"compressPercent\"")
            val stream = ByteArrayOutputStream()

            source.compress(Bitmap.CompressFormat.PNG, compressPercent, stream)
            val compressRawData = stream.toByteArray()

            val compressedBitmap = BitmapFactory.decodeByteArray(compressRawData, 0, compressRawData.size);
            source.recycle()

            return compressedBitmap
        }


        @Throws(IllegalArgumentException::class)
        fun compressBitmapAggressive(source: Bitmap, compressPercent: Int, width: Int, height: Int): Bitmap {
            if(compressPercent < 1 || compressPercent > 100) throw IllegalArgumentException("ImageManager.bitmapToByteArray() wrong param \"compressPercent\"")

            val bitmapOption = BitmapFactory.Options()
            bitmapOption.inJustDecodeBounds = true;
            bitmapOption.inSampleSize = this.calculateInImageScale(bitmapOption, width, height)
            val stream = ByteArrayOutputStream()

            source.compress(Bitmap.CompressFormat.PNG, compressPercent, stream)
            val compressRawData = stream.toByteArray()
            bitmapOption.inJustDecodeBounds = false;

            val compressedBitmap = BitmapFactory.decodeByteArray(compressRawData, 0, compressRawData.size, bitmapOption);
            source.recycle()

            return compressedBitmap
        }

        @Throws(FileNotFoundException::class)
        fun loadBitmapWithCompressAggressive(context: Context, sourceUri: Uri, width: Int, height: Int) : Bitmap? {
            val localContext = WeakReference(context)
//            val bitmapOption = BitmapFactory.Options()
//            bitmapOption.inJustDecodeBounds = false
//            bitmapOption.inSampleSize = this.calculateInImageScale(bitmapOption, width, height)
//
//            val compressedBitmap = BitmapFactory.decodeStream(localContext.get()!!.contentResolver.openInputStream(sourceUri), null, bitmapOption)
//            localContext.clear()
//
//            return compressedBitmap

            // 이미지 사이즈 디코딩
            var imageOption = BitmapFactory.Options();
            imageOption.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(localContext.get()!!.contentResolver.openInputStream(sourceUri), null, imageOption);

            // 이미지가 클경우에는, 스케일 사이즈를 대략 적으로만 계산
            var scale = 1;
            if (imageOption.outWidth > width) {
                scale = imageOption.outWidth / width
            }
            // 샘플 사이즈로 디코딩
            imageOption = BitmapFactory.Options()
            imageOption.inSampleSize = scale

            val compressedBitmap = BitmapFactory.decodeStream(localContext.get()!!.contentResolver.openInputStream(sourceUri), null, imageOption);
            localContext.clear()

            return compressedBitmap
        }


        fun getFileFromImageURI(context: Context, contentUri: Uri): File {
            val localContext: WeakReference<Context> = WeakReference(context)

            val cursor = localContext.get()!!.contentResolver.query(contentUri, null, null, null, null);
            var path: String

            cursor.use {
                it!!.moveToLast()
                path = it.getString(it.getColumnIndex("_data"))
            }
            return File(path);
        }

        /**
         * calc to bitmap scale
         */
        private fun calculateInImageScale(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
            // Raw height and width of image
            val (height: Int, width: Int) = options.run { outHeight to outWidth }
            var inSampleSize = 1

            if (height > reqHeight || width > reqWidth) {

                val halfHeight: Int = height / 2
                val halfWidth: Int = width / 2

                // Calculate the largest inSampleSize value that is a power of 2 and keeps both
                // height and width larger than the requested height and width.
                while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                    inSampleSize *= 2
                }
            }
            return inSampleSize
        }
    }
}