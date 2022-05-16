// This is a generated file. Not intended for manual editing.
package net.earthcomputer.multiconnectintellij.csv.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import com.intellij.psi.StubBasedPsiElement;
import net.earthcomputer.multiconnectintellij.csv.psi.stubs.CsvRowStub;
import java.util.function.UnaryOperator;

public interface CsvRow extends PsiElement, StubBasedPsiElement<CsvRowStub> {

  @NotNull
  List<CsvEntry> getEntryList();

  @NotNull
  List<CsvEntry> getEntries();

  @Nullable
  CsvEntry getEntry(@NotNull String name);

  @Nullable
  CsvEntry setEntry(@NotNull String key, @NotNull CsvEntry entry, boolean loadAst);

  @Nullable
  CsvEntry setEntry(@NotNull String key, @NotNull CsvEntry entry);

  void copyEntry(@NotNull String fromKey, @NotNull String toKey, boolean loadAst);

  void copyEntry(@NotNull String fromKey, @NotNull String toKey);

  void replaceEntry(@NotNull String key, @NotNull UnaryOperator<CsvEntry> func, boolean loadAst);

  void replaceEntry(@NotNull String key, @NotNull UnaryOperator<CsvEntry> func);

}
