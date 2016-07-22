package com.ryanpotsander.androidcv;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.provider.MediaStore;

import java.io.File;
import java.util.Calendar;

/**
 * Created by Ryan on 7/18/16.
 */
public class PreviewObject {
    File file;

    private static final String dummyPath = "dummy/path"; // TODO remove

    String path;

    Bitmap preview;

    String label;

    public PreviewObject(String path){

        this.path = path;

        label = Calendar.getInstance().getTime().toString();

        if (!path.equals(dummyPath)) preview = ThumbnailUtils.createVideoThumbnail(path, MediaStore.Video.Thumbnails.MICRO_KIND);

    }

    public String getLabel() {
        return label;
    }

    public Bitmap getPreview() {
        return preview;
    }

    public String getPath() {
        return path;
    }

    public void setPreview(Bitmap img) {
        preview = img;
    }
}
