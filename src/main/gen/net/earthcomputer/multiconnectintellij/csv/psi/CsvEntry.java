// This is a generated file. Not intended for manual editing.
package net.earthcomputer.multiconnectintellij.csv.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import com.intellij.psi.StubBasedPsiElement;
import net.earthcomputer.multiconnectintellij.csv.psi.stubs.CsvEntryStub;

public interface CsvEntry extends PsiElement, StubBasedPsiElement<CsvEntryStub> {

  @NotNull
  CsvIdentifier getIdentifier();

  @Nullable
  CsvProperties getProperties();

  @NotNull
  String getNamespace();

  @NotNull
  String getPath();

  @Nullable
  String getKey();

}
