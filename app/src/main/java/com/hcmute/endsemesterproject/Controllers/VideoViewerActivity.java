package com.hcmute.endsemesterproject.Controllers;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.VideoView;

import com.hcmute.endsemesterproject.R;

public class VideoViewerActivity extends AppCompatActivity {

    private VideoView videoViewer;
    private String videoUrl;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_viewer);

        initializeControllers();
        displayVideo(videoViewer, videoUrl, VideoViewerActivity.this);
    }

    private void displayVideo(VideoView videoViewer, String videoUrl, Context context) {
        videoViewer.setVideoURI(Uri.parse(videoUrl));

        MediaController mediaController = new MediaController(context);
        videoViewer.setMediaController(mediaController);
        mediaController.setAnchorView(videoViewer);
    }

    private void initializeControllers() {
        videoViewer = (VideoView) findViewById(R.id.video_viewer);
        videoUrl = getIntent().getStringExtra("url");
    }
}