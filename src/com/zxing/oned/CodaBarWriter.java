package com.zxing.oned;

import java.util.Arrays;

public final class CodaBarWriter extends OneDimensionalCodeWriter {

  private static final char[] START_CHARS = {'A', 'B', 'C', 'D'};
  private static final char[] END_CHARS = {'T', 'N', '*', 'E'};

  @Override
  public boolean[] encode(String contents) {

    // Verify input and calculate decoded length.
    if (!CodaBarReader.arrayContains(START_CHARS, Character.toUpperCase(contents.charAt(0)))) {
      throw new IllegalArgumentException(
          "Codabar should start with one of the following: " + Arrays.toString(START_CHARS));
    }
    if (!CodaBarReader.arrayContains(END_CHARS, Character.toUpperCase(contents.charAt(contents.length() - 1)))) {
      throw new IllegalArgumentException(
          "Codabar should end with one of the following: " + Arrays.toString(END_CHARS));
    }
    // The start character and the end character are decoded to 10 length each.
    int resultLength = 20;
    char[] charsWhichAreTenLengthEachAfterDecoded = {'/', ':', '+', '.'};
    for (int i = 1; i < contents.length() - 1; i++) {
      if (Character.isDigit(contents.charAt(i)) || contents.charAt(i) == '-'
          || contents.charAt(i) == '$') {
        resultLength += 9;
      } else if (CodaBarReader.arrayContains(
          charsWhichAreTenLengthEachAfterDecoded, contents.charAt(i))) {
        resultLength += 10;
      } else {
        throw new IllegalArgumentException("Cannot encode : '" + contents.charAt(i) + '\'');
      }
    }
    // A blank is placed between each character.
    resultLength += contents.length() - 1;

    boolean[] result = new boolean[resultLength];
    int position = 0;
    for (int index = 0; index < contents.length(); index++) {
      char c = Character.toUpperCase(contents.charAt(index));
      if (index == contents.length() - 1) {
        // The end chars are not in the CodaBarReader.ALPHABET.
        switch (c) {
          case 'T':
            c = 'A';
            break;
          case 'N':
            c = 'B';
            break;
          case '*':
            c = 'C';
            break;
          case 'E':
            c = 'D';
            break;
        }
      }
      int code = 0;
      for (int i = 0; i < CodaBarReader.ALPHABET.length; i++) {
        // Found any, because I checked above.
        if (c == CodaBarReader.ALPHABET[i]) {
          code = CodaBarReader.CHARACTER_ENCODINGS[i];
          break;
        }
      }
      boolean color = true;
      int counter = 0;
      int bit = 0;
      while (bit < 7) { // A character consists of 7 digit.
        result[position] = color;
        position++;
        if (((code >> (6 - bit)) & 1) == 0 || counter == 1) {
          color = !color; // Flip the color.
          bit++;
          counter = 0;
        } else {
          counter++;
        }
      }
      if (index < contents.length() - 1) {
        result[position] = false;
        position++;
      }
    }
    return result;
  }
}

