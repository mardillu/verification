package co.farmerline.verification.utils.face.highlighter.highlight

import android.graphics.Canvas
import co.farmerline.verification.utils.face.highlighter.FaceHighlighter
import co.farmerline.verification.utils.face.highlighter.transformer.FaceHighlightTransformer
import co.farmerline.verification.utils.face.face.Face

abstract class FaceHighlight(face: Face) {
    abstract fun highlightOn(canvas: Canvas, highlighter: FaceHighlighter)

    abstract fun transform(transformer: FaceHighlightTransformer)
}