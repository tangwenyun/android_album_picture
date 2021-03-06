package com.example.android_album_picture;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.lang.ref.WeakReference;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry;

/** AndroidAlbumPicturePlugin */
public class AndroidAlbumPicturePlugin implements FlutterPlugin, MethodCallHandler, ActivityAware {

  private static final String CHANEL = "android_album_picture";

  private MethodChannel channel;
  String[] mPermissionList = new String[]{
          Manifest.permission.WRITE_EXTERNAL_STORAGE,
          Manifest.permission.READ_EXTERNAL_STORAGE};
  public static final int REQUEST_PICK_IMAGE = 11101;
  String path;
  private WeakReference<Activity> mActivity;

  @Override
  public void onDetachedFromActivityForConfigChanges() {
    mActivity = null;
  }

  @Override
  public void onReattachedToActivityForConfigChanges(@NonNull ActivityPluginBinding binding) {
    onAttachedToActivity(binding);
  }

  @Override
  public void onDetachedFromActivity() { }

  @Override
  public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
    channel.setMethodCallHandler(null);
  }

  //???????????????????????????????????????
  @Override
  public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
    channel = new MethodChannel(flutterPluginBinding.getBinaryMessenger(), CHANEL);
    channel.setMethodCallHandler(this);
  }

  void openAlbum(@NonNull MethodCall call, @NonNull Result result) {
    Activity activity = mActivity.get();
    ActivityCompat.requestPermissions(activity, mPermissionList, 100);
    result.success("??????????????????!");
  }

  @Override
  public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
    switch (call.method) {
      case "openCamera":
        openCamera(call, result);
        break;
      case "openAlbum":
        openAlbum(call, result);
        break;
      default:
        result.notImplemented();
        break;
    }
  }

  private void getImage() {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
      mActivity.get().startActivityForResult(new Intent(Intent.ACTION_GET_CONTENT).setType("image/*"),
              REQUEST_PICK_IMAGE);
    } else {
      Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
      intent.addCategory(Intent.CATEGORY_OPENABLE);
      intent.setType("image/*");
      mActivity.get().startActivityForResult(intent, REQUEST_PICK_IMAGE);
    }
  }

  void openCamera(@NonNull MethodCall call, @NonNull Result result) {
    Activity activity = mActivity.get();
    path = (String) call.arguments;
    Intent takePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
    if (activity.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) {
      activity.startActivityForResult(takePhotoIntent, 1);
    } else {
      System.out.println("????????????");
    }
    result.success("????????????");
  }

  @Override
  public void onAttachedToActivity(@NonNull ActivityPluginBinding binding) {
    this.mActivity = new WeakReference<>(binding.getActivity());
    binding.addRequestPermissionsResultListener((requestCode, permissions, grantResults) -> {
      switch (requestCode) {
        case 100:
          boolean writeExternalStorage = grantResults[0] == PackageManager.PERMISSION_GRANTED;
          boolean readExternalStorage = grantResults[1] == PackageManager.PERMISSION_GRANTED;
          if (grantResults.length > 0 && writeExternalStorage && readExternalStorage) {
            getImage();
          } else {
            Toast.makeText(mActivity.get(), "?????????????????????", Toast.LENGTH_SHORT).show();
          }
          break;
      }
      return false;
    });
    binding.addActivityResultListener((requestCode, resultCode, data) -> {
      if (requestCode == 1 && resultCode == -1) {
        Bitmap bitmap = (Bitmap) data.getExtras().get("data");
        FileOutputStream ops;
        try {
          ops = new FileOutputStream(path);
          bitmap.compress(Bitmap.CompressFormat.JPEG, 100, ops);
        } catch (FileNotFoundException e) {
          e.printStackTrace();
        }
        channel.invokeMethod("callback_photo", path);
      }
      if (resultCode == Activity.RESULT_OK) {
        switch (requestCode) {
          case REQUEST_PICK_IMAGE:
            if (data != null) {
              path = RealPathFromUriUtils.getRealPathFromUri(mActivity.get(), data.getData());
            } else {
              Toast.makeText(mActivity.get(), "??????????????????????????????", Toast.LENGTH_SHORT).show();
            }
            break;
        }
        channel.invokeMethod("callback_photo", path);
      }
      return false;
    });
  }

  //???????????????????????????????????????
  public static void registerWith(PluginRegistry.Registrar registrar) {
    final MethodChannel channel = new MethodChannel(registrar.messenger(), CHANEL);
    channel.setMethodCallHandler(new com.example.android_album_picture.AndroidAlbumPicturePlugin().initPlugin(channel, registrar));
  }

  public com.example.android_album_picture.AndroidAlbumPicturePlugin initPlugin(MethodChannel methodChannel, PluginRegistry.Registrar registrar) {
    channel = methodChannel;
    mActivity = new WeakReference<>(registrar.activity());
    return this;
  }

  // ??????????????????????????????
  static class RealPathFromUriUtils {
    /**
     * ??????Uri???????????????????????????
     *
     * @param context ???????????????
     * @param uri     ?????????Uri
     * @return ??????Uri?????????????????????, ????????????????????????????????????, ????????????null
     */
    public static String getRealPathFromUri(Context context, Uri uri) {
      int sdkVersion = Build.VERSION.SDK_INT;
      if (sdkVersion >= 19) { // api >= 19
        return getRealPathFromUriAboveApi19(context, uri);
      } else { // api < 19
        return getRealPathFromUriBelowAPI19(context, uri);
      }
    }

    /**
     * ??????api19??????(?????????api19),??????uri???????????????????????????
     *
     * @param context ???????????????
     * @param uri     ?????????Uri
     * @return ??????Uri?????????????????????, ????????????????????????????????????, ????????????null
     */
    private static String getRealPathFromUriBelowAPI19(Context context, Uri uri) {
      return getDataColumn(context, uri, null, null);
    }

    /**
     * ??????api19?????????,??????uri???????????????????????????
     *
     * @param context ???????????????
     * @param uri     ?????????Uri
     * @return ??????Uri?????????????????????, ????????????????????????????????????, ????????????null
     */
    @SuppressLint("NewApi")
    private static String getRealPathFromUriAboveApi19(Context context, Uri uri) {
      String filePath = null;
      if (DocumentsContract.isDocumentUri(context, uri)) {
        // ?????????document????????? uri, ?????????document id???????????????
        String documentId = DocumentsContract.getDocumentId(uri);
        if (isMediaDocument(uri)) { // MediaProvider
          // ??????':'??????
          String id = documentId.split(":")[1];

          String selection = MediaStore.Images.Media._ID + "=?";
          String[] selectionArgs = {id};
          filePath = getDataColumn(context, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, selection, selectionArgs);
        } else if (isDownloadsDocument(uri)) { // DownloadsProvider
          Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(documentId));
          filePath = getDataColumn(context, contentUri, null, null);
        }
      } else if ("content".equalsIgnoreCase(uri.getScheme())) {
        // ????????? content ????????? Uri
        filePath = getDataColumn(context, uri, null, null);
      } else if ("file".equals(uri.getScheme())) {
        // ????????? file ????????? Uri,?????????????????????????????????
        filePath = uri.getPath();
      }
      return filePath;
    }

    /**
     * ???????????????????????? _data ???????????????Uri?????????????????????
     *
     * @return
     */
    private static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
      String path = null;

      String[] projection = new String[]{MediaStore.Images.Media.DATA};
      Cursor cursor = null;
      try {
        cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
        if (cursor != null && cursor.moveToFirst()) {
          int columnIndex = cursor.getColumnIndexOrThrow(projection[0]);
          path = cursor.getString(columnIndex);
        }
      } catch (Exception e) {
        if (cursor != null) {
          cursor.close();
        }
      }
      return path;
    }

    /**
     * @param uri the Uri to check
     * @return Whether the Uri authority is MediaProvider
     */
    private static boolean isMediaDocument(Uri uri) {
      return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri the Uri to check
     * @return Whether the Uri authority is DownloadsProvider
     */
    private static boolean isDownloadsDocument(Uri uri) {
      return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }
  }

}
