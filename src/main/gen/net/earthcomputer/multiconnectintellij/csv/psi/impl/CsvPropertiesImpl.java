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
import java.util.Map;

public class CsvPropertiesImpl extends ASTWrapperPsiElement implements CsvProperties {

  public CsvPropertiesImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull CsvVisitor visitor) {
    visitor.visitProperties(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof CsvVisitor) accept((CsvVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public List<CsvKvPair> getKvPairList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, CsvKvPair.class);
  }

  @Override
  @Nullable
  public String getProperty(@NotNull String key) {
    return CsvPsiImplUtil.getProperty(this, key);
  }

  @Override
  @NotNull
  public Map<String, String> toMap() {
    return CsvPsiImplUtil.toMap(this);
  }

}
