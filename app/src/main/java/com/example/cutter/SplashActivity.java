package com.example.cutter;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SplashActivity extends AppCompatActivity {
    private int SPLASH_TIME_OUT = 3000;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        TextView textView = findViewById(R.id.text_view_splash_title);
        List<String> randomQuotes = new ArrayList<>();
        randomQuotes.add("INUYASHA ABAJO!");
        randomQuotes.add("I AM THE STORM THAT IS APPROACHING");
        randomQuotes.add("OJALA ME GANE LA LOTERIA");
        randomQuotes.add("MARIKA!");
        randomQuotes.add("NO TIENE PITO D:");
        randomQuotes.add("A QUIEN LLAMAS SEÑORITA!?");
        randomQuotes.add("PREPARE FOR TROUBLE AND MAKE IT DOUBLE");
        randomQuotes.add("HOW CAN YOU MEND A BROKEN HEARTH?");
        randomQuotes.add("ARI ARI ARI ARI ARI");
        randomQuotes.add("HARDER DADDY");
        randomQuotes.add("SON? :C");
        randomQuotes.add("Daga Kotowaru");
        randomQuotes.add("Doitsu no kagaku wa sekai ichi!");
        randomQuotes.add("KONO DIO DA!");
        randomQuotes.add("ZA WARUDO!");
        randomQuotes.add("CHINGAWHAT!!");
        randomQuotes.add("IS A TRAP!");
        randomQuotes.add("QUE TE PASA QUE ESTAS TAN EXITADA?");
        randomQuotes.add("WELCOME TO HELL");
        randomQuotes.add("IF YOU WANT IT THEN YOU WILL HAVE TO TAKE IT");
        randomQuotes.add("DATOS QUE SERAN FUNDAMENTAL PARA LA TRAMA");
        int random = new Random().nextInt(randomQuotes.size());
        textView.setText(randomQuotes.get(random));
        final Intent intent = new Intent(this, MainActivity.class);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(intent);
            }
        },SPLASH_TIME_OUT);
    }
}