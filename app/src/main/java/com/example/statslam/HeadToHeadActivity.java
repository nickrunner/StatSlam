package com.example.statslam;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.ArrayList;

import dev.firstseed.sports_reference.AbstractSeason;
import dev.firstseed.sports_reference.AbstractTeam;
import dev.firstseed.sports_reference.DownloadListener;
import dev.firstseed.sports_reference.Game;
import dev.firstseed.sports_reference.SportsReference;
import dev.firstseed.sports_reference.StatModel;
import dev.firstseed.sports_reference.cbb.Season;
import dev.firstseed.sports_reference.cbb.Team;

public class HeadToHeadActivity extends AppCompatActivity
{
    public Button simButton;
    public Spinner team1Spinner;
    public Spinner team2Spinner;
    public Spinner modelSpinner;
    public TextView scoreTv;
    public TextView confidenceTv;
    public Season season;
    public Button backButton;
    public CheckBox nonTournyCb;

    private boolean cancelSim = false;
    @Override
    protected void onStart() {
        super.onStart();
        cancelSim = false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_head_to_head);

        int year = getIntent().getIntExtra("year", 2021);


        cancelSim = false;
        season = SportsReference.cbb().getSeason(year);

        team1Spinner = findViewById(R.id.team1Spinner);
        team2Spinner = findViewById(R.id.team2Spinner);
        modelSpinner = findViewById(R.id.h2hModelSpinner);
        simButton = findViewById(R.id.simulateButton);
        scoreTv = findViewById(R.id.scoreTv);
        confidenceTv = findViewById(R.id.confidenceTv);
        backButton = findViewById(R.id.backButtonHeadToHead);
        nonTournyCb = findViewById(R.id.nonTournyCbHeadToHead);
        nonTournyCb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                setSpinners();
            }
        });

        final Activity activity = this;
        simButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    final AbstractTeam team1 = season.getTeam((String) team1Spinner.getSelectedItem());
                    final AbstractTeam team2 = season.getTeam((String) team2Spinner.getSelectedItem());
                    final Game game = new Game(team1, team2);
                    String selectedItem = (String) (modelSpinner.getSelectedItem());
                    if (selectedItem.equals("All"))
                    {
                        Thread t = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                int t1Count = 0;
                                int t2Count = 0;
                                int t1Score = 0;
                                int t2Score = 0;
                                int total = 0;
                                final int size = Assets.getModelList(activity).size();
                                for(String modelName : Assets.getModelList(activity))
                                {
                                    if(cancelSim)
                                    {
                                        break;
                                    }
                                    if(modelName.equals("All"))
                                    {
                                        continue;
                                    }
                                    try
                                    {
                                        StatModel model = Assets.getModel(activity, modelName);
                                        season.calculateComposites(model);
                                        game.predictOutcome(model);
                                        total++;
                                        t1Score += game.team1pts;
                                        t2Score += game.team2pts;
                                        if(game.winner.name.equals(team1.name))
                                        {
                                            t1Count++;
                                        }
                                        else
                                        {
                                            t2Count++;
                                        }
                                        int t1Pts = (int)Math.round( (double)t1Score/(double)total);
                                        int t2Pts = (int)Math.round((double)t2Score/(double)total);
                                        final double confidence = t1Count > t2Count ? (double)t1Count/(double)total : (double)t2Count/(double)total;
                                        final String winner = t1Count > t2Count ? team1.name: team2.name;
                                        final String loser = t1Count > t2Count ? team2.name : team1.name;
                                        final int winnerPts = t1Count > t2Count ? t1Pts : t2Pts;
                                        final int loserPts = t1Count > t2Count ? t2Pts : t1Pts;
                                        final DecimalFormat df = new DecimalFormat("##.##");
                                        final int tt = total;

                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                scoreTv.setText(winner+" "+winnerPts+"\n"+loser+" "+loserPts);
                                                confidenceTv.setText("Confidence: "+df.format(confidence*100)+"%"+" \nSimulations ran: "+tt+"/"+(size-1));
                                            }
                                        });


                                    }
                                    catch (Exception e)
                                    {
                                        continue;
                                    }
                                }
                            }
                        });
                        t.start();

                    }
                    else
                    {
                        StatModel model = Assets.getModel(activity, selectedItem);
                        season.calculateComposites(model);



                        game.predictOutcome(model);
                        DecimalFormat df = new DecimalFormat("##.##");
                        double confidence = game.confidence + 50;
                        if(confidence > 100)
                        {
                            confidence = 100.0;
                        }
                        scoreTv.setText(game.winner.name+" "+game.winnerPts+"\n"+game.loser.name+" "+game.loserPts);
                        confidenceTv.setText("Confidence: "+df.format(confidence)+"%");
                    }


                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });

        setSpinners();
        String model = getIntent().getStringExtra("model");
        modelSpinner.setSelection(Assets.getModelList(this).indexOf(model));

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelSim = true;
                Intent intent = new Intent(HeadToHeadActivity.this, MainActivity.class);
                intent.putExtra("model", (String)modelSpinner.getSelectedItem());
                intent.putExtra("year", season.getYear());
                startActivity(intent);
            }
        });
    }

    public void setSpinners(){
        ArrayList<String> teamNames = new ArrayList();
        if(nonTournyCb.isChecked()){
            teamNames = season.getTeamNames();
        }
        else{
            teamNames = season.getTeamsInTournament();
        }
        ArrayAdapter<String> teamAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, teamNames);
        teamAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        team1Spinner.setAdapter(teamAdapter);
        team2Spinner.setAdapter(teamAdapter);

        ArrayAdapter<String> modelAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, Assets.getModelList(this));
        modelAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        modelSpinner.setAdapter(modelAdapter);
    }
}
