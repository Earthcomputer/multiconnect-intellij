// This is a generated file. Not intended for manual editing.
package net.earthcomputer.multiconnectintellij.csv.psi.impl;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import static net.earthcomputer.multiconnectintellij.csv.psi.CsvTypes.*;
import com.intellij.extapi.psi.StubBasedPsiElementBase;
import net.earthcomputer.multiconnectintellij.csv.psi.stubs.CsvRowStub;
import net.earthcomputer.multiconnectintellij.csv.psi.*;
import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.tree.IElementType;

public class CsvRowImpl extends StubBasedPsiElementBase<CsvRowStub> implements CsvRow {

  public CsvRowImpl(@NotNull CsvRowStub stub, @NotNull IStubElementType<?, ?> type) {
    super(stub, type);
  }

  public CsvRowImpl(@NotNull ASTNode node) {
    super(node);
  }

  public CsvRowImpl(CsvRowStub stub, IElementType type, ASTNode node) {
    super(stub, type, node);
  }

  public void accept(@NotNull CsvVisitor visitor) {
    visitor.visitRow(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof CsvVisitor) accept((CsvVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public List<CsvEntry> getEntryList() {
    return PsiTreeUtil.getStubChildrenOfTypeAsList(this, CsvEntry.class);
  }

  @Override
  @NotNull
  public List<CsvEntry> getEntries() {
    return CsvPsiImplUtil.getEntries(this);
  }

  @Override
  @Nullable
  public CsvEntry getEntry(@NotNull String name) {
    return CsvPsiImplUtil.getEntry(this, name);
  }

  @Override
  @Nullable
  public CsvEntry setEntry(@NotNull String key, @NotNull CsvEntry entry) {
    return CsvPsiImplUtil.setEntry(this, key, entry);
  }

}
