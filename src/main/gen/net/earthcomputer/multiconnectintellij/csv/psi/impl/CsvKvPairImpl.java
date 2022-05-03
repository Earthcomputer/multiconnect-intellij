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

public class CsvKvPairImpl extends ASTWrapperPsiElement implements CsvKvPair {

  public CsvKvPairImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull CsvVisitor visitor) {
    visitor.visitKvPair(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof CsvVisitor) accept((CsvVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public List<CsvStringValue> getStringElements() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, CsvStringValue.class);
  }

  @Override
  @NotNull
  public CsvStringValue getKeyElement() {
    List<CsvStringValue> p1 = getStringElements();
    return p1.get(0);
  }

  @Override
  @Nullable
  public CsvStringValue getValueElement() {
    List<CsvStringValue> p1 = getStringElements();
    return p1.size() < 2 ? null : p1.get(1);
  }

  @Override
  @NotNull
  public String getKey() {
    return CsvPsiImplUtil.getKey(this);
  }

  @Override
  @Nullable
  public String getValue() {
    return CsvPsiImplUtil.getValue(this);
  }

}
