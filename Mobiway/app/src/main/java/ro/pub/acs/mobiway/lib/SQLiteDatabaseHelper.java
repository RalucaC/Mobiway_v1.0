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

import ro.pub.acs.mobiway.rest.RestClient;
import ro.pub.acs.mobiway.rest.model.Location;
import ro.pub.acs.mobiway.rest.model.User;

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
                    EMAIL       + " TEXT, "  +
                    PASSWORD    + " TEXT "   +
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
        // drop older databases
        db.execSQL(LOCATION_TABLE_DROP);
        db.execSQL(UNREGISTERED_USERS_TABLE_DROP);

        db.execSQL(LOCATION_TABLE_CREATE);
        db.execSQL(UNREGISTERED_USERS_TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        // drop older databases
        db.execSQL(LOCATION_TABLE_DROP);
        db.execSQL(UNREGISTERED_USERS_TABLE_DROP);

        // create new tables
        onCreate(db);
    }

    public void insertLocation(ro.pub.acs.mobiway.rest.model.Location location) {

        try{

            // Gets the data repository in write mode
            SQLiteDatabase db = this.getWritableDatabase();

            // Create a new map of values, where column names are the keys
            ContentValues values = new ContentValues();
            values.put(USER_ID, location.getIdUser());
            values.put(LONGITUDE, location.getLongitude());
            values.put(LATITUDE, location.getLatitude());
            values.put(SPEED, location.getSpeed());
            values.put(DATE, simpleDateFormat.format(new Date()));

            // Insert the new row, returning the primary key value of the new row
            long newRowId = db.insert(LOCATION_TABLE_NAME, null, values);

        } catch (Exception e) {
            //ACRA log
            ACRA.getErrorReporter().putCustomData("SQLiteDatabaseHelper.saveLocationInLocalStorage():error", e.toString());

            e.printStackTrace();
        }
    }

    public ArrayList<Location> readLocations(String userId){

        ArrayList<ro.pub.acs.mobiway.rest.model.Location> aLocation = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

// Define a projection that specifies which columns from the database
// you will actually use after this query.
        String[] projection = { USER_ID, LONGITUDE, LATITUDE, SPEED, DATE };

// Filter results WHERE "title" = 'My Title'
        String selection = USER_ID + " = ?";
        String[] selectionArgs = {userId};

// How you want the results sorted in the resulting Cursor
        String sortOrder = DATE + " DESC";

        Cursor cursor = db.query(
                LOCATION_TABLE_NAME,                      // The table to query
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
            savedDbLocation.setIdUser(cursor.getInt(cursor.getColumnIndex(USER_ID)));
            savedDbLocation.setLatitude(cursor.getFloat(cursor.getColumnIndex(LATITUDE)));
            savedDbLocation.setLongitude(cursor.getFloat(cursor.getColumnIndex(LONGITUDE)));
            savedDbLocation.setSpeed(cursor.getInt(cursor.getColumnIndex(SPEED)));
            String dateTime = cursor.getString(cursor.getColumnIndex(DATE));

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

    public void removeLocations(String userId){

        SQLiteDatabase db = this.getReadableDatabase();
        String[] arguments = {userId};

        db.delete(LOCATION_TABLE_NAME, USER_ID + "=?", arguments);

    }

    public void insetUser(User user) {

        try{

            // Gets the data repository in write mode
            SQLiteDatabase db = this.getWritableDatabase();

            // Create a new map of values, where column names are the keys
            ContentValues values = new ContentValues();
            values.put(EMAIL, user.getUsername());
            values.put(PASSWORD, user.getPassword());

            // Insert the new row, returning the primary key value of the new row
            long newRowId = db.insert(UNREGISTERED_USERS_TABLE_NAME, null, values);

        } catch (Exception e) {
            //ACRA log
            ACRA.getErrorReporter().putCustomData("SQLiteDatabaseHelper.insertUser():error", e.toString());

            e.printStackTrace();
        }
    }

    public void readUsersAndSendLocations(){

        SQLiteDatabase db = this.getReadableDatabase();
        String[] projection = { EMAIL, PASSWORD};

        Cursor cursor = db.query(
                UNREGISTERED_USERS_TABLE_NAME,            // The table to query
                projection,                               // The columns to return
                null,                                // The columns for the WHERE clause
                null,                            // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                null                                      // The sort order
        );

        for(cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {

            // The Cursor is now set to the right position
            User savedDbUser = new User();
            savedDbUser.setUsername(cursor.getString(cursor.getColumnIndex(EMAIL)));
            savedDbUser.setPassword(cursor.getString(cursor.getColumnIndex(PASSWORD)));

            // try to authenticate user
            try{

                RestClient restClient = new RestClient();
                User result = restClient.getApiService().loginUserPass(savedDbUser);

                if(result == null) {
                    continue;
                }
                // send saved locations
                ArrayList<ro.pub.acs.mobiway.rest.model.Location> aLocations = this.readLocations("-1");

                for(Location location: aLocations) {

                    // set new user id
                    location.setIdUser(result.getId());
                }

                try {

                    Log.d(TAG, "Update locations");
                    restClient.getApiService().updateLocations(aLocations);

                } catch (Exception e) {
                    // do nothing
                }

            } catch (Exception e) {
                //do nothing
            }
        }

        Log.d(TAG, "Remove locations");
        this.removeLocations("-1");

        Log.d(TAG, "Remove locations");
        this.removeUsers();
    }

    public void removeUsers(){

        SQLiteDatabase db = this.getReadableDatabase();
        db.delete(UNREGISTERED_USERS_TABLE_NAME, null, null);
    }
}

