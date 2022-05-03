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

public class CsvHeaderImpl extends ASTWrapperPsiElement implements CsvHeader {

  public CsvHeaderImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull CsvVisitor visitor) {
    visitor.visitHeader(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof CsvVisitor) accept((CsvVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public List<CsvStringValue> getKeyElements() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, CsvStringValue.class);
  }

  @Override
  @NotNull
  public List<String> getKeyNames() {
    return CsvPsiImplUtil.getKeyNames(this);
  }

}
