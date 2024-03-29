package com.zxing.client.result;
import com.zxing.Result;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class URIResultParser extends ResultParser {

  private static final String ALPHANUM_PART = "[a-zA-Z0-9\\-]";
  private static final Pattern URL_WITH_PROTOCOL_PATTERN = Pattern.compile("[a-zA-Z0-9]{2,}:");
  private static final Pattern URL_WITHOUT_PROTOCOL_PATTERN = Pattern.compile(
      '(' + ALPHANUM_PART + "+\\.)+" + ALPHANUM_PART + "{2,}" + // host name elements
      "(:\\d{1,5})?" + // maybe port
      "(/|\\?|$)"); // query, path or nothing

  @Override
  public URIParsedResult parse(Result result) {
    String rawText = getMassagedText(result);
    // We specifically handle the odd "URL" scheme here for simplicity and add "URI" for fun
    // Assume anything starting this way really means to be a URI
    if (rawText.startsWith("URL:") || rawText.startsWith("URI:")) {
      return new URIParsedResult(rawText.substring(4).trim(), null);
    }
    rawText = rawText.trim();
    return isBasicallyValidURI(rawText) ? new URIParsedResult(rawText, null) : null;
  }

  static boolean isBasicallyValidURI(String uri) {
    if (uri.contains(" ")) {
      // Quick hack check for a common case
      return false;
    }
    Matcher m = URL_WITH_PROTOCOL_PATTERN.matcher(uri);
    if (m.find() && m.start() == 0) { // match at start only
      return true;
    }
    m = URL_WITHOUT_PROTOCOL_PATTERN.matcher(uri);
    return m.find() && m.start() == 0;
  }

}