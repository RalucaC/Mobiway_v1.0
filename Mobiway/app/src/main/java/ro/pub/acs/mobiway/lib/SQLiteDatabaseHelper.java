package ro.pub.acs.mobiway.lib;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by rconstanda on 6/1/2017.
 */
public class SQLiteDatabaseHelper extends SQLiteOpenHelper {

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
}
