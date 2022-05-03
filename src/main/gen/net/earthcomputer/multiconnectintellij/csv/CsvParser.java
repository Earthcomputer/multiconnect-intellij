// This is a generated file. Not intended for manual editing.
package net.earthcomputer.multiconnectintellij.csv;

import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiBuilder.Marker;
import static net.earthcomputer.multiconnectintellij.csv.psi.CsvTypes.*;
import static com.intellij.lang.parser.GeneratedParserUtilBase.*;
import com.intellij.psi.tree.IElementType;
import com.intellij.lang.ASTNode;
import com.intellij.psi.tree.TokenSet;
import com.intellij.lang.PsiParser;
import com.intellij.lang.LightPsiParser;

@SuppressWarnings({"SimplifiableIfStatement", "UnusedAssignment"})
public class CsvParser implements PsiParser, LightPsiParser {

  public ASTNode parse(IElementType t, PsiBuilder b) {
    parseLight(t, b);
    return b.getTreeBuilt();
  }

  public void parseLight(IElementType t, PsiBuilder b) {
    boolean r;
    b = adapt_builder_(t, b, this, null);
    Marker m = enter_section_(b, 0, _COLLAPSE_, null);
    r = parse_root_(t, b);
    exit_section_(b, 0, m, t, r, true, TRUE_CONDITION);
  }

  protected boolean parse_root_(IElementType t, PsiBuilder b) {
    return parse_root_(t, b, 0);
  }

  static boolean parse_root_(IElementType t, PsiBuilder b, int l) {
    return csvFile(b, l + 1);
  }

  /* ********************************************************** */
  // whitespace_* header ((COMMENT|SPACE|WHITESPACE)? NEWLINE whitespace_* row)* whitespace_*
  static boolean csvFile(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "csvFile")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = csvFile_0(b, l + 1);
    r = r && header(b, l + 1);
    r = r && csvFile_2(b, l + 1);
    r = r && csvFile_3(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // whitespace_*
  private static boolean csvFile_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "csvFile_0")) return false;
    while (true) {
      int c = current_position_(b);
      if (!whitespace_(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "csvFile_0", c)) break;
    }
    return true;
  }

  // ((COMMENT|SPACE|WHITESPACE)? NEWLINE whitespace_* row)*
  private static boolean csvFile_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "csvFile_2")) return false;
    while (true) {
      int c = current_position_(b);
      if (!csvFile_2_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "csvFile_2", c)) break;
    }
    return true;
  }

  // (COMMENT|SPACE|WHITESPACE)? NEWLINE whitespace_* row
  private static boolean csvFile_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "csvFile_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = csvFile_2_0_0(b, l + 1);
    r = r && consumeToken(b, NEWLINE);
    r = r && csvFile_2_0_2(b, l + 1);
    r = r && row(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (COMMENT|SPACE|WHITESPACE)?
  private static boolean csvFile_2_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "csvFile_2_0_0")) return false;
    csvFile_2_0_0_0(b, l + 1);
    return true;
  }

  // COMMENT|SPACE|WHITESPACE
  private static boolean csvFile_2_0_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "csvFile_2_0_0_0")) return false;
    boolean r;
    r = consumeToken(b, COMMENT);
    if (!r) r = consumeToken(b, SPACE);
    if (!r) r = consumeToken(b, WHITESPACE);
    return r;
  }

  // whitespace_*
  private static boolean csvFile_2_0_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "csvFile_2_0_2")) return false;
    while (true) {
      int c = current_position_(b);
      if (!whitespace_(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "csvFile_2_0_2", c)) break;
    }
    return true;
  }

  // whitespace_*
  private static boolean csvFile_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "csvFile_3")) return false;
    while (true) {
      int c = current_position_(b);
      if (!whitespace_(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "csvFile_3", c)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // identifier properties?
  public static boolean entry(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "entry")) return false;
    if (!nextTokenIs(b, STRING)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = identifier(b, l + 1);
    r = r && entry_1(b, l + 1);
    exit_section_(b, m, ENTRY, r);
    return r;
  }

  // properties?
  private static boolean entry_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "entry_1")) return false;
    properties(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // stringValue (SPACE stringValue)*
  public static boolean header(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "header")) return false;
    if (!nextTokenIs(b, STRING)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = stringValue(b, l + 1);
    r = r && header_1(b, l + 1);
    exit_section_(b, m, HEADER, r);
    return r;
  }

  // (SPACE stringValue)*
  private static boolean header_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "header_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!header_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "header_1", c)) break;
    }
    return true;
  }

  // SPACE stringValue
  private static boolean header_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "header_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, SPACE);
    r = r && stringValue(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // (stringValue COLON)? stringValue
  public static boolean identifier(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "identifier")) return false;
    if (!nextTokenIs(b, STRING)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = identifier_0(b, l + 1);
    r = r && stringValue(b, l + 1);
    exit_section_(b, m, IDENTIFIER, r);
    return r;
  }

  // (stringValue COLON)?
  private static boolean identifier_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "identifier_0")) return false;
    identifier_0_0(b, l + 1);
    return true;
  }

  // stringValue COLON
  private static boolean identifier_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "identifier_0_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = stringValue(b, l + 1);
    r = r && consumeToken(b, COLON);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // stringValue EQUALS stringValue
  public static boolean kvPair(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "kvPair")) return false;
    if (!nextTokenIs(b, STRING)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = stringValue(b, l + 1);
    r = r && consumeToken(b, EQUALS);
    r = r && stringValue(b, l + 1);
    exit_section_(b, m, KV_PAIR, r);
    return r;
  }

  /* ********************************************************** */
  // OPEN_BRACKET kvPair (COMMA kvPair)* CLOSE_BRACKET
  public static boolean properties(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "properties")) return false;
    if (!nextTokenIs(b, OPEN_BRACKET)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, OPEN_BRACKET);
    r = r && kvPair(b, l + 1);
    r = r && properties_2(b, l + 1);
    r = r && consumeToken(b, CLOSE_BRACKET);
    exit_section_(b, m, PROPERTIES, r);
    return r;
  }

  // (COMMA kvPair)*
  private static boolean properties_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "properties_2")) return false;
    while (true) {
      int c = current_position_(b);
      if (!properties_2_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "properties_2", c)) break;
    }
    return true;
  }

  // COMMA kvPair
  private static boolean properties_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "properties_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && kvPair(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // entry (SPACE entry)*
  public static boolean row(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "row")) return false;
    if (!nextTokenIs(b, STRING)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = entry(b, l + 1);
    r = r && row_1(b, l + 1);
    exit_section_(b, m, ROW, r);
    return r;
  }

  // (SPACE entry)*
  private static boolean row_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "row_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!row_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "row_1", c)) break;
    }
    return true;
  }

  // SPACE entry
  private static boolean row_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "row_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, SPACE);
    r = r && entry(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // STRING
  public static boolean stringValue(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "stringValue")) return false;
    if (!nextTokenIs(b, STRING)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, STRING);
    exit_section_(b, m, STRING_VALUE, r);
    return r;
  }

  /* ********************************************************** */
  // COMMENT|NEWLINE|SPACE|WHITESPACE
  static boolean whitespace_(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "whitespace_")) return false;
    boolean r;
    r = consumeToken(b, COMMENT);
    if (!r) r = consumeToken(b, NEWLINE);
    if (!r) r = consumeToken(b, SPACE);
    if (!r) r = consumeToken(b, WHITESPACE);
    return r;
  }

}
