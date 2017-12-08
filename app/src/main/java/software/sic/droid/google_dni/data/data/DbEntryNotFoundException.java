package software.sic.droid.google_dni.data.data;

/**
 * www.sic.software
 *
 * 17.03.17
 */

public class DbEntryNotFoundException extends Exception {
    private final long rowId;

    public DbEntryNotFoundException() {
        this.rowId = 0;
    }

    public DbEntryNotFoundException(long rowId) {
        this.rowId = rowId;
    }
}//class DbEntryNotFoundException