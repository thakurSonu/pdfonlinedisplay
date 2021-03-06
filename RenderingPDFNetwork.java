package com.sonu.pdf;

import android.content.Context;
import android.os.Environment;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.plugins.RxJavaPlugins;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RenderingPDFNetwork {

    private Context context;
    private boolean status = false;
    private CompositeDisposable disposable = new CompositeDisposable();


    private String filePath;

    public RenderingPDFNetwork(Context context, String endpoint) {
        this.context = context;
        this.filePath = "";
        disposable.clear();

        final DocquityService service = Api.getClient();
        Call<ResponseBody> call = service.streamFile(endpoint);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    writeResponseBodyToDisk(response.body());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }
        });
    }

    private boolean writeResponseBodyToDisk(final ResponseBody body) {
        disposable.add(Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(ObservableEmitter<String> e) throws Exception {
                try {
                    String root = Environment.getExternalStorageDirectory().toString();
                    final File pdfFolder = new File(root + "/Android/data/" + context.getPackageName());
                    pdfFolder.mkdirs();
                    String path = pdfFolder + "/" + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date())+".pdf";
                    pdfFolder.createNewFile();
                    InputStream inputStream = null;
                    OutputStream outputStream = null;
                    try {
                        byte[] fileReader = new byte[4096];

                        long fileSize = body.contentLength();
                        long fileSizeDownloaded = 0;

                        inputStream = body.byteStream();
                        outputStream = new FileOutputStream(path);

                        while (true) {
                            if (status) {
                                e.onComplete();
                            }
                            int read = inputStream.read(fileReader);
                            if (read == -1) {
                                break;
                            }
                            outputStream.write(fileReader, 0, read);
                            fileSizeDownloaded += read;
                            e.onNext(String.valueOf((int) ((fileSizeDownloaded * 100) / fileSize)));
                        }
                        outputStream.flush();

                    } catch (IOException ee) {
                        //  Log.e("data",e.getMessage());
                        e.tryOnError(ee);
                        e.onComplete();
                    } finally {
                        if (inputStream != null) {
                            inputStream.close();
                        }
                        if (outputStream != null) {
                            outputStream.close();
                        }
                    }
                    if (!status) {
                        filePath = path;
                        //File file = new File(path);
                        //new RenderingPDF(context, file, 1);
                        e.onComplete();
                    }
                } catch (IOException ee) {
                  //  e.tryOnError(ee);
                    RxJavaPlugins.onError(ee);
                    e.onComplete();
                }
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<String>() {
                    @Override
                    public void onNext(String s) {

                        PdfRender.progressData(Integer.parseInt(s));
                    }

                    @Override
                    public void onError(Throwable e) {
                        PdfRender.failedData(e);

                    }

                    @Override
                    public void onComplete() {
                        if(!Validation.isEmptyString(filePath))
                               PdfRender.setData(filePath);
                    }
                }));
        return true;
    }

    public void onDestory() {
        status = true;
    }
}

