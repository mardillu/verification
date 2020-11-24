package co.farmerline.verification.utils.face.detector

import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions

class FaceDetector : FaceDetectorBase() {
    override val TAG: String = "FaceDetectionProcessor"

    override lateinit var detector: FirebaseVisionFaceDetector
    override lateinit var options: FirebaseVisionFaceDetectorOptions

    init {
//        options = FirebaseVisionFaceDetectorOptions.Builder().apply {
//            setPerformanceMode(FirebaseVisionFaceDetectorOptions.FAST)
//            enableTracking()
//        }.build()

        options = FirebaseVisionFaceDetectorOptions.Builder()
            .setPerformanceMode(FirebaseVisionFaceDetectorOptions.ACCURATE) //speed-accuracy trade-off
            .setLandmarkMode(FirebaseVisionFaceDetectorOptions.ALL_LANDMARKS)
            .setClassificationMode(FirebaseVisionFaceDetectorOptions.ALL_CLASSIFICATIONS)
            .setMinFaceSize(0.15f)
            .enableTracking()
            .build()

        detector = FirebaseVision.getInstance().getVisionFaceDetector(options)
    }
}