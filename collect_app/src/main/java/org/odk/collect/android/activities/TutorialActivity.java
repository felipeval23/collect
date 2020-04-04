package org.odk.collect.android.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import org.odk.collect.android.R;

import java.util.ArrayList;

/*import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;*/


public class TutorialActivity extends CollectAbstractActivity {

    private static final String TAG = "TutorialActivity";
    private ArrayAdapter adapter;


    private static final String CV19 = "https://www.worldometers.info/coronavirus/";
    private static final String WCV19 = "https://www.cdc.gov/coronavirus/2019-ncov/downloads/2019-ncov-factsheet.pdf";
    private static final String PRO ="https://www.cdc.gov/coronavirus/2019-ncov/prepare/prevention.html";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tutorial);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setTitle(getString(R.string.tuto));
        setSupportActionBar(toolbar);

        initToolbar();

        ListView list = findViewById(R.id.questionTuto);
        /*EditText theFilter = findViewById(R.id.searchFilter);*/

      ///  Log.d(TAG, "onCreate: Started");

        //Add the Tutorial objects (questions) to an ArrayList
        final ArrayList<String> question = new ArrayList<>();
        question.add(getString(R.string.question1));
        question.add(getString(R.string.question2));
        question.add(getString(R.string.question3));
        question.add(getString(R.string.question4));



        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, question);//this table has a standard android layout
        list.setAdapter(adapter);

        /*theFilter.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                (ii_esQuestions.this).adapter.getFilter().filter(charSequence);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });*/

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG,"onItemClick: question: " + question.get(position));

                if (position+1==1)
                {
                    startActivity(new Intent(Intent.ACTION_VIEW,
                            Uri.parse(WCV19)));
                 ///   Intent intent = new Intent(TutorialActivity.this, iii_esQ2.class);
                   /// startActivity(intent);
                    //Toast.makeText(ii_esQuestions.this, "Has seleccionado: " + question.get(position), Toast.LENGTH_SHORT).show();
                }
                if (position+1==2)
                {
                    startActivity(new Intent(Intent.ACTION_VIEW,
                            Uri.parse(PRO)));
                   /// Intent intent = new Intent(TutorialActivity.this, question1.class);
                    ///startActivity(intent);
                    //Toast.makeText(TutorialActivity.this, "Has seleccionado: " + question.get(position), Toast.LENGTH_SHORT).show();
                }
                if (position+1==3)
                {
                    startActivity(new Intent(Intent.ACTION_VIEW,
                            Uri.parse(CV19)));
                  ///  Intent intent = new Intent(TutorialActivity.this, Question3Activity.class);
                    ///startActivity(intent);
                    //Toast.makeText(ii_esQuestions.this, "Has seleccionado: " + question.get(position), Toast.LENGTH_SHORT).show();
                }
            /*    if (position+1==4)
                {
                    Intent intent = new Intent(TutorialActivity.this, Question4Activity.class);
                    startActivity(intent);
                    //Toast.makeText(ii_esQuestions.this, "Has seleccionado: " + question.get(position), Toast.LENGTH_SHORT).show();
                }
                if (position+1==5)
                {
                    Intent intent = new Intent(ii_esQuestions.this, iii_esQ5.class);
                    startActivity(intent);
                    Toast.makeText(ii_esQuestions.this, "Has seleccionado: " + question.get(position), Toast.LENGTH_SHORT).show();
                }
                if (position+1==6)
                {
                    Intent intent = new Intent(ii_esQuestions.this, iii_esQ6.class);
                    startActivity(intent);
                    Toast.makeText(ii_esQuestions.this, "Has seleccionado: " + question.get(position), Toast.LENGTH_SHORT).show();
                }
                if (position+1==7)
                {
                    Intent intent = new Intent(ii_esQuestions.this, iii_esQ7.class);
                    startActivity(intent);
                    Toast.makeText(ii_esQuestions.this, "Has seleccionado: " + question.get(position), Toast.LENGTH_SHORT).show();
                }
                if (position+1==8)
                {
                    Intent intent = new Intent(ii_esQuestions.this, iii_esQ8.class);
                    startActivity(intent);
                    Toast.makeText(ii_esQuestions.this, "Has seleccionado: " + question.get(position), Toast.LENGTH_SHORT).show();
                }
                if (position+1==9)
                {
                    Intent intent = new Intent(ii_esQuestions.this, iii_esQ9.class);
                    startActivity(intent);
                    Toast.makeText(ii_esQuestions.this, "Has seleccionado: " + question.get(position), Toast.LENGTH_SHORT).show();
                }
                if (position+1==10)
                {
                    Intent intent = new Intent(ii_esQuestions.this, iii_esQ10.class);
                    startActivity(intent);
                    Toast.makeText(ii_esQuestions.this, "Has seleccionado: " + question.get(position), Toast.LENGTH_SHORT).show();
                }*/

            }
        });

    }

  /*  private void initToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setTitle(getString(R.string.admin_preferences));
        setSupportActionBar(toolbar);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.loginin, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_login:
                startActivity(new Intent(this, AccesoActivity.class));
                finish();
                return true;

        }
        return super.onOptionsItemSelected(item);
    }*/

    private void initToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }
}
