package app.template.extension.line;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * The action behind the "Read without receipt" row of the chat-list long-press menu.
 *
 * <p>LINE's menu items carry an action of its obfuscated sealed action class. The patch makes
 * that class this one's superclass (and points the constructor's super call at it), so the item
 * accepts it; the menu's dispatcher hands it to {@link ReadWithoutReceipt#onMenuAction(Object)}
 * before its own type checks. Only the chat list's bottom-sheet menu gets the row, and it
 * dispatches in-process, so the action is never parcelled (the chat preview screen's menu, which
 * returns its action through an Intent, does not get the row). The Parcelable methods only satisfy
 * the superclass's interface.
 */
public final class ReadWithoutReceiptMenuAction implements Parcelable {
    final String chatId;

    public ReadWithoutReceiptMenuAction(String chatId) {
        this.chatId = chatId;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(chatId);
    }
}
