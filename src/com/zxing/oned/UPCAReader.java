package com.zxing.oned;

import com.zxing.BarcodeFormat;
import com.zxing.BinaryBitmap;
import com.zxing.ChecksumException;
import com.zxing.DecodeHintType;
import com.zxing.FormatException;
import com.zxing.NotFoundException;
import com.zxing.Result;
import com.zxing.common.BitArray;

import java.util.Map;

public final class UPCAReader extends UPCEANReader {

  private final UPCEANReader ean13Reader = new EAN13Reader();

  @Override
  public Result decodeRow(int rowNumber,
                          BitArray row,
                          int[] startGuardRange,
                          Map<DecodeHintType,?> hints)
      throws NotFoundException, FormatException, ChecksumException {
    return maybeReturnResult(ean13Reader.decodeRow(rowNumber, row, startGuardRange, hints));
  }

  @Override
  public Result decodeRow(int rowNumber, BitArray row, Map<DecodeHintType,?> hints)
      throws NotFoundException, FormatException, ChecksumException {
    return maybeReturnResult(ean13Reader.decodeRow(rowNumber, row, hints));
  }

  @Override
  public Result decode(BinaryBitmap image) throws NotFoundException, FormatException {
    return maybeReturnResult(ean13Reader.decode(image));
  }

  @Override
  public Result decode(BinaryBitmap image, Map<DecodeHintType,?> hints)
      throws NotFoundException, FormatException {
    return maybeReturnResult(ean13Reader.decode(image, hints));
  }

  @Override
  BarcodeFormat getBarcodeFormat() {
    return BarcodeFormat.UPC_A;
  }

  @Override
  protected int decodeMiddle(BitArray row, int[] startRange, StringBuilder resultString)
      throws NotFoundException {
    return ean13Reader.decodeMiddle(row, startRange, resultString);
  }

  private static Result maybeReturnResult(Result result) throws FormatException {
    String text = result.getText();
    if (text.charAt(0) == '0') {
      return new Result(text.substring(1), null, result.getResultPoints(), BarcodeFormat.UPC_A);
    } else {
      throw FormatException.getFormatInstance();
    }
  }

}
