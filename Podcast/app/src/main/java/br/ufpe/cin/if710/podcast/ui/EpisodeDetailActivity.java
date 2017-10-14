package br.ufpe.cin.if710.podcast.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import br.ufpe.cin.if710.podcast.R;


public class EpisodeDetailActivity extends Activity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_episode_detail);

        TextView epTitle = (TextView) findViewById(R.id.epTitle);
        TextView epDesc = (TextView) findViewById(R.id.epDesc);
        TextView epPubDate = (TextView) findViewById(R.id.epPubDate);
        TextView epLink = (TextView) findViewById(R.id.epLink);

        Intent myIntent = getIntent();
        epTitle.setText(myIntent.getExtras().getString("title"));
        epDesc.setText(myIntent.getExtras().getString("description"));
        epPubDate.setText(myIntent.getExtras().getString("date"));
        epLink.setText(myIntent.getExtras().getString("link"));

    }
}
