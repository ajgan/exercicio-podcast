package br.ufpe.cin.if710.podcast.service;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Intent;

import br.ufpe.cin.if710.podcast.ui.MainActivity;

public class JobTimeService extends JobService {


    @Override
    public boolean onStartJob(JobParameters params) {

        Intent downloadService = new Intent(getApplicationContext(), XMLDownloadService.class);
        downloadService.putExtra("rss", MainActivity.RSS_FEED_PUBLIC);
        getApplicationContext().startService(downloadService);

        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return false;
    }
}
