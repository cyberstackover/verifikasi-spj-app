package com.zxing.qrcode.decoder;

import com.zxing.ChecksumException;
import com.zxing.DecodeHintType;
import com.zxing.FormatException;
import com.zxing.common.BitMatrix;
import com.zxing.common.DecoderResult;
import com.zxing.common.reedsolomon.GenericGF;
import com.zxing.common.reedsolomon.ReedSolomonDecoder;
import com.zxing.common.reedsolomon.ReedSolomonException;

import java.util.Map;


public final class Decoder {

  private final ReedSolomonDecoder rsDecoder;

  public Decoder() {
    rsDecoder = new ReedSolomonDecoder(GenericGF.QR_CODE_FIELD_256);
  }

  public DecoderResult decode(boolean[][] image) throws ChecksumException, FormatException {
    return decode(image, null);
  }

  /**
   * <p>Convenience method that can decode a QR Code represented as a 2D array of booleans.
   * "true" is taken to mean a black module.</p>
   *
   * @param image booleans representing white/black QR Code modules
   * @return text and bytes encoded within the QR Code
   * @throws FormatException if the QR Code cannot be decoded
   * @throws ChecksumException if error correction fails
   */
  public DecoderResult decode(boolean[][] image, Map<DecodeHintType,?> hints)
      throws ChecksumException, FormatException {
    int dimension = image.length;
    BitMatrix bits = new BitMatrix(dimension);
    for (int i = 0; i < dimension; i++) {
      for (int j = 0; j < dimension; j++) {
        if (image[i][j]) {
          bits.set(j, i);
        }
      }
    }
    return decode(bits, hints);
  }

  public DecoderResult decode(BitMatrix bits) throws ChecksumException, FormatException {
    return decode(bits, null);
  }

  /**
   * <p>Decodes a QR Code represented as a {@link BitMatrix}. A 1 or "true" is taken to mean a black module.</p>
   *
   * @param bits booleans representing white/black QR Code modules
   * @return text and bytes encoded within the QR Code
   * @throws FormatException if the QR Code cannot be decoded
   * @throws ChecksumException if error correction fails
   */
  public DecoderResult decode(BitMatrix bits, Map<DecodeHintType,?> hints)
      throws FormatException, ChecksumException {

    // Construct a parser and read version, error-correction level
    BitMatrixParser parser = new BitMatrixParser(bits);
    Version version = parser.readVersion();
    ErrorCorrectionLevel ecLevel = parser.readFormatInformation().getErrorCorrectionLevel();

    // Read codewords
    byte[] codewords = parser.readCodewords();
    // Separate into data blocks
    DataBlock[] dataBlocks = DataBlock.getDataBlocks(codewords, version, ecLevel);

    // Count total number of data bytes
    int totalBytes = 0;
    for (DataBlock dataBlock : dataBlocks) {
      totalBytes += dataBlock.getNumDataCodewords();
    }
    byte[] resultBytes = new byte[totalBytes];
    int resultOffset = 0;

    // Error-correct and copy data blocks together into a stream of bytes
    for (DataBlock dataBlock : dataBlocks) {
      byte[] codewordBytes = dataBlock.getCodewords();
      int numDataCodewords = dataBlock.getNumDataCodewords();
      correctErrors(codewordBytes, numDataCodewords);
      for (int i = 0; i < numDataCodewords; i++) {
        resultBytes[resultOffset++] = codewordBytes[i];
      }
    }

    // Decode the contents of that stream of bytes
    return DecodedBitStreamParser.decode(resultBytes, version, ecLevel, hints);
  }

  /**
   * <p>Given data and error-correction codewords received, possibly corrupted by errors, attempts to
   * correct the errors in-place using Reed-Solomon error correction.</p>
   *
   * @param codewordBytes data and error correction codewords
   * @param numDataCodewords number of codewords that are data bytes
   * @throws ChecksumException if error correction fails
   */
  private void correctErrors(byte[] codewordBytes, int numDataCodewords) throws ChecksumException {
    int numCodewords = codewordBytes.length;
    // First read into an array of ints
    int[] codewordsInts = new int[numCodewords];
    for (int i = 0; i < numCodewords; i++) {
      codewordsInts[i] = codewordBytes[i] & 0xFF;
    }
    int numECCodewords = codewordBytes.length - numDataCodewords;
    try {
      rsDecoder.decode(codewordsInts, numECCodewords);
    } catch (ReedSolomonException ignored) {
      throw ChecksumException.getChecksumInstance();
    }
    // Copy back into array of bytes -- only need to worry about the bytes that were data
    // We don't care about errors in the error-correction codewords
    for (int i = 0; i < numDataCodewords; i++) {
      codewordBytes[i] = (byte) codewordsInts[i];
    }
  }

}
