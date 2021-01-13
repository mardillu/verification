package co.farmerline.verification.app.main

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import co.farmerline.verification.R
import kotlin.properties.Delegates

class VerificationActivity : AppCompatActivity() {

    public var imageName: String? = ""
    public var farmerName: String? = ""
    public var phoneNumber: String? = ""
    public var farmerIdString: String? = ""
    public var modelPath: String? = ""
    public var threshHold by Delegates.notNull<Double>()
    public var farmerContext by Delegates.notNull<Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_verification)

        imageName = this.intent.getStringExtra("image_name")
        farmerContext = intent.getIntExtra("image_context", 1)
        farmerName = this.intent.getStringExtra("farmer_name")
        phoneNumber = this.intent.getStringExtra("farmer_phone_number")
        modelPath = this.intent.getStringExtra("model_name")
        threshHold = intent.getDoubleExtra("threshold", 0.70)
        farmerIdString = this.intent.getStringExtra("farmer_id_string")
    }

    public fun setFinishActivity(score: Double){
        var it = Intent(this, VerificationActivity::class.java)
        it.putExtra("score", score)
        it.putExtra("farmer_id_string", farmerIdString)
        if (score >= threshHold){
            it.putExtra("status", "success")
        }else{
            it.putExtra("status", "failed")
        }
        setResult(RESULT_OK, it)
        finish()
    }

    public fun setFinishActivity(message: String?){
        var it = Intent(this, VerificationActivity::class.java)
        it.putExtra("score", 0.0)
        it.putExtra("status", "failed")
        it.putExtra("message", message)
        it.putExtra("farmer_id_string", farmerIdString)
        setResult(RESULT_OK, it)
        finish()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val it = Intent(this, VerificationActivity::class.java)
        it.putExtra("score", -1.0)
        it.putExtra("status", "failed")
        it.putExtra("message", "Canceled")
        it.putExtra("farmer_id_string", farmerIdString)
        setResult(RESULT_CANCELED, it)
        finish()
    }
}