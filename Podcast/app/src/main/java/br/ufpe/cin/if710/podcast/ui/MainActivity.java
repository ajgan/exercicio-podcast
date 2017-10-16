package br.ufpe.cin.if710.podcast.ui;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;

import br.ufpe.cin.if710.podcast.R;
import br.ufpe.cin.if710.podcast.db.PodcastProviderContract;
import br.ufpe.cin.if710.podcast.domain.ItemFeed;
import br.ufpe.cin.if710.podcast.service.XMLDownloadService;
import br.ufpe.cin.if710.podcast.ui.adapter.XmlFeedAdapter;

public class MainActivity extends Activity {

    //ao fazer envio da resolucao, use este link no seu codigo!
    private final String RSS_FEED = "http://leopoldomt.com/if710/fronteirasdaciencia.xml";


    //TODO teste com outros links de podcast

    private static final int MY_NOTIFICATION_ID = 5;

    public static final String DOWNLOAD_COMPLETE = "br.ufpe.cin.if710.podcast.action.DOWNLOAD_COMPLETE";
    public static final String DOWNLOAD_XML_COMPLETE = "br.ufpe.cin.if710.podcast.action.XMLDOWNLOADED";
    public static final String PAUSE_EPISODE = "br.ufpe.cin.if710.podcast.action.PAUSE_EPISODE";
    public static final String END_EPISODE = "br.ufpe.cin.if710.podcast.action.END_EPISODE";
    public static final String RSS_FEED_PUBLIC = "http://leopoldomt.com/if710/fronteirasdaciencia.xml";

    private ListView items;
    boolean isPrimeiroPlano = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        items = (ListView) findViewById(R.id.items);
        items.setTextFilterEnabled(true);
        items.setClickable(true);

        items.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                XmlFeedAdapter adapter = (XmlFeedAdapter) parent.getAdapter();
                ItemFeed itemFeed = adapter.getItem(position);

