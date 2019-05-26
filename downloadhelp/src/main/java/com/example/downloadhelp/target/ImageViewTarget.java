package com.example.downloadhelp.target;

import android.graphics.Bitmap;
import android.widget.ImageView;
import com.example.downloadhelp.request.RequestOptions;

public class ImageViewTarget implements Target<Bitmap> {

    private ImageView imageView;

    public ImageViewTarget(ImageView imageView) {
        this.imageView = imageView;
    }

    @Override
    public void onReady(RequestOptions options, Bitmap bitmap) {
        imageView.setImageBitmap(bitmap);
    }

    public ImageView getImageView() {
        return imageView;
    }

    public void setImageView(ImageView imageView) {
        this.imageView = imageView;
    }

}
