package com.amolg.flutterbarcodescanner;

public interface BarcodeResultListener {
    void onBarcodeResult(String barcode);
    void onError(String error);
    void onCancel();
}