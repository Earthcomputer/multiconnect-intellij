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
import net.earthcomputer.multiconnectintellij.csv.psi.stubs.CsvEntryStub;
import net.earthcomputer.multiconnectintellij.csv.psi.*;
import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.tree.IElementType;

public class CsvEntryImpl extends StubBasedPsiElementBase<CsvEntryStub> implements CsvEntry {

  public CsvEntryImpl(@NotNull CsvEntryStub stub, @NotNull IStubElementType<?, ?> type) {
    super(stub, type);
  }

  public CsvEntryImpl(@NotNull ASTNode node) {
    super(node);
  }

  public CsvEntryImpl(CsvEntryStub stub, IElementType type, ASTNode node) {
    super(stub, type, node);
  }

  public void accept(@NotNull CsvVisitor visitor) {
    visitor.visitEntry(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof CsvVisitor) accept((CsvVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public CsvIdentifier getIdentifier() {
    return notNullChild(PsiTreeUtil.getChildOfType(this, CsvIdentifier.class));
  }

  @Override
  @Nullable
  public CsvProperties getProperties() {
    return PsiTreeUtil.getChildOfType(this, CsvProperties.class);
  }

  @Override
  @NotNull
  public String getNamespace() {
    return CsvPsiImplUtil.getNamespace(this);
  }

  @Override
  @NotNull
  public String getPath() {
    return CsvPsiImplUtil.getPath(this);
  }

  @Override
  @Nullable
  public String getKey() {
    return CsvPsiImplUtil.getKey(this);
  }

}
