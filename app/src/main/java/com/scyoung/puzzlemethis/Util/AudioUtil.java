package com.scyoung.puzzlemethis.Util;

import android.content.Context;
import android.net.Uri;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class AudioUtil {
    public static File saveAudioToInternalStorage(String outputFilename, Uri audioUri, Context context) {
        FileOutputStream out;
        File buttonAudioFile = null;
        outputFilename += DateUtil.getDateString();
        try {
            buttonAudioFile = new File(context.getFilesDir(), outputFilename);
            InputStream in = context.getContentResolver().openInputStream(audioUri);
            out = new FileOutputStream(buttonAudioFile);
            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            in.close();

            // write the output file
            out.flush();
            out.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return buttonAudioFile;
    }
}
