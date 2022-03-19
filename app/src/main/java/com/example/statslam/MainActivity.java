package com.example.statslam;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import dev.firstseed.sports_reference.AbstractSeason;
import dev.firstseed.sports_reference.DownloadListener;
import dev.firstseed.sports_reference.SportsReference;
import dev.firstseed.sports_reference.StatModel;
import dev.firstseed.sports_reference.cbb.CBB;
import dev.firstseed.sports_reference.cbb.Season;

public class MainActivity extends AppCompatActivity
{

    public Button editAlgButton;
    public Button headToHeadButton;
    public Button tournamentSimButton;

    private Spinner seasonSpinner;
    private Spinner modelSpinner;

    private CBB cbb;
    private final int MIN_YEAR = 2010;
    private final int MAX_YEAR = 2022;

    int selectedYear = MAX_YEAR;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        seasonSpinner = findViewById(R.id.seasonSpinner);
        modelSpinner = findViewById(R.id.modelSpinner);
        editAlgButton = findViewById(R.id.editAlgButton);
        headToHeadButton = findViewById(R.id.headToHeadButton);
        tournamentSimButton = findViewById(R.id.tournamentSimButton);

        headToHeadButton = findViewById(R.id.headToHeadButton);
        headToHeadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, HeadToHeadActivity.class);
                intent.putExtra("year", selectedYear);
                intent.putExtra("model", (String)modelSpinner.getSelectedItem());
                startActivity(intent);
            }
        });
        disableButton(headToHeadButton);

        tournamentSimButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, TournamentSimActivity.class);
                intent.putExtra("year", selectedYear);
                intent.putExtra("model", (String)modelSpinner.getSelectedItem());
                startActivity(intent);

            }
        });
        disableButton(tournamentSimButton);

        try{
            Assets.copyTournamentToCache(this);
        }
        catch(Exception e){
            e.printStackTrace();
        }

        SportsReference.cbb(DirectoryManager.CACHE_DIR(this));
        cbb = SportsReference.cbb();
        final Activity activity = this;
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                cbb.getSeason(MAX_YEAR);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        enableButton(editAlgButton);
                        enableButton(headToHeadButton);
                        enableButton(tournamentSimButton);
                        Thread t = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                setSpinner();
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        modelSpinner.setSelection(Assets.getModelList(activity).indexOf("2016_2021_2_1528"));
                                    }
                                });

                            }
                        });
                        t.start();


                    }
                });

            }
        });
        t.start();


    }

    private void disableButton(Button button)
    {
        button.setEnabled(false);
        button.setBackgroundColor(Color.GRAY);
    }

    private void enableButton(Button button)
    {
        button.setEnabled(true);
        button.setBackgroundColor(getColor(R.color.colorPrimary));
    }

    @Override
    public void onStart()
    {
        super.onStart();
        //season = SportsReference.cbb().getSeason();
        try
        {
            String model = getIntent().getStringExtra("model");
            modelSpinner.setSelection(Assets.getModelList(this).indexOf(model));
            seasonSpinner.setSelection(getIntent().getIntExtra("year", MAX_YEAR) - MIN_YEAR);
        }
        catch(Exception e)
        {
            modelSpinner.setSelection(Assets.getModelList(this).indexOf("2010_2019_1580"));
            seasonSpinner.setSelection(0);
        }

    }

    public void setSpinner()
    {

        final ArrayList<String> seasons = new ArrayList<>();
        int index=0;
        int selectedPos = 12;
        for(int i=MIN_YEAR; i<=MAX_YEAR; i++)
        {
            seasons.add(Integer.toString(i));
        }
        final int sp = selectedPos;
        final ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, seasons);
        adapter.setDropDownViewResource(android.R.layout.simple_list_item_1);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                seasonSpinner.setAdapter(adapter);
                seasonSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        disableButton(tournamentSimButton);
                        disableButton(headToHeadButton);
                        final int year = Integer.parseInt(adapter.getItem(position));
                        selectedYear = year;
                        //season = SportsReference.cbb().getSeason();
                        if (cbb.getSeason(year) == null) {
                            disableButton(tournamentSimButton);
                            disableButton(headToHeadButton);
                        }
                        else
                        {
                            enableButton(tournamentSimButton);
                            enableButton(headToHeadButton);
                        }

                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }

                });


                seasonSpinner.setSelection(sp);
            }
        });

        final ArrayAdapter<String> modelAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, Assets.getModelList(this));
        modelAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                modelSpinner.setAdapter(modelAdapter);
            }
        });



    }

    // Storage Permissions variables
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    //persmission method.
    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have read or write permission
        int writePermission = ActivityCompat.checkSelfPermission(activity, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int readPermission = ActivityCompat.checkSelfPermission(activity, android.Manifest.permission.READ_EXTERNAL_STORAGE);

        if (writePermission != PackageManager.PERMISSION_GRANTED || readPermission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }

    private void sendEmail(String subject, String body, File attachment){
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        Log.d("LF", "Creating Email Intent");
        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.setType("message/rfc822");
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
        emailIntent.putExtra(Intent.EXTRA_TEXT, body);
        Uri path = Uri.fromFile(attachment);
        //Uri path = FileProvider.getUriForFile(activity, "com.phase1eng.nick.graham2.provider", attachment);
        emailIntent.putExtra(Intent.EXTRA_STREAM, path);
        Log.d("LF", "Sending Email");
        try {
            startActivity(Intent.createChooser(emailIntent, "Send mail..."));
            Log.d("LF", "Done!");
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(this, "There are no email clients installed.", Toast.LENGTH_SHORT).show();
        }
    }

}
