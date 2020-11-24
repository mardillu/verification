package co.farmerline.verification.app.facerecognition

import android.content.Context
import androidx.camera.core.ImageAnalysis
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.farmerline.verification.utils.camera.analysis.CameraAnalysisFactory
import co.farmerline.verification.utils.camera.analysis.CameraFrameAnalyzerLambdaType
import co.farmerline.verification.utils.camera.analysis.configs.LowResFaceDetectionCameraAnalysisConfig
import co.farmerline.verification.utils.face.detector.FaceDetector
import co.farmerline.verification.utils.face.detector.onDetectLambdaType
import co.farmerline.verification.utils.face.face.Face
import co.farmerline.verification.utils.face.highlighter.highlight.FaceHighlight
import co.farmerline.verification.utils.face.highlighter.highlight.LabeledRectangularFaceHighlight
import com.google.android.gms.vision.face.Landmark
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata
import kotlinx.coroutines.launch

class FaceRecognitionViewModel : ViewModel() {

    private lateinit var faceDetector: FaceDetector
    private lateinit var fragment: FaceRecognitionFragment

    val highlightedFacesHashMap = HashMap<Int, Face>()
    val highlightedFacesLiveData = MutableLiveData<List<FaceHighlight>>()
    private val highlightDetectedFaces: onDetectLambdaType = { faces, frame ->
        faces.forEach {
            if (highlightedFacesHashMap.containsKey(it.trackingId)) {
                highlightedFacesHashMap[it.trackingId]?.updateFace(it, frame)
            } else {
                val face = Face(it, frame)
                highlightedFacesHashMap.put(it.trackingId, face)
                viewModelScope.launch {
                    highlightedFacesHashMap[it.trackingId]?.apply {
                        //face.classifyFace(faceClassifier)
                    }
                }
            }
        }

        val highlightedFacesList = ArrayList<FaceHighlight>()
        highlightedFacesHashMap.values.forEach {
            highlightedFacesList.add(LabeledRectangularFaceHighlight(it))
        }

        highlightedFacesLiveData.postValue(highlightedFacesList)

        //Here's a chance to auto-take snapshot
        if (faces.size == 1){
            val face = faces[0]

            val bounds = face.boundingBox

            // If landmark detection was enabled (mouth, ears, eyes, cheeks, and
            // nose available):
            val leftEye = face.getLandmark(Landmark.LEFT_EYE)
            val rightEye = face.getLandmark(Landmark.RIGHT_EYE)

            if (leftEye != null){
                if (rightEye != null){
                    fragment.takePicture()
                }else{
                    fragment.turnFaceLeft()
                }
            }else{
                fragment.turnFaceRight()
            }
        }
    }

    public lateinit var cameraFrameAnalysis: ImageAnalysis
    public val cameraFrameAnalysisConfig = LowResFaceDetectionCameraAnalysisConfig

    private val cameraFameAnalyzer: CameraFrameAnalyzerLambdaType = { imageProxy, rotation ->
        val mediaImage = imageProxy?.image

        val imageRotation = when (rotation) {
            0 -> FirebaseVisionImageMetadata.ROTATION_0
            90 -> FirebaseVisionImageMetadata.ROTATION_90
            180 -> FirebaseVisionImageMetadata.ROTATION_180
            270 -> FirebaseVisionImageMetadata.ROTATION_270
            else -> throw Exception("Rotation must be 0, 90, 180, or 270.")
        }

        mediaImage?.let {
            faceDetector.processImage(mediaImage, imageRotation, highlightDetectedFaces)
        }
    }

    fun initViewModel(context: Context, fragment: FaceRecognitionFragment) {
        this.fragment = fragment
        initFaceDetector()
        //initFaceClassifier(context)
        initCameraFrameAnalysis()
    }

    private fun initFaceDetector() {
        faceDetector = FaceDetector()
    }

    private fun initFaceClassifier(context: Context) {
        //faceClassifier = FaceClassifier.getInstance(context, OracleFaceClassifierConfig)
    }

    private fun initCameraFrameAnalysis() {
        cameraFrameAnalysis = CameraAnalysisFactory.createCameraAnalysis(
            cameraFrameAnalysisConfig,
            analyzer = cameraFameAnalyzer
        )
    }

    override fun onCleared() {
        super.onCleared()

        faceDetector.stop()
    }
}