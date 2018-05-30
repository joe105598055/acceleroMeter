package com.example.joe.accelermeter;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;

/**
 * Created by joe on 2018/4/26.
 */

public class ExcelBuilder {

    public static final String TAG = "ExcelBuilder";

    private static Workbook _wb = null;

    public static int rowAccelIndex = 1;
    public static int rowDeltaIndex = 1;
    public static int rowActiveIndex = 1;
    public static int rowmAccelCurrent = 1;


    public static int colIndex = 0;
    private static int[] distances = {1, 2, 3, 5, 8, 13, 20, 30, 40, 50};

    public static void initExcel() {

        if (_wb == null) {
            _wb = new HSSFWorkbook();
            _wb.createSheet("AcceleroMeter");
        }
        Sheet AMSheet = _wb.getSheet("AcceleroMeter");
        AMSheet.createRow(0).createCell(0).setCellValue("mAccel");
        AMSheet.getRow(0).createCell(1).setCellValue("delta");
        AMSheet.getRow(0).createCell(2).setCellValue("Active");

    }

    public static void clearExcel() {

        _wb = null;

    }

//    public static void setCellByRowInOrder(BeaconObject content) {
//
//        Sheet RSSISheet = _wb.getSheet("RSSI");
//        RSSISheet.createRow(rowIndex).createCell(colIndex).setCellValue("[" + content.major + "," + content.minor + "]");
//        RSSISheet.getRow(rowIndex).createCell(1).setCellValue(content.rssi);
//        rowIndex++;
//    }

    public static void setAccel(Float accel) {
        System.out.println("[setAccel]");
        Sheet AMSheet = _wb.getSheet("AcceleroMeter");
        AMSheet.createRow(rowAccelIndex).createCell(0).setCellValue(accel);
        rowAccelIndex++;
    }

    public static void setDuration(long duration){
        Sheet AMSheet = _wb.getSheet("AcceleroMeter");
        AMSheet.getRow(0).createCell(4).setCellValue("Duration");
        AMSheet.getRow(1).createCell(4).setCellValue(duration);
    }
    public static void setDelta(String delta) {
        System.out.println("[setAccel]");
        Sheet AMSheet = _wb.getSheet("AcceleroMeter");
        AMSheet.getRow(rowDeltaIndex).createCell(1).setCellValue(delta);
        rowDeltaIndex++;
    }

    public static void setActive(String active) {
        System.out.println("[setActive]");
        Sheet AMSheet = _wb.getSheet("AcceleroMeter");
        AMSheet.getRow(rowActiveIndex).createCell(2).setCellValue(active);
        rowActiveIndex++;
    }
    public static void setmAccelCurrent(float accelCurrent){
        Sheet AMSheet = _wb.getSheet("AcceleroMeter");
        AMSheet.getRow(rowmAccelCurrent).createCell(3).setCellValue(accelCurrent);
        rowmAccelCurrent++;
    }

    public static boolean saveExcelFile(Context context, String fileName) {

        // check if available and not read only
        if (!isExternalStorageAvailable() || isExternalStorageReadOnly()) {
            Log.e(TAG, "Storage not available or read only");
            return false;
        }

        boolean success = false;

        // Create a path where we will place our List of objects on external storage
        File file = new File(context.getExternalFilesDir(null), fileName + ".xls");
        FileOutputStream os = null;

        try {
            os = new FileOutputStream(file);
            _wb.write(os);
            Log.w("FileUtils", "Writing file" + file);
            success = true;
        } catch (IOException e) {
            Log.w("FileUtils", "Error writing " + file, e);
        } catch (Exception e) {
            Log.w("FileUtils", "Failed to save file", e);
        } finally {
            try {
                if (null != os)
                    os.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return success;
    }

    @Deprecated
    public static void readExcelFile(Context context, String fileName) {

        if (!isExternalStorageAvailable() || isExternalStorageReadOnly()) {
            Log.e(TAG, "Storage not available or read only");
            return;
        }

        try {
            // Creating Input Stream
            File file = new File(context.getExternalFilesDir(null), fileName + ".xls");
            FileInputStream myInput = new FileInputStream(file);

            // Create a POIFSFileSystem object
            POIFSFileSystem myFileSystem = new POIFSFileSystem(myInput);

            // Create a workbook using the File System
            HSSFWorkbook myWorkBook = new HSSFWorkbook(myFileSystem);

            // Get the first sheet from workbook
            HSSFSheet mySheet = myWorkBook.getSheetAt(0);

            /** We now need something to iterate through the cells.**/
            Iterator rowIter = mySheet.rowIterator();

            while (rowIter.hasNext()) {
                HSSFRow myRow = (HSSFRow) rowIter.next();
                Iterator cellIter = myRow.cellIterator();
                while (cellIter.hasNext()) {
                    HSSFCell myCell = (HSSFCell) cellIter.next();
                    Log.d(TAG, "Cell Value: " + myCell.toString());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return;
    }

    public static boolean isExternalStorageReadOnly() {
        String extStorageState = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(extStorageState)) {
            return true;
        }
        return false;
    }


    public static boolean isExternalStorageAvailable() {
        String extStorageState = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(extStorageState)) {
            return true;
        }
        return false;
    }
}