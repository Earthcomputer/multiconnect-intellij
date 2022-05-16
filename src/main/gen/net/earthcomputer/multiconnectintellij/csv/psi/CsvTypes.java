// This is a generated file. Not intended for manual editing.
package net.earthcomputer.multiconnectintellij.csv.psi;

import com.intellij.psi.tree.IElementType;
import com.intellij.psi.PsiElement;
import com.intellij.lang.ASTNode;
import net.earthcomputer.multiconnectintellij.csv.psi.impl.CsvEntryType;
import net.earthcomputer.multiconnectintellij.csv.psi.impl.CsvHeaderType;
import net.earthcomputer.multiconnectintellij.csv.psi.impl.CsvRowType;
import net.earthcomputer.multiconnectintellij.csv.psi.impl.*;

public interface CsvTypes {

  IElementType ENTRY = new CsvEntryType("ENTRY");
  IElementType HEADER = new CsvHeaderType("HEADER");
  IElementType IDENTIFIER = new CsvElementType("IDENTIFIER");
  IElementType KV_PAIR = new CsvElementType("KV_PAIR");
  IElementType PROPERTIES = new CsvElementType("PROPERTIES");
  IElementType ROW = new CsvRowType("ROW");
  IElementType STRING_VALUE = new CsvElementType("STRING_VALUE");

  IElementType CLOSE_BRACKET = new CsvTokenType("CLOSE_BRACKET");
  IElementType COLON = new CsvTokenType("COLON");
  IElementType COMMA = new CsvTokenType("COMMA");
  IElementType COMMENT = new CsvTokenType("COMMENT");
  IElementType EQUALS = new CsvTokenType("EQUALS");
  IElementType NEWLINE = new CsvTokenType("NEWLINE");
  IElementType OPEN_BRACKET = new CsvTokenType("OPEN_BRACKET");
  IElementType SPACE = new CsvTokenType("SPACE");
  IElementType STRING = new CsvTokenType("STRING");
  IElementType WHITESPACE = new CsvTokenType("WHITESPACE");

  class Factory {
    public static PsiElement createElement(ASTNode node) {
      IElementType type = node.getElementType();
      if (type == ENTRY) {
        return new CsvEntryImpl(node);
      }
      else if (type == HEADER) {
        return new CsvHeaderImpl(node);
      }
      else if (type == IDENTIFIER) {
        return new CsvIdentifierImpl(node);
      }
      else if (type == KV_PAIR) {
        return new CsvKvPairImpl(node);
      }
      else if (type == PROPERTIES) {
        return new CsvPropertiesImpl(node);
      }
      else if (type == ROW) {
        return new CsvRowImpl(node);
      }
      else if (type == STRING_VALUE) {
        return new CsvStringValueImpl(node);
      }
      throw new AssertionError("Unknown element type: " + type);
    }
  }
}
