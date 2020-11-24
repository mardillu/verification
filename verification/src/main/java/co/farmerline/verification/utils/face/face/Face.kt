package co.farmerline.verification.utils.face.face

import android.graphics.Bitmap
import android.graphics.Rect
import com.google.firebase.ml.vision.face.FirebaseVisionFace
import co.farmerline.verification.utils.face.classifier.FaceClassifier
import co.farmerline.verification.utils.face.classifier.onClassifyLambdaType
import kotlin.properties.Delegates

class Face(visionFace: FirebaseVisionFace, var frame: Bitmap) {
    lateinit var boundingBox: Rect
    var trackingId: Int = -1
    lateinit var imageFrame: Bitmap
    var x by Delegates.notNull<Int>()
    var y by Delegates.notNull<Int>()
    var width by Delegates.notNull<Int>()
    var height by Delegates.notNull<Int>()

    var visionFace: FirebaseVisionFace = visionFace
        set(value) {
            field = value

            boundingBox = value.boundingBox
            trackingId = value.trackingId
        }

    init {
        this.updateFace(visionFace, frame)
    }

    var label = ""
        private set

    fun getFaceBitmap(): Bitmap {
        val faceX: Int =
            if ((boundingBox.centerX() + boundingBox.width()) > frame.width) 0 else boundingBox.centerX()
        val faceY: Int =
            if ((boundingBox.centerY() + boundingBox.height()) > frame.height) 0 else boundingBox.centerY()

        val faceWidth: Int =
            if ((faceX + boundingBox.width()) > frame.width) frame.width else boundingBox.width()
        val faceHeight: Int =
            if ((faceY + boundingBox.height()) > frame.height) frame.height else boundingBox.height()

        x = faceX
        y = faceY
        width = faceWidth
        height = faceHeight

        return Bitmap.createBitmap(frame, faceX, faceY, faceWidth, faceHeight)

//        return Bitmap.createBitmap(frame,
//            if (faceX<0) 0 else faceX,
//            if (faceY<0) 0 else faceY,
//            if (faceWidth<=0) 1 else faceWidth,
//            if (faceHeight<=0) 1 else faceHeight)
    }

    fun updateFace(visionFace: FirebaseVisionFace, frame: Bitmap) {
        this.frame = frame
        this.visionFace = visionFace
    }

    suspend fun classifyFace(
        faceClassifier: FaceClassifier,
        onClassify: onClassifyLambdaType = {}
    ) {
        faceClassifier.classify(this.getFaceBitmap()) {
            this.label = it
            onClassify(it)
        }
    }
}