// This is a generated file. Not intended for manual editing.
package net.earthcomputer.multiconnectintellij.csv.psi.impl;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import static net.earthcomputer.multiconnectintellij.csv.psi.CsvTypes.*;
import com.intellij.extapi.psi.ASTWrapperPsiElement;
import net.earthcomputer.multiconnectintellij.csv.psi.*;

public class CsvEntryImpl extends ASTWrapperPsiElement implements CsvEntry {

  public CsvEntryImpl(@NotNull ASTNode node) {
    super(node);
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
    return findNotNullChildByClass(CsvIdentifier.class);
  }

  @Override
  @Nullable
  public CsvProperties getProperties() {
    return findChildByClass(CsvProperties.class);
  }

  @Override
  @Nullable
  public String getKey() {
    return CsvPsiImplUtil.getKey(this);
  }

}
