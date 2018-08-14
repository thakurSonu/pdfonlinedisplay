package com.sonu.pdf;

import android.graphics.Bitmap;

public interface OnResultListener  {
    void resultData(String pdfPath);
    void progressData(int progress);
    void failed(Throwable t);
}