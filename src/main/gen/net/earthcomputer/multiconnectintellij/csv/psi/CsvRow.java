// This is a generated file. Not intended for manual editing.
package net.earthcomputer.multiconnectintellij.csv.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import com.intellij.psi.StubBasedPsiElement;
import net.earthcomputer.multiconnectintellij.csv.psi.stubs.CsvRowStub;

public interface CsvRow extends PsiElement, StubBasedPsiElement<CsvRowStub> {

  @NotNull
  List<CsvEntry> getEntryList();

  @NotNull
  List<CsvEntry> getEntries();

  @Nullable
  CsvEntry getEntry(@NotNull String name);

  @Nullable
  CsvEntry setEntry(@NotNull String key, @NotNull CsvEntry entry);

}
