package co.farmerline.verification.playground;

import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.view.CameraView;

import co.farmerline.verification.app.main.VerificationActivity;

/**
 * Created on 12/11/2020 at 12:00.
 *
 * @author Ezekiel Sebastine.
 */
class PlayGround extends AppCompatActivity {

    CameraView cameraView = new CameraView(null);

    void someMEthod(){
        Intent intent = new Intent(this, VerificationActivity.class);
    }
}
