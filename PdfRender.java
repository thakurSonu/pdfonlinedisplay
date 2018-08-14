package com.sonu.pdf;

import android.content.Context;
import android.graphics.Bitmap;

public class PdfRender {
    static OnResultListener onResultListener;
    static RenderingPDFNetwork renderingPDFNetwork;
    private Context context;


    public PdfRender(Context context) {
        this.context = context;
    }


    public static void cancle() {
        if (renderingPDFNetwork != null) {
            renderingPDFNetwork.onDestory();
        }
    }

    public static String setData(String  pdfPath) {
        onResultListener.resultData(pdfPath);
        return pdfPath;
    }



    public static Throwable failedData(Throwable t) {
        onResultListener.failed(t);
        return t;
    }

    public static int progressData(int progress) {
        onResultListener.progressData(progress);
        return progress;
    }

    public void initFromNetwork(String endpoint, OnResultListener resultListener) {
        onResultListener = resultListener;
        renderingPDFNetwork = new RenderingPDFNetwork(context, endpoint);
    }

}
