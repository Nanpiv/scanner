package com.amolg.flutterbarcodescanner;

import android.app.Activity;
import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;
import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.EventChannel;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry;

public class FlutterBarcodeScannerPlugin implements 
    FlutterPlugin, 
    ActivityAware, 
    MethodChannel.MethodCallHandler, 
    EventChannel.StreamHandler {

    private MethodChannel channel;
    private EventChannel eventChannel;
    private Activity activity;
    private ActivityPluginBinding activityBinding;
    private Result pendingResult;
    private final int RC_BARCODE_CAPTURE = 9001;

    @Override
    public void onAttachedToEngine(@NonNull FlutterPluginBinding binding) {
        channel = new MethodChannel(binding.getBinaryMessenger(), "flutter_barcode_scanner");
        channel.setMethodCallHandler(this);
        
        eventChannel = new EventChannel(binding.getBinaryMessenger(), "flutter_barcode_scanner_receiver");
        eventChannel.setStreamHandler(this);
    }

    @Override
    public void onAttachedToActivity(@NonNull ActivityPluginBinding binding) {
        activity = binding.getActivity();
        activityBinding = binding;
        
        binding.addActivityResultListener((requestCode, resultCode, data) -> {
            if (requestCode == RC_BARCODE_CAPTURE) {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    String barcode = data.getStringExtra("barcode_result");
                    if (pendingResult != null) {
                        pendingResult.success(barcode);
                        pendingResult = null;
                    }
                } else {
                    if (pendingResult != null) {
                        pendingResult.success("");
                        pendingResult = null;
                    }
                }
                return true;
            }
            return false;
        });
    }

    @Override
    public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
        if (call.method.equals("scanBarcode")) {
            pendingResult = result;
            startBarcodeScannerActivity(
                call.argument("lineColor"),
                call.argument("cancelButtonText"),
                call.argument("isShowFlashIcon"),
                call.argument("isContinuousScan"),
                call.argument("scanMode")
            );
        } else {
            result.notImplemented();
        }
    }

    private void startBarcodeScannerActivity(
        String lineColor, 
        String cancelButtonText, 
        Boolean isShowFlashIcon,
        Boolean isContinuousScan,
        Integer scanMode
    ) {
        Intent intent = new Intent(activity, BarcodeCaptureActivity.class);
        intent.putExtra("line_color", lineColor);
        intent.putExtra("cancel_button_text", cancelButtonText);
        intent.putExtra("is_show_flash_icon", isShowFlashIcon);
        intent.putExtra("is_continuous_scan", isContinuousScan);
        intent.putExtra("scan_mode", scanMode);
        activity.startActivityForResult(intent, RC_BARCODE_CAPTURE);
    }

    @Override
    public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
        channel.setMethodCallHandler(null);
        channel = null;
        eventChannel.setStreamHandler(null);
        eventChannel = null;
    }

    @Override
    public void onDetachedFromActivity() {
        activity = null;
        if (activityBinding != null) {
            activityBinding = null;
        }
    }

    @Override
    public void onReattachedToActivityForConfigChanges(@NonNull ActivityPluginBinding binding) {
        onAttachedToActivity(binding);
    }

    @Override
    public void onDetachedFromActivityForConfigChanges() {
        onDetachedFromActivity();
    }

    @Override
    public void onListen(Object arguments, EventChannel.EventSink events) {
        // Handle continuous scanning events
    }

    @Override
    public void onCancel(Object arguments) {
        // Clean up resources
    }
}