package br.ufpe.cin.if710.podcast.service;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;

import java.io.IOException;

import br.ufpe.cin.if710.podcast.domain.ItemFeed;


public class PlayerService extends Service {

    public static final String EPISODE_PAUSE = "br.ufpe.cin.if710.podcast.action.PAUSE_EPISODE";
    public static final String EPISODE_OVER = "br.ufpe.cin.if710.podcast.action.EPISODE_OVER";

    private MediaPlayer myPlayer;
    private int myStartId;
    ItemFeed itemFeed;

    @Override
    public void onCreate() {
        super.onCreate();

        myPlayer = new MediaPlayer();

        if (myPlayer != null) {
            myPlayer.setLooping(false);

            myPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

                @Override
                public void onCompletion(MediaPlayer mp) {

                    Intent musicOver = new Intent(EPISODE_OVER);
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("Item", itemFeed);
                    musicOver.putExtras(bundle);
                    getApplicationContext().sendBroadcast(musicOver);
                    stopSelf(myStartId);
                }
            });
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (null != myPlayer) {
            myStartId = startId;

            Bundle params = intent.getExtras();
            itemFeed = (ItemFeed)params.get("Item");

            if (myPlayer.isPlaying()) {
                int timePaused = myPlayer.getCurrentPosition();
                itemFeed.setTimePaused(timePaused);

                myPlayer.reset();

                Intent musicPause = new Intent(EPISODE_PAUSE);
                Bundle bundle = new Bundle();
                bundle.putSerializable("Item", itemFeed);
                musicPause.putExtras(bundle);
                getApplicationContext().sendBroadcast(musicPause);
            }
            else {
                try {
                    myPlayer.setDataSource(this, Uri.parse(itemFeed.getFileUri()));
                    myPlayer.prepare();
                    myPlayer.seekTo(itemFeed.getTimePaused());
                    myPlayer.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (null != myPlayer) {
            myPlayer.stop();
            myPlayer.release();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
