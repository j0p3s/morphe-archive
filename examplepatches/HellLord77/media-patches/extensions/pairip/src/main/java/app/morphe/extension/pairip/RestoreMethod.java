package app.morphe.extension.pairip;

import android.util.Log;
import java.lang.reflect.Method;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("unused")
public class RestoreMethod {
  private static final String TAG = "RestoreMethod";

  @Nullable
  public static Method get(@NotNull String signature) {
    var className = extractClassName(signature);
    if (className == null) {
      return null;
    }

    Class<?> classObject;
    try {
      classObject = Class.forName(className, false, RestoreMethod.class.getClassLoader());
    } catch (ClassNotFoundException e) {
      Log.e(TAG, e.toString());
      return null;
    }

    for (var method : classObject.getDeclaredMethods()) {
      if (method.toGenericString().equals(signature)) {
        return method;
      }
    }
    return null;
  }

  @Nullable
  private static String extractClassName(@NotNull String signature) {
    var index = signature.indexOf('(');
    if (index == -1) {
      return null;
    }
    signature = signature.substring(0, index);

    index = signature.lastIndexOf('.');
    if (index == -1) {
      return null;
    }
    signature = signature.substring(0, index);

    index = signature.lastIndexOf(' ');
    if (index == -1) {
      return null;
    }
    return signature.substring(index + 1);
  }
}
