import re, sys
p = 'android/app/src/main/AndroidManifest.xml'
s = open(p, encoding='utf-8').read()
if 'application/pdf' in s:
    print('manifest already patched'); sys.exit(0)
filters = '''
            <intent-filter>
                <action android:name="android.intent.action.VIEW" />
                <category android:name="android.intent.category.DEFAULT" />
                <category android:name="android.intent.category.BROWSABLE" />
                <data android:scheme="content" android:mimeType="application/pdf" />
                <data android:scheme="file" android:mimeType="application/pdf" />
            </intent-filter>
            <intent-filter>
                <action android:name="android.intent.action.SEND" />
                <category android:name="android.intent.category.DEFAULT" />
                <data android:mimeType="application/pdf" />
            </intent-filter>
'''
# insert right before the closing tag of the MainActivity <activity> element
i = s.index('</activity>')
s = s[:i] + filters + '        ' + s[i:]
open(p, 'w', encoding='utf-8').write(s)
print('manifest patched')
