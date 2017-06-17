package ro.pub.acs.mobiway.lib;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import org.acra.ACRA;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import ro.pub.acs.mobiway.general.SharedPreferencesManagement;
import ro.pub.acs.mobiway.rest.model.Location;

/**
 * Created by rconstanda on 6/1/2017.
 */
public class SQLiteDatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = SQLiteDatabaseHelper.class.getSimpleName();
    private static final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    // Define constants about database
    public static final String DATABASE_NAME    = "MOBIWAY";
    public static final int DATABASE_VERSION    = 1 ;

    // Define database tables
    public static final String LOCATION_TABLE_NAME = "userLocations";
    public static final String UNREGISTERED_USERS_TABLE_NAME = "unregisteredUsers";

    // Define userLocations collection's columns
    public static final String USER_ID         = "userId";
    public static final String LONGITUDE       = "long";
    public static final String LATITUDE        = "lat";
    public static final String SPEED           = "speed";
    public static final String DATE            = "date";

    // Define unregisteredUsers collection's columns
    public static final String NAME     = "name";
    public static final String EMAIL    = "email";
    public static final String PASSWORD = "password";

    // Create userLocations TABLE
    private static final String LOCATION_TABLE_CREATE =
            "CREATE TABLE " + LOCATION_TABLE_NAME + " (" +
                    USER_ID     + " INT, "   +
                    LONGITUDE   + " FLOAT, " +
                    LATITUDE    + " FLOAT, " +
                    SPEED       + " INT, "   +
                    DATE        + " DATE"    +
                    ");";

    // Drop userLocations TABLE
    private static final String LOCATION_TABLE_DROP =
            "DROP TABLE IF EXISTS " + LOCATION_TABLE_NAME;

    // Create unregisteredUsers TABLE
    private static final String UNREGISTERED_USERS_TABLE_CREATE =
            "CREATE TABLE " + UNREGISTERED_USERS_TABLE_NAME + " (" +
                    NAME        + "TEXT, " +
                    EMAIL       + "TEXT, " +
                    PASSWORD    + "TEXT "  +
                    ");";

    // Drop unregisteredUsers table
    private static final String UNREGISTERED_USERS_TABLE_DROP =
            "DROP TABLE IF EXISTS " + UNREGISTERED_USERS_TABLE_NAME;


    // Class constructor
    public SQLiteDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL(LOCATION_TABLE_CREATE);
        db.execSQL(UNREGISTERED_USERS_TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        // drop older databases
        db.execSQL("DROP TABLE IF EXISTS " + LOCATION_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + UNREGISTERED_USERS_TABLE_NAME);

        // create new tables
        onCreate(db);
    }

    public void saveLocationInLocalStorage(ro.pub.acs.mobiway.rest.model.Location location) {

        try{

            // Gets the data repository in write mode
            SQLiteDatabase db = this.getWritableDatabase();

            // Create a new map of values, where column names are the keys
            ContentValues values = new ContentValues();
            values.put(SQLiteDatabaseHelper.USER_ID, location.getIdUser());
            values.put(SQLiteDatabaseHelper.LONGITUDE, location.getLongitude());
            values.put(SQLiteDatabaseHelper.LATITUDE, location.getLatitude());
            values.put(SQLiteDatabaseHelper.SPEED, location.getSpeed());
            values.put(SQLiteDatabaseHelper.DATE, simpleDateFormat.format(new Date()));

            // Insert the new row, returning the primary key value of the new row
            long newRowId = db.insert(SQLiteDatabaseHelper.LOCATION_TABLE_NAME, null, values);

        } catch (Exception e) {
            //ACRA log
            ACRA.getErrorReporter().putCustomData("MainActivity.saveLocationInLocalStorage():error", e.toString());

            e.printStackTrace();
        }
    }

    public ArrayList<Location> readLocationsFromLocalStorage(Context context){

        ArrayList<ro.pub.acs.mobiway.rest.model.Location> aLocation = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        SharedPreferencesManagement spm = new SharedPreferencesManagement(context);

// Define a projection that specifies which columns from the database
// you will actually use after this query.
        String[] projection = {
                SQLiteDatabaseHelper.USER_ID,
                SQLiteDatabaseHelper.LONGITUDE,
                SQLiteDatabaseHelper.LATITUDE,
                SQLiteDatabaseHelper.SPEED,
                SQLiteDatabaseHelper.DATE
        };

// Filter results WHERE "title" = 'My Title'
        String selection = SQLiteDatabaseHelper.USER_ID + " = ?";
        String[] selectionArgs = {spm.getAuthUserId() + ""};

// How you want the results sorted in the resulting Cursor
        String sortOrder =
                SQLiteDatabaseHelper.DATE + " DESC";

        Cursor cursor = db.query(
                SQLiteDatabaseHelper.LOCATION_TABLE_NAME,                     // The table to query
                projection,                               // The columns to return
                selection,                                // The columns for the WHERE clause
                selectionArgs,                            // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                sortOrder                                 // The sort order
        );

        for(cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {

            // The Cursor is now set to the right position
            ro.pub.acs.mobiway.rest.model.Location savedDbLocation = new ro.pub.acs.mobiway.rest.model.Location();
            savedDbLocation.setIdUser(cursor.getInt(cursor.getColumnIndex(SQLiteDatabaseHelper.USER_ID)));
            savedDbLocation.setLatitude(cursor.getFloat(cursor.getColumnIndex(SQLiteDatabaseHelper.LATITUDE)));
            savedDbLocation.setLongitude(cursor.getFloat(cursor.getColumnIndex(SQLiteDatabaseHelper.LONGITUDE)));
            savedDbLocation.setSpeed(cursor.getInt(cursor.getColumnIndex(SQLiteDatabaseHelper.SPEED)));
            String dateTime = cursor.getString(cursor.getColumnIndex(SQLiteDatabaseHelper.DATE));

            try {
                Date date = simpleDateFormat.parse(dateTime);
                savedDbLocation.setTimestamp(date);

            } catch (ParseException e) {
                Log.e(TAG, "Parsing ISO8601 datetime failed", e);
            }

            aLocation.add(savedDbLocation);
        }

        Log.d(TAG, aLocation + "");
        return aLocation;
    }

    public void removeLocationsFromLocalStorage(Context context){

        SQLiteDatabase db = this.getReadableDatabase();
        SharedPreferencesManagement spm = new SharedPreferencesManagement(context);
        String[] arguments = {spm.getAuthUserId() + ""};

        db.delete(SQLiteDatabaseHelper.LOCATION_TABLE_NAME, SQLiteDatabaseHelper.USER_ID+"=?", arguments);

    }

}
