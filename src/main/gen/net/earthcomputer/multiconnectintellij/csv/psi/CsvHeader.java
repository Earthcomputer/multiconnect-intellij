// This is a generated file. Not intended for manual editing.
package net.earthcomputer.multiconnectintellij.csv.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import com.intellij.psi.StubBasedPsiElement;
import net.earthcomputer.multiconnectintellij.csv.psi.stubs.CsvHeaderStub;

public interface CsvHeader extends PsiElement, StubBasedPsiElement<CsvHeaderStub> {

  @NotNull
  List<CsvStringValue> getKeyElements();

  @NotNull
  List<String> getKeyNames();

}
