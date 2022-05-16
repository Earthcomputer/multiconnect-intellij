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

public class CsvIdentifierImpl extends ASTWrapperPsiElement implements CsvIdentifier {

  public CsvIdentifierImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull CsvVisitor visitor) {
    visitor.visitIdentifier(this);
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
  @Nullable
  public CsvStringValue getNamespaceElement() {
    return CsvPsiImplUtil.getNamespaceElement(this);
  }

  @Override
  @NotNull
  public CsvStringValue getPathElement() {
    return CsvPsiImplUtil.getPathElement(this);
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
  @NotNull
  public String getNormalizedString() {
    return CsvPsiImplUtil.getNormalizedString(this);
  }

  @Override
  @NotNull
  public String getName() {
    return CsvPsiImplUtil.getName(this);
  }

  @Override
  @NotNull
  public CsvIdentifier setName(@NotNull String name) {
    return CsvPsiImplUtil.setName(this, name);
  }

}
