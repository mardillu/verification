package co.farmerline.verification.app.facerecognition

import android.Manifest
import android.app.Dialog
import android.app.ProgressDialog
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.camera.core.CameraX
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCapture.ImageCaptureError
import androidx.camera.view.CameraView
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import co.farmerline.verification.R
import co.farmerline.verification.app.facerecognition.face.FaceNet
import co.farmerline.verification.app.main.VerificationActivity
import co.farmerline.verification.utils.camera.view.CameraViewFactory
import co.farmerline.verification.utils.camera.view.configs.FaceDetectionCameraViewConfig
import co.farmerline.verification.utils.face.highlighter.FaceHighlighter
import java.io.File


class FaceRecognitionFragment : Fragment(), LifecycleOwner {

    private lateinit var viewModel: FaceRecognitionViewModel
    private val viewModelInstance: Class<FaceRecognitionViewModel> =
        FaceRecognitionViewModel::class.java

    private var frameCount = 0

    private lateinit var cameraView: CameraView
    private val cameraViewConfig = FaceDetectionCameraViewConfig
    lateinit var dialog: ProgressDialog

    private lateinit var highlighterFaces: FaceHighlighter
    var countDownTimer: CountDownTimer? = null
    var dia: AlertDialog.Builder? = null

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_facedetection, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProviders.of(this).get(viewModelInstance)
        viewModel.initViewModel(view.context, this)

        initViews(view)

        setupViews()

