package app.morphe.extension.minecraft.documentsprovider;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.os.CancellationSignal;
import android.os.ParcelFileDescriptor;
import android.provider.DocumentsContract.Document;
import android.provider.DocumentsContract.Root;
import android.provider.DocumentsProvider;
import android.webkit.MimeTypeMap;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public final class AppDataDocumentsProvider extends DocumentsProvider {
    private static final String ROOT_ID = "root";

    private static final String[] DEFAULT_ROOT_PROJECTION = {
            Root.COLUMN_ROOT_ID,
            Root.COLUMN_MIME_TYPES,
            Root.COLUMN_FLAGS,
            Root.COLUMN_ICON,
            Root.COLUMN_TITLE,
            Root.COLUMN_SUMMARY,
            Root.COLUMN_DOCUMENT_ID,
    };

    private static final String[] DEFAULT_DOCUMENT_PROJECTION = {
            Document.COLUMN_DOCUMENT_ID,
            Document.COLUMN_MIME_TYPE,
            Document.COLUMN_DISPLAY_NAME,
            Document.COLUMN_LAST_MODIFIED,
            Document.COLUMN_FLAGS,
            Document.COLUMN_SIZE,
    };

    private Map<String, File> bases;

    @Override
    public boolean onCreate() {
        return true;
    }

    private synchronized Map<String, File> getBases() {
        if (bases == null) {
            Context context = getContext();
            Map<String, File> map = new LinkedHashMap<>();

            map.put("data", context.getDataDir());

            File externalFiles = context.getExternalFilesDir(null);
            if (externalFiles != null && externalFiles.getParentFile() != null) {
                map.put("android_data", externalFiles.getParentFile());
            }

            File obb = context.getObbDir();
            if (obb != null) {
                map.put("android_obb", obb);
            }

            bases = map;
        }
        return bases;
    }

    private String getAppLabel() {
        Context context = getContext();
        return context.getApplicationInfo().loadLabel(context.getPackageManager()).toString();
    }

    private File getFileForDocId(String documentId) throws FileNotFoundException {
        int slash = documentId.indexOf('/');
        String baseName = slash < 0 ? documentId : documentId.substring(0, slash);
        String relativePath = slash < 0 ? "" : documentId.substring(slash + 1);

        File base = getBases().get(baseName);
        if (base == null) {
            throw new FileNotFoundException("Unknown document: " + documentId);
        }

        File file = relativePath.isEmpty() ? base : new File(base, relativePath);

        try {
            String basePath = base.getCanonicalPath();
            String filePath = file.getCanonicalPath();
            if (!filePath.equals(basePath) && !filePath.startsWith(basePath + File.separator)) {
                throw new FileNotFoundException("Invalid document: " + documentId);
            }
        } catch (IOException e) {
            throw new FileNotFoundException(e.getMessage());
        }

        return file;
    }

    private String getDocIdForFile(String parentDocumentId, File file) {
        return parentDocumentId + "/" + file.getName();
    }

    private static boolean isBaseDocId(String documentId) {
        return documentId.indexOf('/') < 0;
    }

    private static String getMimeType(File file) {
        if (file.isDirectory()) {
            return Document.MIME_TYPE_DIR;
        }

        String name = file.getName();
        int dot = name.lastIndexOf('.');
        if (dot >= 0) {
            String extension = name.substring(dot + 1).toLowerCase(Locale.ROOT);
            String mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
            if (mime != null) {
                return mime;
            }
        }
        return "application/octet-stream";
    }

    private void includeFile(MatrixCursor cursor, String documentId, File file) {
        boolean isBase = isBaseDocId(documentId);
        int flags = 0;

        if (file.canWrite()) {
            if (file.isDirectory()) {
                flags |= Document.FLAG_DIR_SUPPORTS_CREATE;
            } else {
                flags |= Document.FLAG_SUPPORTS_WRITE;
            }
            if (!isBase) {
                flags |= Document.FLAG_SUPPORTS_DELETE | Document.FLAG_SUPPORTS_RENAME;
            }
        }

        MatrixCursor.RowBuilder row = cursor.newRow();
        row.add(Document.COLUMN_DOCUMENT_ID, documentId);
        row.add(Document.COLUMN_DISPLAY_NAME, isBase ? documentId : file.getName());
        row.add(Document.COLUMN_MIME_TYPE, getMimeType(file));
        row.add(Document.COLUMN_LAST_MODIFIED, file.lastModified());
        row.add(Document.COLUMN_FLAGS, flags);
        row.add(Document.COLUMN_SIZE, file.isDirectory() ? null : file.length());
    }

    private void includeRoot(MatrixCursor cursor) {
        MatrixCursor.RowBuilder row = cursor.newRow();
        row.add(Document.COLUMN_DOCUMENT_ID, ROOT_ID);
        row.add(Document.COLUMN_DISPLAY_NAME, getAppLabel());
        row.add(Document.COLUMN_MIME_TYPE, Document.MIME_TYPE_DIR);
        row.add(Document.COLUMN_LAST_MODIFIED, null);
        row.add(Document.COLUMN_FLAGS, 0);
        row.add(Document.COLUMN_SIZE, null);
    }

    @Override
    public Cursor queryRoots(String[] projection) {
        Context context = getContext();
        ApplicationInfo info = context.getApplicationInfo();

        MatrixCursor cursor = new MatrixCursor(projection != null ? projection : DEFAULT_ROOT_PROJECTION);
        MatrixCursor.RowBuilder row = cursor.newRow();
        row.add(Root.COLUMN_ROOT_ID, ROOT_ID);
        row.add(Root.COLUMN_DOCUMENT_ID, ROOT_ID);
        row.add(Root.COLUMN_TITLE, getAppLabel());
        row.add(Root.COLUMN_SUMMARY, context.getPackageName());
        row.add(Root.COLUMN_ICON, info.icon);
        row.add(Root.COLUMN_MIME_TYPES, "*/*");
        row.add(Root.COLUMN_FLAGS, Root.FLAG_SUPPORTS_CREATE | Root.FLAG_SUPPORTS_IS_CHILD);
        return cursor;
    }

    @Override
    public Cursor queryDocument(String documentId, String[] projection) throws FileNotFoundException {
        MatrixCursor cursor = new MatrixCursor(projection != null ? projection : DEFAULT_DOCUMENT_PROJECTION);
        if (ROOT_ID.equals(documentId)) {
            includeRoot(cursor);
        } else {
            includeFile(cursor, documentId, getFileForDocId(documentId));
        }
        return cursor;
    }

    @Override
    public Cursor queryChildDocuments(String parentDocumentId, String[] projection, String sortOrder)
            throws FileNotFoundException {
        MatrixCursor cursor = new MatrixCursor(projection != null ? projection : DEFAULT_DOCUMENT_PROJECTION);

        if (ROOT_ID.equals(parentDocumentId)) {
            for (Map.Entry<String, File> entry : getBases().entrySet()) {
                File base = entry.getValue();
                if (base.exists() || base.mkdirs()) {
                    includeFile(cursor, entry.getKey(), base);
                }
            }
            return cursor;
        }

        File parent = getFileForDocId(parentDocumentId);
        File[] children = parent.listFiles();
        if (children != null) {
            for (File child : children) {
                includeFile(cursor, getDocIdForFile(parentDocumentId, child), child);
            }
        }
        return cursor;
    }

    @Override
    public ParcelFileDescriptor openDocument(String documentId, String mode, CancellationSignal signal)
            throws FileNotFoundException {
        if (ROOT_ID.equals(documentId)) {
            throw new FileNotFoundException("Cannot open root");
        }
        File file = getFileForDocId(documentId);
        return ParcelFileDescriptor.open(file, ParcelFileDescriptor.parseMode(mode));
    }

    @Override
    public String createDocument(String parentDocumentId, String mimeType, String displayName)
            throws FileNotFoundException {
        if (ROOT_ID.equals(parentDocumentId)) {
            throw new FileNotFoundException("Cannot create in root");
        }

        File parent = getFileForDocId(parentDocumentId);
        File file = new File(parent, displayName);

        int index = 1;
        while (file.exists()) {
            String name = displayName;
            String extension = "";
            int dot = displayName.lastIndexOf('.');
            if (dot > 0 && !Document.MIME_TYPE_DIR.equals(mimeType)) {
                name = displayName.substring(0, dot);
                extension = displayName.substring(dot);
            }
            file = new File(parent, name + " (" + index++ + ")" + extension);
        }

        boolean created;
        try {
            created = Document.MIME_TYPE_DIR.equals(mimeType) ? file.mkdir() : file.createNewFile();
        } catch (IOException e) {
            throw new FileNotFoundException(e.getMessage());
        }
        if (!created) {
            throw new FileNotFoundException("Failed to create " + file);
        }

        return getDocIdForFile(parentDocumentId, file);
    }

    private static boolean deleteRecursively(File file) {
        File[] children = file.listFiles();
        if (children != null) {
            for (File child : children) {
                if (!deleteRecursively(child)) {
                    return false;
                }
            }
        }
        return file.delete();
    }

    @Override
    public void deleteDocument(String documentId) throws FileNotFoundException {
        if (ROOT_ID.equals(documentId) || isBaseDocId(documentId)) {
            throw new FileNotFoundException("Cannot delete " + documentId);
        }
        File file = getFileForDocId(documentId);
        if (!deleteRecursively(file)) {
            throw new FileNotFoundException("Failed to delete " + documentId);
        }
    }

    @Override
    public String renameDocument(String documentId, String displayName) throws FileNotFoundException {
        if (ROOT_ID.equals(documentId) || isBaseDocId(documentId)) {
            throw new FileNotFoundException("Cannot rename " + documentId);
        }
        File file = getFileForDocId(documentId);
        File target = new File(file.getParentFile(), displayName);
        if (target.exists() || !file.renameTo(target)) {
            throw new FileNotFoundException("Failed to rename " + documentId);
        }
        String parentDocumentId = documentId.substring(0, documentId.lastIndexOf('/'));
        return getDocIdForFile(parentDocumentId, target);
    }

    @Override
    public boolean isChildDocument(String parentDocumentId, String documentId) {
        if (ROOT_ID.equals(parentDocumentId)) {
            return !ROOT_ID.equals(documentId);
        }
        return documentId.startsWith(parentDocumentId + "/");
    }
}
