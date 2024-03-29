package com.zxing.pdf417;

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
import com.zxing.common.DecoderResult;
import com.zxing.multi.MultipleBarcodeReader;
import com.zxing.pdf417.decoder.PDF417ScanningDecoder;
import com.zxing.pdf417.detector.Detector;
import com.zxing.pdf417.detector.PDF417DetectorResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class PDF417Reader implements Reader, MultipleBarcodeReader {

  /**
   * Locates and decodes a PDF417 code in an image.
   *
   * @return a String representing the content encoded by the PDF417 code
   * @throws NotFoundException if a PDF417 code cannot be found,
   * @throws FormatException if a PDF417 cannot be decoded
   */
//  @Override
  public Result decode(BinaryBitmap image) throws NotFoundException, FormatException, ChecksumException {
    return decode(image, null);
  }

//  @Override
  public Result decode(BinaryBitmap image, Map<DecodeHintType,?> hints) throws NotFoundException, FormatException,
      ChecksumException {
    Result[] result = decode(image, hints, false);
    if (result == null || result.length == 0 || result[0] == null) {
      throw NotFoundException.getNotFoundInstance();
    }
    return result[0];
  }

 // @Override
  public Result[] decodeMultiple(BinaryBitmap image) throws NotFoundException {
    return decodeMultiple(image, null);
  }

//  @Override
  public Result[] decodeMultiple(BinaryBitmap image, Map<DecodeHintType,?> hints) throws NotFoundException {
    try {
      return decode(image, hints, true);
    } catch (FormatException ignored) {
      throw NotFoundException.getNotFoundInstance();
    } catch (ChecksumException ignored) {
      throw NotFoundException.getNotFoundInstance();
    }
  }

  private static Result[] decode(BinaryBitmap image, Map<DecodeHintType, ?> hints, boolean multiple) 
      throws NotFoundException, FormatException, ChecksumException {
    List<Result> results = new ArrayList<Result>();
    PDF417DetectorResult detectorResult = Detector.detect(image, hints, multiple);
    for (ResultPoint[] points : detectorResult.getPoints()) {
      DecoderResult decoderResult = PDF417ScanningDecoder.decode(detectorResult.getBits(), points[4], points[5],
          points[6], points[7], getMinCodewordWidth(points), getMaxCodewordWidth(points));
      if (decoderResult == null) {
        throw NotFoundException.getNotFoundInstance();
      }
      Result result = new Result(decoderResult.getText(), decoderResult.getRawBytes(), points, BarcodeFormat.PDF_417);
      result.putMetadata(ResultMetadataType.ERROR_CORRECTION_LEVEL, decoderResult.getECLevel());
      PDF417ResultMetadata pdf417ResultMetadata = (PDF417ResultMetadata) decoderResult.getOther();
      if (pdf417ResultMetadata != null) {
        result.putMetadata(ResultMetadataType.PDF417_EXTRA_METADATA, pdf417ResultMetadata);
      }
      results.add(result);
    }
    return results.toArray(new Result[results.size()]);
  }

  private static int getMaxWidth(ResultPoint p1, ResultPoint p2) {
    if (p1 == null || p2 == null) {
      return 0;
    }
    return (int) Math.abs(p1.getX() - p2.getX());
  }

  private static int getMinWidth(ResultPoint p1, ResultPoint p2) {
    if (p1 == null || p2 == null) {
      return Integer.MAX_VALUE;
    }
    return (int) Math.abs(p1.getX() - p2.getX());
  }

  private static int getMaxCodewordWidth(ResultPoint[] p) {
    return Math.max(
        Math.max(getMaxWidth(p[0], p[4]), getMaxWidth(p[6], p[2]) * PDF417Common.MODULES_IN_CODEWORD /
            PDF417Common.MODULES_IN_STOP_PATTERN),
        Math.max(getMaxWidth(p[1], p[5]), getMaxWidth(p[7], p[3]) * PDF417Common.MODULES_IN_CODEWORD /
            PDF417Common.MODULES_IN_STOP_PATTERN));
  }

  private static int getMinCodewordWidth(ResultPoint[] p) {
    return Math.min(
        Math.min(getMinWidth(p[0], p[4]), getMinWidth(p[6], p[2]) * PDF417Common.MODULES_IN_CODEWORD /
            PDF417Common.MODULES_IN_STOP_PATTERN),
        Math.min(getMinWidth(p[1], p[5]), getMinWidth(p[7], p[3]) * PDF417Common.MODULES_IN_CODEWORD /
            PDF417Common.MODULES_IN_STOP_PATTERN));
  }

//  @Override
  public void reset() {
    // nothing needs to be reset
  }

}
