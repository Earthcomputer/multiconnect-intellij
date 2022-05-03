// This is a generated file. Not intended for manual editing.
package net.earthcomputer.multiconnectintellij.csv.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface CsvIdentifier extends PsiElement {

  @NotNull
  List<CsvStringValue> getStringElements();

  @Nullable
  CsvStringValue getNamespaceElement();

  @NotNull
  CsvStringValue getPathElement();

  @NotNull
  String getNamespace();

  @NotNull
  String getPath();

  @NotNull
  String getNormalizedString();

}
