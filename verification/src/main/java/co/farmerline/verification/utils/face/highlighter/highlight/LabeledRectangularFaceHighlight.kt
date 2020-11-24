package co.farmerline.verification.utils.face.highlighter.highlight

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import co.farmerline.verification.utils.face.highlighter.FaceHighlighter
import co.farmerline.verification.utils.face.face.Face

class LabeledRectangularFaceHighlight(face: Face) :
    RectangularFaceHighlight(face) {
    private val textPaint = Paint().apply {
        color = Color.TRANSPARENT
        textSize = 32f
    }

    private val textMargins = listOf(12, 12, -12, -12)

    override fun highlightOn(canvas: Canvas, highlighter: FaceHighlighter) {
        super.highlightOn(canvas, highlighter)

        if (face.label.isNotBlank()) {
            canvas.drawText(
                face.label,
                (boxFace.left + textMargins[0]).toFloat(),
                (boxFace.bottom + textMargins[3]).toFloat(),
                textPaint
            )
        }
    }
}