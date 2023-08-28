package eu.basicairdata.graziano.gpslogger

import android.app.Activity.RESULT_CANCELED
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import eu.basicairdata.graziano.gpslogger.databinding.FragmentFuncTestBinding
import eu.basicairdata.graziano.gpslogger.management.ImageManager
import eu.basicairdata.graziano.gpslogger.management.TrackRecordManager


/**
 * @author dspark( sha1028en )
 * @since 2023-08-11
 *
 * just TEST / PROOF Func...
 */
class FuncTestFragment : Fragment() {
    private var mBinding: FragmentFuncTestBinding? = null

    private lateinit var requestCamera: ActivityResultLauncher<Intent>
    private lateinit var recordManager: TrackRecordManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?

    ): View {
        this.mBinding = FragmentFuncTestBinding.inflate(inflater)

        this.requestCamera = registerForActivityResult(ActivityResultContracts.StartActivityForResult(), ActivityResultCallback<ActivityResult>() { result ->
            if (result.resultCode == RESULT_OK && result.data != null) {
                val extras: Bundle? = result.data!!.extras
                if (extras != null) {
                    val bitmap = extras["data"] as Bitmap?
                    ImageManager.saveImage(this.mBinding!!.root.context, "Parking", "Hiking/서울 강남구 대모산 무장애 나눔길", bitmap)
                }

            } else if(result.resultCode == RESULT_CANCELED) {
                ImageManager.removeLastImage(this.mBinding!!.root.context, "Parking")
            }
        })

        this.recordManager = TrackRecordManager.createInstance(this.mBinding!!.root.context);

        this.mBinding!!.funcTestTextView.setOnClickListener {
            this.requestCamera.launch(
                Intent(MediaStore.ACTION_IMAGE_CAPTURE).also {
                    it.putExtra(MediaStore.EXTRA_OUTPUT, ImageManager.createTmpFile(this.mBinding!!.root.context, "Parking", "Hiking/서울 강남구 대모산 무장애 나눔길"))
                }
            )
        }

        this.mBinding!!.funcTestLoadImgBtn.setOnClickListener {
//            val image: Bitmap? = ImageManager.loadImage(this.mBinding!!.root.context, "Parking", "Hiking/서울 강남구 대모산 무장애 나눔길")
//            if(image != null) {
//                this.mBinding!!.funcTestImageView.setImageBitmap(image)
//            }
            val image = ImageManager.loadImage(this.mBinding!!.root.context, "Parking", "Hiking/서울 강남구 대모산 무장애 나눔길", "png")
            this.mBinding!!.funcTestImageView.setImageBitmap(image)
        }

        this.mBinding!!.funcTestAddAnnotation.setOnClickListener {
            this.recordManager.addPlaceMark("Parking", "서울 강남구 대모산 무장애 나눔길")
        }

        this.mBinding!!.funcTestStartRecord.setOnClickListener {
            this.recordManager.startRecordTrack("서울 강남구 대모산 무장애 나눔길", "코스 1");
        }

        this.mBinding!!.funcTestStopRecord.setOnClickListener {
            this.recordManager.stopRecordTrack(true, "서울 강남구 대모산 무장애 나눔길", "코스 1", false)
        }
        return this.mBinding!!.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        this.mBinding = null
    }
}