// This is a generated file. Not intended for manual editing.
package net.earthcomputer.multiconnectintellij.csv.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface CsvRow extends PsiElement {

  @NotNull
  List<CsvEntry> getEntryList();

  @Nullable
  CsvEntry getEntry(@NotNull String name);

}
