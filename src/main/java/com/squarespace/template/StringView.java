package com.squarespace.template;


/**
 * Tracks the location of a subsequence in a parent String. Used to avoid excessive copies
 * of Strings when processing templates, as they will consist of large blocks of characters
 * which are simply copied verbatim to the output.
 * 
 * For example, replace this:
 * 
 *   String s1 = "template data";
 *   String s2 = s1.substring(1, 4);
 *    ...
 *   StringBuffer buffer = new StringBuffer();
 *   buf.append(s2);
 *   
 * with this:
 * 
 *   String s1 = "template data";
 *   StringView s2 = new StringView(s1, 1, 4);
 *    ...
 *   StringBuffer buffer = new StringBuffer();
 *   buf.append(s2.getData(), s2.start(), s2.end());
 *   
 * Speeds things up by about 20-30%, depending on sequence length, and for the JSONT 
 * engine greatly reduces the creation of intermediate String/char[] objects, thus
 * reducing pressure on the garbage collector.
 */
public class StringView implements CharSequence {

  
  private String str;
  
  private int start;

  private int end;
  
  private int hashVal;
  
  private String repr;
  
  public StringView(String data) {
    this(data, 0, data.length());
  }
  
  public StringView(String str, int start, int end) {
    this.str = str;
    this.start = Math.max(start, 0);
    this.end = Math.min(end, str.length());
  }

  public String data() {
    return str;
  }
  
  public int start() {
    return this.start;
  }
  
  public int end() {
    return this.end;
  }
  
  public char lastChar() {
    return str.charAt(end - 1);
  }
  
  /**
   * This creates an instance with a different window over the same string,
   */
  public StringView subview(int start0, int end0) {
    return new StringView(str, start + start0, start + end0);
  }

  public String repr() {
    if (repr == null) {
      repr = str.substring(this.start, this.end);
    }
    return repr;
  }

  @Override
  public String toString() {
    return repr();
  }
  
  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof StringView)) {
      return false;
    }
    StringView other = (StringView) obj;
    int len = length();
    if (len != other.length()) {
      return false;
    }
    String s1 = str;
    String s2 = other.str;
    int start1 = start;
    int start2 = other.start;
    while (start1 < end && start2 < other.end) {
      if (s1.charAt(start1) != s2.charAt(start2)) {
        return false;
      }
      start1++;
      start2++;
    }
    return true;
  }

  /**
   * A modified hash function from older JDK. The initial value is a large prime.
   * The value 31 is a small prime.
   */
  @Override
  public int hashCode() {
    if (hashVal == 0) {
      int h = 0x01000193;
      for (int i = start; i < end; i++) {
        h = 31 * h + str.charAt(i);
      }
      if (h == 0) {
        h++;
      }
      hashVal = h;
    }
    return hashVal;
  }

  
  @Override
  public int length() {
    return end - start;
  }

  @Override
  public char charAt(int index) {
    return str.charAt(start + index);
  }

  @Override
  public CharSequence subSequence(int start, int end) {
    return subview(start, end);
  }
    
}
