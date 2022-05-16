// This is a generated file. Not intended for manual editing.
package net.earthcomputer.multiconnectintellij.csv.psi;

import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.ContributedReferenceHost;

public class CsvVisitor extends PsiElementVisitor {

  public void visitEntry(@NotNull CsvEntry o) {
    visitPsiElement(o);
  }

  public void visitHeader(@NotNull CsvHeader o) {
    visitPsiElement(o);
  }

  public void visitIdentifier(@NotNull CsvIdentifier o) {
    visitPsiNamedElement(o);
  }

  public void visitKvPair(@NotNull CsvKvPair o) {
    visitPsiElement(o);
  }

  public void visitProperties(@NotNull CsvProperties o) {
    visitPsiElement(o);
  }

  public void visitRow(@NotNull CsvRow o) {
    visitPsiElement(o);
  }

  public void visitStringValue(@NotNull CsvStringValue o) {
    visitContributedReferenceHost(o);
  }

  public void visitContributedReferenceHost(@NotNull ContributedReferenceHost o) {
    visitElement(o);
  }

  public void visitPsiNamedElement(@NotNull PsiNamedElement o) {
    visitElement(o);
  }

  public void visitPsiElement(@NotNull PsiElement o) {
    visitElement(o);
  }

}
