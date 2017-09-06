adb shell mkdir /sdcard/Android/data/com.wistron.generic.pqaa/files/pqaa_config
adb push config /sdcard/Android/data/com.wistron.generic.pqaa/files/pqaa_config/
adb install app/generic_pqaa-release.apk
