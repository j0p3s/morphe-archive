package app.morphe.extension.shared.compat.java.lang;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Class {
  @Nullable
  public static java.lang.Class<?> forPrimitiveName(@NotNull String primitiveName) {
    return switch (primitiveName) {
      case "int" -> int.class;
      case "long" -> long.class;
      case "short" -> short.class;
      case "char" -> char.class;
      case "byte" -> byte.class;

      case "float" -> float.class;
      case "double" -> double.class;

      case "boolean" -> boolean.class;
      case "void" -> void.class;

      default -> null;
    };
  }
}
