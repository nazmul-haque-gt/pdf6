# PDF Editor – Android app (GitHub build)

1. Create a new GitHub repo and upload everything in this folder (keep the `.github` folder).
2. Push to `main` (or Actions tab → "Build Android APK" → Run workflow).
3. When it finishes, open the run → **Artifacts** → download `pdf-editor-debug-apk`, unzip, install `app-debug.apk` on your phone.

The editor is `www/index.html`. Edit it and push to rebuild.
Saved PDFs open the Android share sheet (Save to Files / Drive / etc.).

## Open PDFs directly from your phone
After installing the APK, tap any PDF in Files / WhatsApp / Gmail etc. -> choose **PDF Editor** -> **Always** to make it the default.
(The `android-patch` folder adds the PDF intent filter and the native handler during the GitHub build - keep it in the repo.)
