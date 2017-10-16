package br.ufpe.cin.if710.podcast.db;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

public class PodcastProvider extends ContentProvider {

    PodcastDBHelper myHelper;

    public PodcastProvider() {
    }

    @Override
    public boolean onCreate() {
        myHelper = PodcastDBHelper.getInstance(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        return myHelper.getReadableDatabase().query(PodcastDBHelper.DATABASE_TABLE, projection, selection, selectionArgs, null, null, sortOrder);
    }


    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Implement this to handle requests to delete one or more rows.
        return myHelper.getWritableDatabase().delete(PodcastDBHelper.DATABASE_TABLE, selection,selectionArgs);
    }

    @Override
    public String getType(Uri uri) {
        // TODO: Implement this to handle requests for the MIME type of the data
        // at the given EPISODE_FILE_URI.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        long id = myHelper.getWritableDatabase().insert(PodcastDBHelper.DATABASE_TABLE, null, values);
        return Uri.withAppendedPath(PodcastProviderContract.EPISODE_LIST_URI, Long.toString(id));
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        return myHelper.getWritableDatabase().update(PodcastDBHelper.DATABASE_TABLE, values, selection, selectionArgs);
    }

}
