package com.zxing.maxicode;
import com.zxing.BarcodeFormat;
import com.zxing.BinaryBitmap;
import com.zxing.ChecksumException;
import com.zxing.DecodeHintType;
import com.zxing.FormatException;
import com.zxing.NotFoundException;
import com.zxing.Reader;
import com.zxing.Result;
import com.zxing.ResultMetadataType;
import com.zxing.ResultPoint;
import com.zxing.common.BitMatrix;
import com.zxing.common.DecoderResult;
import com.zxing.maxicode.decoder.Decoder;

import java.util.Map;

public final class MaxiCodeReader implements Reader {

  private static final ResultPoint[] NO_POINTS = new ResultPoint[0];
  private static final int MATRIX_WIDTH = 30;
  private static final int MATRIX_HEIGHT = 33;

  private final Decoder decoder = new Decoder();

  Decoder getDecoder() {
    return decoder;
  }

  /**
   * Locates and decodes a MaxiCode in an image.
   *
   * @return a String representing the content encoded by the MaxiCode
   * @throws NotFoundException if a MaxiCode cannot be found
   * @throws FormatException if a MaxiCode cannot be decoded
   * @throws ChecksumException if error correction fails
   */
 // @Override
  public Result decode(BinaryBitmap image) throws NotFoundException, ChecksumException, FormatException {
    return decode(image, null);
  }

//  @Override
  public Result decode(BinaryBitmap image, Map<DecodeHintType,?> hints)
      throws NotFoundException, ChecksumException, FormatException {
    DecoderResult decoderResult;
    if (hints != null && hints.containsKey(DecodeHintType.PURE_BARCODE)) {
      BitMatrix bits = extractPureBits(image.getBlackMatrix());
      decoderResult = decoder.decode(bits, hints);
    } else {
      throw NotFoundException.getNotFoundInstance();
    }

    ResultPoint[] points = NO_POINTS;
    Result result = new Result(decoderResult.getText(), decoderResult.getRawBytes(), points, BarcodeFormat.MAXICODE);

    String ecLevel = decoderResult.getECLevel();
    if (ecLevel != null) {
      result.putMetadata(ResultMetadataType.ERROR_CORRECTION_LEVEL, ecLevel);
    }
    return result;
  }

//  @Override
  public void reset() {
    // do nothing
  }

  /**
   * This method detects a code in a "pure" image -- that is, pure monochrome image
   * which contains only an unrotated, unskewed, image of a code, with some white border
   * around it. This is a specialized method that works exceptionally fast in this special
   * case.
   *
   * @see com.google.zxing.datamatrix.DataMatrixReader#extractPureBits(BitMatrix)
   * @see com.google.zxing.qrcode.QRCodeReader#extractPureBits(BitMatrix)
   */
  private static BitMatrix extractPureBits(BitMatrix image) throws NotFoundException {
    
    int[] enclosingRectangle = image.getEnclosingRectangle();
    if (enclosingRectangle == null) {
      throw NotFoundException.getNotFoundInstance();
    }
    
    int left = enclosingRectangle[0];
    int top = enclosingRectangle[1];
    int width = enclosingRectangle[2];
    int height = enclosingRectangle[3];

    // Now just read off the bits
    BitMatrix bits = new BitMatrix(MATRIX_WIDTH, MATRIX_HEIGHT);
    for (int y = 0; y < MATRIX_HEIGHT; y++) {
      int iy = top + (y * height + height / 2) / MATRIX_HEIGHT;
      for (int x = 0; x < MATRIX_WIDTH; x++) {
        int ix = left + (x * width + width / 2 + (y & 0x01) *  width / 2) / MATRIX_WIDTH;
        if (image.get(ix, iy)) {
          bits.set(x, y);
        }
      }
    }
    return bits;
  }

}
