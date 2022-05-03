// This is a generated file. Not intended for manual editing.
package net.earthcomputer.multiconnectintellij.csv.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface CsvProperties extends PsiElement {

  @NotNull
  List<CsvKvPair> getKvPairList();

  @Nullable
  String getProperty(@NotNull String key);

}
