package com.example.statslam;

import android.content.Context;
import android.content.ContextWrapper;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;


public class DirectoryManager
{
    private static boolean isExternalStorageReadable = false;
    private static boolean isExternalStorageWritable = false;




    public static File getTempStorageDir(Context context, String baseFileName) {
        ContextWrapper cw = new ContextWrapper(context.getApplicationContext());
        File dir = new File(cw.getFilesDir().getAbsolutePath()+"/"+baseFileName);
        if(!dir.exists()){
            dir.mkdir();
        }
        return dir;
    }
    public static File getDirectory(String dirPath)
    {
        File dir = new File(dirPath);
        if(!dir.exists()){
            Log.d("DM","CREATING DIRECTORY "+dirPath);
            try {
                Log.d("DM", "MKDIR="+dir.mkdir());
            }
            catch(Exception e) {
                e.printStackTrace();
            }
        }
        return dir;
    }

    public static File CACHE_DIR(Context context){ return getTempStorageDir(context, "cache");}
    public static File MODELS_DIR(Context context){ return getTempStorageDir(context, "models");}

    /* Checks if external storage is available to at least read */
    public static boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) || Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            isExternalStorageReadable = true;
            return true;
        }
        isExternalStorageReadable = false;
        return false;
    }

    /* Checks if external storage is available for read and write */
    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            isExternalStorageWritable = true;
            return true;
        }
        isExternalStorageWritable = false;
        return false;
    }



    public static void deleteRecursive(File fileOrDirectory) {

        Log.d("DELETING DIRECTORY", fileOrDirectory.getName());

        if (fileOrDirectory.isDirectory()) {
            for (File child : fileOrDirectory.listFiles()) {
                deleteRecursive(child);
            }
        }

        fileOrDirectory.delete();
    }

    public static void copy(String inputPath, String inputFile, String outputPath) throws IOException {

        InputStream in = null;
        OutputStream out = null;
        inputPath += "/";
        outputPath += "/";
        try {

            //create output directory if it doesn't exist
            File dir = new File (outputPath);
            if (!dir.exists())
            {
                dir.mkdirs();
            }
            //Otherwise we don't need to do anything
            /*else{
                return;
            }*/


            in = new FileInputStream(inputPath + inputFile);
            out = new FileOutputStream(outputPath + inputFile);

            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            in.close();
            in = null;

            // write the output file (You have now copied the file)
            out.flush();
            out.close();
            out = null;

        }  catch (FileNotFoundException fnfe1) {
            Log.e("FILE NOT FOUND", fnfe1.getMessage());
        }
        catch (Exception e) {
            Log.e("FILE ERROR", e.getMessage());
        }
    }

    public static void copyFileUsingStream(File source, File dest) throws IOException {
        InputStream is = null;
        OutputStream os = null;
        try {
            is = new FileInputStream(source);
            os = new FileOutputStream(dest);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = is.read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }
        } finally {
            is.close();
            os.close();
        }
    }

    public static boolean zipDirectory(File sourceDir, File zipDir) {
        List<File> fileList = getSubFiles(sourceDir, true);
        ZipOutputStream zipOutputStream = null;
        try {
            zipOutputStream = new ZipOutputStream(new FileOutputStream(zipDir));
            int bufferSize = 1024;
            byte[] buf = new byte[bufferSize];
            ZipEntry zipEntry;
            for(int i = 0; i < fileList.size(); i++) {
                File file = fileList.get(i);
                zipEntry = new ZipEntry(sourceDir.toURI().relativize(file.toURI()).getPath());
                zipOutputStream.putNextEntry(zipEntry);
                if (!file.isDirectory()) {
                    InputStream inputStream = new BufferedInputStream(new FileInputStream(file));
                    int readLength;
                    while ((readLength = inputStream.read(buf, 0, bufferSize)) != -1) {
                        zipOutputStream.write(buf, 0, readLength);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                zipOutputStream.close();
            }catch (Exception e) {
                Log.e("LF", "Failed closing os");
            }
        }
        return true;
    }

    private static List<File> getSubFiles(File baseDir, boolean isContainFolder) {
        List<File> fileList = new ArrayList<>();
        File[] tmpList = baseDir.listFiles();
        for (File file : tmpList) {
            if (file.isFile()) {
                fileList.add(file);
            }
            if (file.isDirectory()) {
                if (isContainFolder) {
                    fileList.add(file); //key code
                }
                fileList.addAll(getSubFiles(file, false));
            }
        }
        return fileList;
    }
}
