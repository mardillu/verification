package co.farmerline.verification.utils.face.highlighter.transformer

interface FaceHighlightTransformer {
    val widthScaleFactor: Float
    val heightScaleFactor: Float

    val leftOffset: Int
    val topOffset: Int
}