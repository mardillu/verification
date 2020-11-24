package com.mardillu.verification;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import co.farmerline.verification.app.main.IntroActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btn = findViewById(R.id.btn);

        btn.setOnClickListener(this::startt);
    }

    /**
     * This is how you will send a initiate the verification
     *
     * Use an intent then {@link this#startActivityForResult(Intent, int)}
     *
     * And wait for the result in {@link this#onActivityResult(int, int, Intent)}
     * verification result will be contained in the Intent data
     */
    public void startt(View view){
        Intent intent = new Intent(this, IntroActivity.class);
        intent.putExtra("image_name", "78.jpg");
        intent.putExtra("image_context", 1);
        intent.putExtra("farmer_name", "Ezekiel Sebastine");
        intent.putExtra("farmer_phone_number", "0550670914");
        startActivityForResult(intent, 1);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1){
            if (resultCode == RESULT_OK){
                Log.d("TAG", "onActivityResult: OK");
                String status = data.getStringExtra("status");
                double score = data.getDoubleExtra("score", 0.0);
                if (status != null && status.equals("success")){
                    //Face similarity score was 0.85 or higher
                    Log.d("TAG", "onActivityResult: MATCH FOUND "+score);
                }else{
                    //Face similarity score was below 0.85
                    Log.d("TAG", "onActivityResult: NO MATCH "+score);
                }
            }else{
                //Result not ok. Reason will be found in message data
                String status = data.getStringExtra("message");
                Log.d("TAG", "onActivityResult: CANCEL "+status);
            }
        }
    }
}