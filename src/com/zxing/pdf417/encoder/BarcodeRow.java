package com.zxing.pdf417.encoder;

final class BarcodeRow {

  private final byte[] row;
  //A tacker for position in the bar
  private int currentLocation;

  BarcodeRow(int width) {
    this.row = new byte[width];
    currentLocation = 0;
  }

  void set(int x, byte value) {
    row[x] = value;
  }

  /**
   * Sets a specific location in the bar
   *
   * @param x The location in the bar
   * @param black Black if true, white if false;
   */
  void set(int x, boolean black) {
    row[x] = (byte) (black ? 1 : 0);
  }

  /**
   * @param black A boolean which is true if the bar black false if it is white
   * @param width How many spots wide the bar is.
   */
  void addBar(boolean black, int width) {
    for (int ii = 0; ii < width; ii++) {
      set(currentLocation++, black);
    }
  }

  byte[] getRow() {
    return row;
  }

  /**
   * This function scales the row
   *
   * @param scale How much you want the image to be scaled, must be greater than or equal to 1.
   * @return the scaled row
   */
  byte[] getScaledRow(int scale) {
    byte[] output = new byte[row.length * scale];
    for (int i = 0; i < output.length; i++) {
      output[i] = row[i / scale];
    }
    return output;
  }
}
