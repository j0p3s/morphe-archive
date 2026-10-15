// Based on andrewliang25/morphe-patches (GPL-3.0), extensions/extension KeepUnsentMessages.java: the
// placeholder INSERT that marks a kept message as unsent. Unchanged apart from the package, the log
// tag and the comments.

package app.template.extension.line;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

/**
 * Adds the "unsent a message" notice under a message that the "Keep unsent messages" patch kept.
 *
 * <p>The patch makes LINE skip its unsend database work, so the original chat_history row stays as
 * it was. Nothing would then show that the message was recalled, so this class inserts a second,
 * synthetic chat_history row of LINE's UNSENT type (27). LINE's own renderer draws that row as
 * "&lt;name&gt; unsent a message." (or "You unsent a message.") from its from_mid, which is copied
 * from the original row, so naming and localisation come for free.
 *
 * <p>Best effort: any failure is logged and swallowed, leaving the kept message without a notice.
 */
@SuppressWarnings("unused")
public final class KeepUnsentMessages {
    private static final String TAG = "MorpheLineKeepUnsent";

    /** chat_history.type of LINE's "this message was unsent" row. */
    private static final int TYPE_UNSENT = 27;

    /** The tombstone types (UNSENT, UNSENT_NO_MARK, UNSENT_SILENT): such a row already is a notice. */
    private static final String TYPES_ALREADY_UNSENT = "27, 28, 38";

    /** attachement_type NONE, as LINE's own unsend writes it. */
    private static final int ATTACHMENT_NONE = 0;

    /** Suffix of the placeholder's server_id: tells it apart from real rows and makes the insert idempotent. */
    private static final String MARK = "_unsent";

    /**
     * Copies the identifying columns of the unsent message into a new tombstone row.
     * created_time is TEXT holding epoch millis; +1 ms orders the notice right below its message.
     * status, read_count and sent_count are inherited so the notice shows the same receipt. The
     * chat's unread badge comes from the chat row's own counters, which this never writes.
     * Rows that already are tombstones, and messages that already have a notice, are skipped.
     */
    private static final String INSERT_PLACEHOLDER =
            "INSERT INTO chat_history"
                    + " (server_id, type, chat_id, from_mid, created_time, delivered_time,"
                    + " status, read_count, sent_count, attachement_type)"
                    + " SELECT o.server_id || ?, ?, o.chat_id, o.from_mid,"
                    + " CAST(CAST(o.created_time AS INTEGER) + 1 AS TEXT), o.delivered_time,"
                    + " o.status, o.read_count, o.sent_count, ?"
                    + " FROM chat_history o"
                    + " WHERE (o.server_id = ? OR o.id = ?)"
                    + " AND o.server_id IS NOT NULL"
                    + " AND o.created_time IS NOT NULL"
                    + " AND o.type NOT IN (" + TYPES_ALREADY_UNSENT + ")"
                    + " AND NOT EXISTS ("
                    + "  SELECT 1 FROM chat_history x WHERE x.server_id = o.server_id || ?)"
                    + " LIMIT 1";

    private KeepUnsentMessages() {
    }

    /**
     * @param db        the open chat database, inside LINE's own unsend transaction.
     * @param messageId the unsent message's id as loaded at the patch site: either the local
     *                  chat_history.id or the server_id. The WHERE clause accepts both.
     */
    public static void insertPlaceholder(SQLiteDatabase db, long messageId) {
        if (db == null) return;
        try {
            db.execSQL(INSERT_PLACEHOLDER, new Object[]{
                    MARK,
                    TYPE_UNSENT,
                    ATTACHMENT_NONE,
                    String.valueOf(messageId),
                    messageId,
                    MARK,
            });
        } catch (Throwable t) {
            Log.w(TAG, "Could not add the unsent notice; keeping the message without one.", t);
        }
    }
}
