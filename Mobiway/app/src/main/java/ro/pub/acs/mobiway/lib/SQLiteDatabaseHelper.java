package ro.pub.acs.mobiway.lib;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by rconstanda on 6/1/2017.
 */
public class SQLiteDatabaseHelper extends SQLiteOpenHelper {

    public static final String USER_ID         = "userId";
    public static final String LONGITUDE       = "long";
    public static final String LATITUDE        = "lat";
    public static final String SPEED           = "speed";
    public static final String DATE            = "date";
    public static final String LOCATION_TABLE_NAME = "userLocations";

    private static final int DATABASE_VERSION = 1 ;
    public static final String DATABASE_NAME   = "MOBIWAY";

    private static final String LOCATION_TABLE_CREATE =
            "CREATE TABLE " + LOCATION_TABLE_NAME + " (" +
                    USER_ID     + " INT, "  +
                    LONGITUDE   + " FLOAT, " +
                    LATITUDE    + " FLOAT, " +
                    SPEED       + " INT, "   +
                    DATE        + " DATE"    +
                    ");";

    private static final String LOCATION_TABLE_DROP =
            "DROP TABLE IF EXISTS " + LOCATION_TABLE_NAME;


    public SQLiteDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL(LOCATION_TABLE_DROP);
        db.execSQL(LOCATION_TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        // TODO
    }
}
