package com.ghijoon.klinikdayamedika;

/**
 * Created by Rendi on 01-Jul-17.
 */

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

public class SQLHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "dayamedika.sqlite";
    private static final int DB_VERSION = 1;
    private static String DB_PATH = "/data/data/com.ghijoon.klinikdayamedika/databases/";
    private Context myContext;

    public SQLHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        // TODO Auto-generated constructor stub
        myContext=context;
    }

    public void createDataBase() throws IOException{
        if(isDatabaseExist()){
            //do nothing - database already exist
        }
        else{
            //By calling this method and empty database will be created into the default system path
            //of your application so we are gonna be able to overwrite that database with our database.
            this.getReadableDatabase();

            try {
                copyDataBase();
            } catch (IOException e) {
                throw new Error("Error copying database");
            }
        }

    }

    private boolean isDatabaseExist(){
        SQLiteDatabase checkDB = null;
        try{
            String myPath = DB_PATH + DB_NAME;
            checkDB = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);
        }catch(SQLiteException e){
            //database doesn't exist yet.
        }
        if(checkDB != null){
            checkDB.close();
        }
        if(checkDB != null )return true ;else return false;
    }

    private void copyDataBase() throws IOException{
        //Open your local db as the input stream
        InputStream myInput = myContext.getAssets().open(DB_NAME);
        // Path to the just created empty db
        String outFileName = DB_PATH + DB_NAME;
        //Open the empty db as the output stream
        OutputStream myOutput = new FileOutputStream(outFileName);
        //transfer bytes from the inputfile to the outputfile
        byte[] buffer = new byte[1024];
        int length;
        while ((length = myInput.read(buffer))>0){
            myOutput.write(buffer, 0, length);
        }
        //Close the streams
        myOutput.flush();
        myOutput.close();
        myInput.close();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO Auto-generated method stub

    }
}
