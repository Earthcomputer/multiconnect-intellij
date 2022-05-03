// This is a generated file. Not intended for manual editing.
package net.earthcomputer.multiconnectintellij.csv.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface CsvKvPair extends PsiElement {

  @NotNull
  List<CsvStringValue> getStringElements();

  @NotNull
  CsvStringValue getKeyElement();

  @Nullable
  CsvStringValue getValueElement();

  @NotNull
  String getKey();

  @Nullable
  String getValue();

}