                Intent i = new Intent(getApplicationContext(), EpisodeDetailActivity.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable("Item", itemFeed);
                i.putExtras(bundle);
                startActivity(i);
            }
        });
    }

    private void tela(){

        Cursor c = getContentResolver().query(PodcastProviderContract.EPISODE_LIST_URI, null, null, null, null);
        ArrayList<ItemFeed> itemFeed = new ArrayList<>();
        while (c.moveToNext()) {
            String title = c.getString(c.getColumnIndex(PodcastProviderContract.EPISODE_TITLE));
            String link = c.getString(c.getColumnIndex(PodcastProviderContract.EPISODE_LINK));
            String pubDate = c.getString(c.getColumnIndex(PodcastProviderContract.EPISODE_DATE));
            String description = c.getString(c.getColumnIndex(PodcastProviderContract.EPISODE_DESC));
            String downloadLink = c.getString(c.getColumnIndex(PodcastProviderContract.EPISODE_DOWNLOAD_LINK));
            String uriString = c.getString(c.getColumnIndex(PodcastProviderContract.EPISODE_FILE_URI));

            ItemFeed item = new ItemFeed(title, link, pubDate, description, downloadLink);
            item.setFileUri(uriString);
            itemFeed.add(item);
        }

        //Adapter Personalizado
        XmlFeedAdapter adapter = new XmlFeedAdapter(getApplicationContext(), R.layout.itemlista, itemFeed);

        //atualizar o list view
        items.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            startActivity(new Intent(this,SettingsActivity.class));
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        isPrimeiroPlano = true;
        super.onResume();
    }

    @Override
    protected void onPause() {
        isPrimeiroPlano = false;
        super.onPause();
    }

    @Override
    protected void onStart() {
        isPrimeiroPlano = true;
        super.onStart();

        tela();
        registerReceiver(downloadCompleto, new IntentFilter(DOWNLOAD_COMPLETE));
        registerReceiver(pauseEpisode, new IntentFilter(PAUSE_EPISODE));
        registerReceiver(endEpisode, new IntentFilter(END_EPISODE));
        registerReceiver(downloadXMLComplete, new IntentFilter(DOWNLOAD_XML_COMPLETE));
        Intent downloadXMLService = new Intent(getApplicationContext(), XMLDownloadService.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable("rss", RSS_FEED);
        downloadXMLService.putExtras(bundle);
        getApplicationContext().startService(downloadXMLService);
    }

    @Override
    protected void onStop() {
        isPrimeiroPlano = false;
        super.onStop();
        XmlFeedAdapter adapter = (XmlFeedAdapter) items.getAdapter();
        adapter.clear();
    }

    @Override
    protected void onDestroy() {
        isPrimeiroPlano = false;
        super.onDestroy();
        unregisterReceiver(downloadCompleto);
        unregisterReceiver(pauseEpisode);
        unregisterReceiver(endEpisode);
        unregisterReceiver(downloadXMLComplete);
    }

    BroadcastReceiver downloadCompleto = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            Toast.makeText(context, "Download Completo", Toast.LENGTH_SHORT).show();

            Bundle params = intent.getExtras();
            ItemFeed itemFeed = (ItemFeed)params.get("Item");

            ContentResolver cr = getContentResolver();
            ContentValues cv = new ContentValues();
            cv.put(PodcastProviderContract.EPISODE_FILE_URI, params.get("uri").toString());
            String selection = PodcastProviderContract.EPISODE_TITLE + " = ?";
            String[] selectionArgs = new String[]{itemFeed.getTitle()};
            cr.update(PodcastProviderContract.EPISODE_LIST_URI, cv, selection, selectionArgs);
            downloadNotification(true, "Download Realizado com Sucesso!", "");
        }
    };

    BroadcastReceiver pauseEpisode = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {

            Bundle params = intent.getExtras();
            ItemFeed itemFeed = (ItemFeed)params.get("Item");

            ContentResolver cr = getContentResolver();
            ContentValues cv = new ContentValues();
            cv.put(PodcastProviderContract.EPISODE_TIME_PAUSED, itemFeed.getTimePaused());
            String selection = PodcastProviderContract.EPISODE_TITLE + " = ?";
            String[] selectionArgs = new String[]{itemFeed.getTitle()};
            cr.update(PodcastProviderContract.EPISODE_LIST_URI, cv, selection, selectionArgs);
            tela();
        }
    };

    BroadcastReceiver endEpisode = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {

            Bundle params = intent.getExtras();
            ItemFeed itemFeed = (ItemFeed)params.get("Item");

            File root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            root.mkdirs();
            Uri uri = Uri.parse(itemFeed.getDownloadLink());
            File output = new File(root, uri.getLastPathSegment());
            if (output.exists()) {
                output.delete();
            }

            ContentResolver cr = getContentResolver();
            ContentValues cv = new ContentValues();
            cv.put(PodcastProviderContract.EPISODE_TIME_PAUSED, 0);
            cv.put(PodcastProviderContract.EPISODE_FILE_URI, "");
            String selection = PodcastProviderContract.EPISODE_TITLE + " = ?";
            String[] selectionArgs = new String[]{itemFeed.getTitle()};
            cr.update(PodcastProviderContract.EPISODE_LIST_URI, cv, selection, selectionArgs);
            tela();
        }
    };

    BroadcastReceiver downloadXMLComplete = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {

            Bundle params = intent.getExtras();
            boolean atualizou = (Boolean) params.get("atualizou");

            downloadNotification(atualizou, "Lista de Podcasts Atualizada", "");
        }
    };

    private void downloadNotification(Boolean atualizar, String title, String text){
        View rootView = getWindow().getDecorView().getRootView();

        if (rootView.isShown()){
            if (atualizar){
                tela();
            }
        }else {
            final Intent notificationIntent = new Intent(getApplicationContext(), MainActivity.class);
            final PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, notificationIntent, 0);

            final Notification notification = new Notification.Builder(
                    getApplicationContext())
                    .setSmallIcon(android.R.drawable.btn_star)
                    .setAutoCancel(true)
                    .setOngoing(true).setContentTitle(title)
                    .setContentText(text)
                    .setContentIntent(pendingIntent)
                    .build();

            NotificationManager notificationManager = (NotificationManager)getApplicationContext().getSystemService(NOTIFICATION_SERVICE);
            notificationManager.notify(MY_NOTIFICATION_ID, notification);
        }
    }
}
