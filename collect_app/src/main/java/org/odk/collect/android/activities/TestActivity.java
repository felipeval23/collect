package org.odk.collect.android.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.widget.Toolbar;

import org.odk.collect.android.R;


public class TestActivity extends CollectAbstractActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test);

        Button enterDataButton = findViewById(R.id.tuto_data);
        enterDataButton.setText(getString(R.string.enter_tutorial));

        Button enterDataButton1 = findViewById(R.id.survey_data);
        enterDataButton1.setText(getString(R.string.enter_survey));

        initToolbar();
    }


    public void testx(View view){


        startActivity(new Intent( this, MainMenuActivity.class));
    }

    public void tutor(View view){

       startActivity(new Intent(this, TutorialActivity.class));
    }

    private void initToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }







}
