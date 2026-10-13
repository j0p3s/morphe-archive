package app.czyeru.extension.securefolder;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Reverse-engineered helper for Samsung One UI "Move to Secure Folder" feature.
 * Uses reflection to interact with Knox SemPersonaManager and SemRemoteContentManager services.
 */
@SuppressWarnings("unused")
public final class SecureFolderHelper {
    private static final String TAG = "SecureFolderHelper";
    private static final String PERSONA_SERVICE = "persona";
    private static final String RCP_SERVICE = "rcp";
    private static final int SECURE_FOLDER_CONTAINER_TYPE = 1002;
    private static final int MOVE_TO_CONTAINER_OP = 1;

    private SecureFolderHelper() {
    }

    /**
     * Checks if Secure Folder is active and available on the device.
     */
    public static boolean isSecureFolderAvailable(Context context) {
        return getSecureFolderContainerId(context) != -1;
    }

    /**
     * Queries Samsung Knox SemPersonaManager to retrieve the Secure Folder container ID.
     * Container type 1002 corresponds to Secure Folder.
     */
    @SuppressWarnings("unchecked")
    public static int getSecureFolderContainerId(Context context) {
        try {
            Object personaManager = context.getSystemService(PERSONA_SERVICE);
            if (personaManager == null) return -1;

            Method getMenuListMethod = personaManager.getClass()
                    .getMethod("getMoveToKnoxMenuList", Context.class);
            Object result = getMenuListMethod.invoke(personaManager, context);
            if (!(result instanceof List)) return -1;

            for (Bundle bundle : (List<Bundle>) result) {
                int containerType = bundle.getInt("com.sec.knox.moveto.containerType", -1);
                if (containerType == SECURE_FOLDER_CONTAINER_TYPE) {
                    int containerId = bundle.getInt("com.sec.knox.moveto.containerId", -1);
                    Log.d(TAG, "Found Secure Folder Container ID: " + containerId);
                    return containerId;
                }
            }
            return -1;
        } catch (Exception e) {
            Log.w(TAG, "Failed to query Secure Folder container ID: " + e.getMessage());
            return -1;
        }
    }

    /**
     * Moves a list of file paths to Samsung Secure Folder via SemRemoteContentManager.
     */
    public static boolean moveToSecureFolder(Context context, List<String> filePaths) {
        if (filePaths.isEmpty()) return false;

        int containerId = getSecureFolderContainerId(context);
        if (containerId == -1) {
            Log.e(TAG, "Cannot move to Secure Folder: Secure Folder is not active or container ID not found");
            return false;
        }

        try {
            Object rcpManager = context.getSystemService(RCP_SERVICE);
            if (rcpManager == null) {
                throw new IllegalStateException("RCP system service ('rcp') is not available on this device");
            }

            ArrayList<String> pathsList = new ArrayList<>(filePaths);
            Method moveFilesMethod = rcpManager.getClass().getMethod(
                    "moveFiles",
                    int.class,
                    ArrayList.class,
                    ArrayList.class,
                    int.class
            );

            moveFilesMethod.invoke(rcpManager, MOVE_TO_CONTAINER_OP, pathsList, pathsList, containerId);
            Log.i(TAG, "Successfully sent " + filePaths.size()
                    + " file(s) to Secure Folder (Container ID: " + containerId + ")");
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error moving files to Secure Folder: " + e.getMessage(), e);
            return false;
        }
    }
}
