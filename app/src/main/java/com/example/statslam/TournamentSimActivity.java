package com.example.statslam;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;

import dev.firstseed.sports_reference.Game;
import dev.firstseed.sports_reference.SportsReference;
import dev.firstseed.sports_reference.StatModel;
import dev.firstseed.sports_reference.cbb.NcaaBracket;
import dev.firstseed.sports_reference.cbb.Season;

public class TournamentSimActivity extends AppCompatActivity
{

    public Season season;
    public LinearLayout firstRoundLl;
    public LinearLayout secondRoundLl;
    public LinearLayout sweetSixteenLl;
    public LinearLayout eliteEightLl;
    public LinearLayout finalFourLl;
    public LinearLayout championshipLl;
    public LinearLayout championLl;
    public Spinner modelSpinner;

    public Button backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tournament_sim);


        int year = getIntent().getIntExtra("year", 2022);
        season = SportsReference.cbb().getSeason(year);
        modelSpinner = findViewById(R.id.tournamentModelSpinner);
    }

    @Override
    public void onStart()
    {
        super.onStart();
        setSpinner();
        String model = getIntent().getStringExtra("model");
        modelSpinner.setSelection(Assets.getModelList(this).indexOf(model));
    }

    public void setSpinner(){
        ArrayAdapter<String> modelAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, Assets.getModelList(this));
        modelAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        modelSpinner.setAdapter(modelAdapter);

        final Activity activity = this;
        modelSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                try
                {
                    String selectedItem = (String) (modelSpinner.getSelectedItem());
                    if(selectedItem.equals("All")){
                        ArrayList<String> modelList = Assets.getModelList(activity);
                        ArrayList<StatModel> models = new ArrayList();
                        for(String modelName : modelList){
                            if(modelName.equals("All")){
                                continue;
                            }
                            System.out.println("Getting model "+modelName);
                            models.add(Assets.getModel(activity, modelName));
                        }
                        Thread t = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                NcaaBracket bracket = season.getPredictedBracket(models);
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        showTournament(bracket);
                                    }
                                });

                            }
                        });
                        //t.start();

                    }
                    else{
                        StatModel model = Assets.getModel(activity, selectedItem);
                        season.calculateComposites(model);
                        showTournament(season.getPredictedBracket(model));
                    }

                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }

        });
    }

    public void showTournament(NcaaBracket tournament)
    {

        firstRoundLl = findViewById(R.id.firstRoundLl);
        secondRoundLl = findViewById(R.id.secondRoundLl);
        sweetSixteenLl = findViewById(R.id.sweetSixteenLl);
        eliteEightLl = findViewById(R.id.eliteEightLl);
        finalFourLl = findViewById(R.id.finalFourLl);
        championshipLl = findViewById(R.id.championshipLl);
        championLl = findViewById(R.id.chamiponLl);

        firstRoundLl.setDividerPadding(5);
        secondRoundLl.setDividerPadding(5);
        sweetSixteenLl.setDividerPadding(5);
        eliteEightLl.setDividerPadding(5);
        finalFourLl.setDividerPadding(5);
        championshipLl.setDividerPadding(5);
        championLl.setDividerPadding(5);

        firstRoundLl.removeAllViews();
        secondRoundLl.removeAllViews();
        sweetSixteenLl.removeAllViews();
        eliteEightLl.removeAllViews();
        finalFourLl.removeAllViews();
        championshipLl.removeAllViews();
        championLl.removeAllViews();

        for(Game game : tournament.getRound1()){
            TextView tv1 = new TextView(this);
            tv1.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            if(game.team1 != null){
                tv1.setText(game.team1.name+"  \t"+game.team1pts);
            }


            TextView tv2 = new TextView(this);
            tv2.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            if(game.team2 != null) {
                tv2.setText(game.team2.name + "  \t" + game.team2pts);
            }
            if(game.team1pts > game.team2pts){
                tv1.setTextColor(getResources().getColor(R.color.colorPrimary));
            }
            else{
                tv2.setTextColor(getResources().getColor(R.color.colorPrimary));
            }
            if(tournament.getRound1().indexOf(game) %2 == 0){
                tv1.setBackgroundColor(Color.LTGRAY);
                tv2.setBackgroundColor(Color.LTGRAY);
            }

            firstRoundLl.addView(tv1);
            firstRoundLl.addView(tv2);
        }

        for(Game game : tournament.getRound2()){
            TextView tv1 = new TextView(this);
            tv1.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            if(game.team1 != null) {
                tv1.setText(game.team1.name + "  \t" + game.team1pts);
            }
            TextView tv2 = new TextView(this);
            tv2.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            if(game.team2 != null) {
                tv2.setText(game.team2.name + "  \t" + game.team2pts);
            }
            if(game.team1pts > game.team2pts){
                tv1.setTextColor(getResources().getColor(R.color.colorPrimary));
            }
            else{
                tv2.setTextColor(getResources().getColor(R.color.colorPrimary));
            }
            if(tournament.getRound2().indexOf(game) %2 == 0){
                tv1.setBackgroundColor(Color.LTGRAY);
                tv2.setBackgroundColor(Color.LTGRAY);
            }

            secondRoundLl.addView(tv1);
            secondRoundLl.addView(tv2);
        }

        for(Game game : tournament.getSweetSixteen()){
            TextView tv1 = new TextView(this);
            tv1.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            tv1.setText(game.team1.name+"  \t"+game.team1pts);
            TextView tv2 = new TextView(this);
            tv2.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            tv2.setText(game.team2.name+"  \t"+game.team2pts);
            if(game.team1pts > game.team2pts){
                tv1.setTextColor(getResources().getColor(R.color.colorPrimary));
            }
            else{
                tv2.setTextColor(getResources().getColor(R.color.colorPrimary));
            }
            if(tournament.getSweetSixteen().indexOf(game) %2 == 0){
                tv1.setBackgroundColor(Color.LTGRAY);
                tv2.setBackgroundColor(Color.LTGRAY);
            }

            sweetSixteenLl.addView(tv1);
            sweetSixteenLl.addView(tv2);
        }

        for(Game game : tournament.getEliteEight()){
            TextView tv1 = new TextView(this);
            tv1.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            tv1.setText(game.team1.name+"  \t"+game.team1pts);
            TextView tv2 = new TextView(this);
            tv2.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            tv2.setText(game.team2.name+"  \t"+game.team2pts);
            if(game.team1pts > game.team2pts){
                tv1.setTextColor(getResources().getColor(R.color.colorPrimary));
            }
            else{
                tv2.setTextColor(getResources().getColor(R.color.colorPrimary));
            }
            if(tournament.getEliteEight().indexOf(game) %2 == 0){
                tv1.setBackgroundColor(Color.LTGRAY);
                tv2.setBackgroundColor(Color.LTGRAY);
            }

            eliteEightLl.addView(tv1);
            eliteEightLl.addView(tv2);
        }

        for(Game game : tournament.getFinalFour()){
            TextView tv1 = new TextView(this);
            tv1.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            tv1.setText(game.team1.name+"  \t"+game.team1pts);
            TextView tv2 = new TextView(this);
            tv2.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            tv2.setText(game.team2.name+"  \t"+game.team2pts);
            if(game.team1pts > game.team2pts){
                tv1.setTextColor(getResources().getColor(R.color.colorPrimary));
            }
            else{
                tv2.setTextColor(getResources().getColor(R.color.colorPrimary));
            }
            if(tournament.getFinalFour().indexOf(game) %2 == 0){
                tv1.setBackgroundColor(Color.LTGRAY);
                tv2.setBackgroundColor(Color.LTGRAY);
            }

            finalFourLl.addView(tv1);
            finalFourLl.addView(tv2);
        }

        Game game = tournament.getChampionship();
        TextView tv1 = new TextView(this);
        tv1.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        tv1.setText(game.team1.name+"  \t"+game.team1pts);
        TextView tv2 = new TextView(this);
        tv2.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        tv2.setText(game.team2.name+"  \t"+game.team2pts);
        if(game.team1pts > game.team2pts){
            tv1.setTextColor(getResources().getColor(R.color.colorPrimary));
        }
        else{
            tv2.setTextColor(getResources().getColor(R.color.colorPrimary));
        }

        championshipLl.addView(tv1);
        championshipLl.addView(tv2);


        TextView tv = new TextView(this);
        tv.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        tv.setText(tournament.getChampion().name);
        tv.setBackgroundColor(Color.YELLOW);

        championLl.addView(tv);

        backButton = findViewById(R.id.backButtonTournament);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TournamentSimActivity.this, MainActivity.class);
                intent.putExtra("model", (String)modelSpinner.getSelectedItem());
                intent.putExtra("year", season.getYear());
                startActivity(intent);
            }
        });
    }
}
