package br.ufpe.cin.if710.podcast.service;

import android.app.IntentService;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;

import org.xmlpull.v1.XmlPullParserException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import br.ufpe.cin.if710.podcast.db.PodcastProviderContract;
import br.ufpe.cin.if710.podcast.domain.ItemFeed;
import br.ufpe.cin.if710.podcast.domain.XmlFeedParser;

public class XMLDownloadService extends IntentService {

    public static final String XMLDOWNLOADED = "br.ufpe.cin.if710.podcast.action.XMLDOWNLOADED";

    public XMLDownloadService() {
        super("XMLDownloadService");
    }

    //TODO Opcional - pesquise outros meios de obter arquivos da internet
    private String getRssFeed(String feed) throws IOException {
        InputStream in = null;
        String rssFeed = "";
        try {
            URL url = new URL(feed);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            in = conn.getInputStream();
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            for (int count; (count = in.read(buffer)) != -1; ) {
                out.write(buffer, 0, count);
            }
            byte[] response = out.toByteArray();
            rssFeed = new String(response, "UTF-8");
        } finally {
            if (in != null) {
                in.close();
            }
        }
        return rssFeed;
    }

    @Override
    public void onHandleIntent(Intent i) {

        Bundle params = i.getExtras();
        String rss = (String) params.get("rss");

        List<ItemFeed> itemList = new ArrayList<>();
        try {
            itemList = XmlFeedParser.parse(getRssFeed(rss));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        }

        boolean atualizou = false;

        ContentResolver cr;
        ContentValues cv = new ContentValues();

        for (ItemFeed item : itemList) {
            ContentResolver crAux = getContentResolver();
            String selection = PodcastProviderContract.EPISODE_TITLE + " = ?";
            String[] selectionArgs = new String[]{item.getTitle()};
            Cursor c = crAux.query(PodcastProviderContract.EPISODE_LIST_URI, null, selection, selectionArgs, null);

            atualizou = atualizou || (c.getCount() == 0);

            if (c.getCount() == 0) {
                cr = getContentResolver();
                cv.put(PodcastProviderContract.EPISODE_TITLE, item.getTitle());
                cv.put(PodcastProviderContract.EPISODE_LINK, item.getLink());
                cv.put(PodcastProviderContract.EPISODE_DATE, item.getPubDate());
                cv.put(PodcastProviderContract.EPISODE_DESC, item.getDescription());
                cv.put(PodcastProviderContract.EPISODE_DOWNLOAD_LINK, item.getDownloadLink());
                cv.put(PodcastProviderContract.EPISODE_FILE_URI, "");
                cv.put(PodcastProviderContract.EPISODE_TIME_PAUSED, "0");
                cr.insert(PodcastProviderContract.EPISODE_LIST_URI, cv);
            }
        }

        Intent downloadXmlComplete = new Intent(XMLDOWNLOADED);
        downloadXmlComplete.putExtra("atualizou", atualizou);
        getApplicationContext().sendBroadcast(downloadXmlComplete);
    }
}
