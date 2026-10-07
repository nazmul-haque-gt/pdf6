package com.pdfeditor.app;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import com.getcapacitor.BridgeActivity;
import org.json.JSONObject;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

/** Lets Android hand a tapped PDF ("Open with" / default app) to the web editor. */
public class MainActivity extends BridgeActivity {
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    handleIntent(getIntent());
  }

  @Override
  protected void onNewIntent(Intent intent) {
    super.onNewIntent(intent);
    setIntent(intent);
    handleIntent(intent);
  }

  private void handleIntent(Intent intent) {
    if (intent == null) return;
    String action = intent.getAction();
    Uri uri = null;
    if (Intent.ACTION_VIEW.equals(action)) uri = intent.getData();
    else if (Intent.ACTION_SEND.equals(action)) uri = (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);
    if (uri == null) return;
    // mark as handled so a rotation / recreate doesn't open it again
    intent.setAction(Intent.ACTION_MAIN);
    intent.setData(null);
    final Uri src = uri;
    new Thread(new Runnable() {
      @Override public void run() { copyAndDispatch(src); }
    }).start();
  }

  private String displayName(Uri uri) {
    String name = null;
    try (Cursor c = getContentResolver().query(uri, null, null, null, null)) {
      if (c != null && c.moveToFirst()) {
        int i = c.getColumnIndex(OpenableColumns.DISPLAY_NAME);
        if (i >= 0) name = c.getString(i);
      }
    } catch (Exception e) { /* ignore */ }
    if (name == null || name.isEmpty()) name = uri.getLastPathSegment();
    if (name == null || name.isEmpty()) name = "document.pdf";
    if (!name.toLowerCase().endsWith(".pdf")) name = name + ".pdf";
    return name;
  }

  private void copyAndDispatch(Uri uri) {
    try {
      File dir = getCacheDir();
      File[] old = dir.listFiles();
      if (old != null) for (File f : old) if (f.getName().startsWith("open_") && f.getName().endsWith(".pdf")) f.delete();
      final long id = System.currentTimeMillis();
      File out = new File(dir, "open_" + id + ".pdf");
      try (InputStream in = getContentResolver().openInputStream(uri);
           FileOutputStream os = new FileOutputStream(out)) {
        byte[] buf = new byte[65536];
        int n;
        while ((n = in.read(buf)) > 0) os.write(buf, 0, n);
      }
      final String json = "{id:" + id + ",name:" + JSONObject.quote(displayName(uri))
          + ",path:" + JSONObject.quote(out.getAbsolutePath()) + "}";
      final String js = "if(window.__lastIntentId!==" + id + "){window.__pendingIntentPdf=" + json
          + ";if(window.__openIntentPdf)window.__openIntentPdf();}";
      // The page may still be loading (cold start), so try a few times; the id check makes repeats harmless.
      final int[] delays = {0, 600, 1500, 3000, 5000};
      for (final int d : delays) {
        runOnUiThread(new Runnable() {
          @Override public void run() {
            if (getBridge() == null || getBridge().getWebView() == null) return;
            getBridge().getWebView().postDelayed(new Runnable() {
              @Override public void run() { getBridge().getWebView().evaluateJavascript(js, null); }
            }, d);
          }
        });
      }
    } catch (Exception e) { /* could not read the file */ }
  }
}