        setupObservables()
    }

    private fun initViews(view: View) {
        view.apply {
            cameraView = findViewById(R.id.view_camera)
            highlighterFaces = findViewById(R.id.highlighter_faces)
        }
    }

    private fun setupViews() {
        cameraView.post {
            setupCameraView()

            setupCameraAnalysis()
        }
    }

    private fun setupCameraView() {
        cameraView.apply {
            if (activity?.let { ActivityCompat.checkSelfPermission(it, Manifest.permission.CAMERA) } == PackageManager.PERMISSION_GRANTED
                && activity?.let { ActivityCompat.checkSelfPermission(
                            it,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ) } == PackageManager.PERMISSION_GRANTED
                && activity?.let { ActivityCompat.checkSelfPermission(
                            it,
                            Manifest.permission.READ_EXTERNAL_STORAGE
                    ) } == PackageManager.PERMISSION_GRANTED
            ) {
                bindToLifecycle(this@FaceRecognitionFragment)
                CameraViewFactory.applyConfig(cameraView, cameraViewConfig)
                highlighterFaces.attachCameraView(cameraView, cameraViewConfig)
            }else{
                requestPermissions(
                        arrayOf(
                                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                Manifest.permission.READ_EXTERNAL_STORAGE,
                                Manifest.permission.CAMERA
                        ),
                        1
                )
            }
        }
    }

    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<out String>,
            grantResults: IntArray
    ) {
        when (requestCode) {
            1 -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    cameraView.apply {
                        if (activity?.let {
                                    ActivityCompat.checkSelfPermission(
                                            it,
                                            Manifest.permission.CAMERA
                                    )
                                } == PackageManager.PERMISSION_GRANTED
                                && activity?.let {
                                    ActivityCompat.checkSelfPermission(
                                            it,
                                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                                    )
                                } == PackageManager.PERMISSION_GRANTED
                                && activity?.let {
                                    ActivityCompat.checkSelfPermission(
                                            it,
                                            Manifest.permission.READ_EXTERNAL_STORAGE
                                    )
                                } == PackageManager.PERMISSION_GRANTED
                        ) {
                            bindToLifecycle(this@FaceRecognitionFragment)
                            CameraViewFactory.applyConfig(cameraView, cameraViewConfig)
                            highlighterFaces.attachCameraView(cameraView, cameraViewConfig)
                        } else {
                            Toast.makeText(
                                    requireContext(),
                                    "Permission denied. Face verification cannot continue",
                                    Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }
            }
        }
    }

    private fun setupCameraAnalysis() {
        CameraX.bindToLifecycle(this, viewModel.cameraFrameAnalysis)
        highlighterFaces.attachCameraAnalysisConfig(viewModel.cameraFrameAnalysisConfig)
    }

    private fun setupObservables() {
        viewModel.highlightedFacesLiveData.observe(this, Observer { highlightedFaces ->
            highlighterFaces.clear()

            highlightedFaces.forEach {
                highlighterFaces.add(it)
            }

            highlighterFaces.postInvalidate()
        })
    }

    public fun takePicture(){
        //restart the count down timer whenever a face is detected
        updateCountDownTimer()

        //take picture on the fifth frame only, per session
        //this gives camera enough time to focus on target face and take a good image
        if (frameCount++ != 5){
            return
        }
        val file = File(
                Environment.getExternalStorageDirectory()
                        .toString() + "/MERGDATA/Images/temp_face_image.jpg"
        )
        cameraView.takePicture(file, object : ImageCapture.OnImageSavedListener {
            override fun onImageSaved(file: File) {
                Log.d("TAG", "onImageSaved: Image captured and saved")
                //run recognition
                verifyFace()
            }

            override fun onError(
                    imageCaptureError: ImageCaptureError,
                    message: String,
                    cause: Throwable?
            ) {
                Log.d("TAG", "onImageSaved: $message")
                if (cause != null) {
                    Log.d("TAG", "onImageSaved: ${cause.printStackTrace()}")
                }
            }
        })
    }

    public fun turnFaceRight(){
        Toast.makeText(
                requireContext(),
                "Turn face slightly to the right",
                Toast.LENGTH_LONG
        ).show()
    }

    public fun turnFaceLeft(){
        Toast.makeText(
                requireContext(),
                "Turn face slightly to the left",
                Toast.LENGTH_LONG
        ).show()
    }

    private fun showDialog(){
        dialog = ProgressDialog(activity)
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)
        dialog.setMessage("Verifying farmer face, please wait...")

        dialog.show()
    }

    private fun hideDialog(){
        activity?.runOnUiThread {
            if (dialog != null && dialog.isShowing) {
                dialog.dismiss()
            }
        }
    }

    fun verifyFace(){
        showDialog()
        val cameraImage = Environment.getExternalStorageDirectory()
            .toString() + "/MERGDATA/Images/temp_face_image.jpg"

        var fContext =  (activity as VerificationActivity?)!!.farmerContext
        var subLoc = if (fContext==1)
            "Images"
        else
            "reference_Images"

        val farmerImage = Environment.getExternalStorageDirectory()
            .toString() + "/MERGDATA/" + subLoc +"/" + (activity as VerificationActivity?)!!.imageName

        val options: BitmapFactory.Options = BitmapFactory.Options()
        options.inPreferredConfig = Bitmap.Config.ARGB_8888
        options.inSampleSize = 8
        val cameraFaceBitmap: Bitmap = BitmapFactory.decodeFile(cameraImage, options)
        val farmerFaceBitmap: Bitmap = BitmapFactory.decodeFile(farmerImage, options)

        verifyFace(cameraFaceBitmap, farmerFaceBitmap)
    }

    private fun verifyFace(face1Bitmap: Bitmap?, face2Bitmap: Bitmap?) {
        Thread {
            try {
                var modelPath: String? =  (activity as VerificationActivity?)!!.modelPath
                val facenet: FaceNet
                facenet = if (modelPath == null){
                    FaceNet(activity?.assets)
                }else{
                    FaceNet(modelPath)
                }
                //val mtcnn = MTCNN(activity?.assets)
                //val face1: Bitmap = facenet.cropFace(face1Bitmap, mtcnn)
                //val face2: Bitmap = facenet.cropFace(face2Bitmap, mtcnn)
                // To make sure both faces were detected successfully
                val score: Double = facenet.computeCosineSimilarity(
                        face1Bitmap,
                        face2Bitmap
                ) // cosine similarity between the face descriptor vectors
                Log.d("TAG", "verifyFace: SCORE $score")
                //mtcnn.close()
                facenet.close()
                sendResultSuccess(score)
            } catch (e: Exception) {
                e.printStackTrace()
                sendResultError(e.message)
            }
        }.start()
    }

    private fun sendResultSuccess(score: Double){
        try {
            (activity as VerificationActivity?)!!.setFinishActivity(score)
        }catch (e: java.lang.Exception){}
    }

    override fun onPause() {
        super.onPause()
        countDownTimer?.cancel()
    }

    override fun onDestroy() {
        super.onDestroy()
        hideDialog()
    }

    override fun onResume() {
        super.onResume()
        updateCountDownTimer()
    }

    private fun sendResultError(message: String?){
        try {
            (activity as VerificationActivity?)!!.setFinishActivity(message)
        }catch (e: java.lang.Exception){}
    }

    private fun updateCountDownTimer(){
        countDownTimer?.cancel()
        countDownTimer = object : CountDownTimer(40000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val secsLeft = millisUntilFinished/1000
                if (secsLeft <= 10){ //10 secs left
                    //start visual countdown
                    updateContDownUI(secsLeft)
                }
            }

            override fun onFinish() {
                sendResultError("Timed out")
            }
        }.start()
    }

    var d: AlertDialog? = null
    fun updateContDownUI(secLef: Long){
        //dia = activity?.let { AlertDialog.Builder(it) }
        if (dia == null){
            dia = activity?.let { AlertDialog.Builder(it) }
            dia?.setTitle("Still Verifying?")
            dia?.setMessage("Verification will automatically close in $secLef")
            dia?.setCancelable(false)
            dia?.setPositiveButton("Close Now") { dialog, _ ->
                run {
                    dialog.dismiss()
                    countDownTimer?.cancel()
                    sendResultError("Timed out")
                }
            }
            dia?.setNegativeButton("Cancel") { dialog, _ ->
                run {
                    dialog.dismiss()
                    updateCountDownTimer()
                }
            }
            dia?.create()
            d = dia?.show()
        }else{
            if (!d?.isShowing!!){
                d = dia?.show()
            }
            d?.setMessage("Verification will automatically close in $secLef")
        }
    }
}