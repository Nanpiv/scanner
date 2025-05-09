import 'dart:async';
import 'package:flutter/services.dart';

/// Scan mode which is either QR code or BARCODE
enum ScanMode { qr, barcode, def }

/// Provides access to the barcode scanner.
///
/// This class is an interface between the native Android and iOS classes and a
/// Flutter project.
class FlutterBarcodeScanner {
  static const MethodChannel _channel =
      MethodChannel('flutter_barcode_scanner');
  static const EventChannel _eventChannel =
      EventChannel('flutter_barcode_scanner_receiver');

  static Stream<String>? _onBarcodeReceiver;

  /// Scan with the camera until a barcode is identified, then return.
  ///
  /// Shows a scan line with [lineColor] over a scan window. A flash icon is
  /// displayed if [isShowFlashIcon] is true. The text of the cancel button can
  /// be customized with the [cancelButtonText] string.
  static Future<String> scanBarcode(
    String lineColor,
    String cancelButtonText,
    bool isShowFlashIcon,
    ScanMode scanMode,
  ) async {
    final params = <String, dynamic>{
      'lineColor': lineColor.isEmpty ? '#FF0000' : lineColor,
      'cancelButtonText':
          cancelButtonText.isEmpty ? 'Cancel' : cancelButtonText,
      'isShowFlashIcon': isShowFlashIcon,
      'isContinuousScan': false,
      'scanMode': scanMode.index,
    };

    try {
      final String? result = await _channel.invokeMethod('scanBarcode', params);
      return result ?? '';
    } on PlatformException catch (e) {
      return 'Failed to scan barcode: ${e.message}';
    }
  }

  /// Returns a continuous stream of barcode scans until the user cancels the
  /// operation.
  ///
  /// Shows a scan line with [lineColor] over a scan window. A flash icon is
  /// displayed if [isShowFlashIcon] is true. The text of the cancel button can
  /// be customized with the [cancelButtonText] string. Returns a stream of
  /// detected barcode strings.
  static Stream<String>? getBarcodeStreamReceiver(
    String lineColor,
    String cancelButtonText,
    bool isShowFlashIcon,
    ScanMode scanMode,
  ) {
    final params = <String, dynamic>{
      'lineColor': lineColor.isEmpty ? '#FF0000' : lineColor,
      'cancelButtonText':
          cancelButtonText.isEmpty ? 'Cancel' : cancelButtonText,
      'isShowFlashIcon': isShowFlashIcon,
      'isContinuousScan': true,
      'scanMode': scanMode.index,
    };

    try {
      _channel.invokeMethod('scanBarcode', params);
      _onBarcodeReceiver ??= _eventChannel
          .receiveBroadcastStream()
          .map((dynamic event) => event.toString());
      return _onBarcodeReceiver;
    } on PlatformException catch (e) {
      print('Failed to get barcode stream: ${e.message}');
      return null;
    }
  }
}
