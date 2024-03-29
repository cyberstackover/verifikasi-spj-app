package com.zxing.oned;

import com.zxing.BarcodeFormat;
import com.zxing.NotFoundException;
import com.zxing.Result;
import com.zxing.ResultMetadataType;
import com.zxing.ResultPoint;
import com.zxing.common.BitArray;

import java.util.EnumMap;
import java.util.Map;

final class UPCEANExtension2Support {

  private final int[] decodeMiddleCounters = new int[4];
  private final StringBuilder decodeRowStringBuffer = new StringBuilder();

  Result decodeRow(int rowNumber, BitArray row, int[] extensionStartRange) throws NotFoundException {

    StringBuilder result = decodeRowStringBuffer;
    result.setLength(0);
    int end = decodeMiddle(row, extensionStartRange, result);

    String resultString = result.toString();
    Map<ResultMetadataType,Object> extensionData = parseExtensionString(resultString);

    Result extensionResult =
        new Result(resultString,
                   null,
                   new ResultPoint[] {
                       new ResultPoint((extensionStartRange[0] + extensionStartRange[1]) / 2.0f, (float) rowNumber),
                       new ResultPoint((float) end, (float) rowNumber),
                   },
                   BarcodeFormat.UPC_EAN_EXTENSION);
    if (extensionData != null) {
      extensionResult.putAllMetadata(extensionData);
    }
    return extensionResult;
  }

  int decodeMiddle(BitArray row, int[] startRange, StringBuilder resultString) throws NotFoundException {
    int[] counters = decodeMiddleCounters;
    counters[0] = 0;
    counters[1] = 0;
    counters[2] = 0;
    counters[3] = 0;
    int end = row.getSize();
    int rowOffset = startRange[1];

    int checkParity = 0;

    for (int x = 0; x < 2 && rowOffset < end; x++) {
      int bestMatch = UPCEANReader.decodeDigit(row, counters, rowOffset, UPCEANReader.L_AND_G_PATTERNS);
      resultString.append((char) ('0' + bestMatch % 10));
      for (int counter : counters) {
        rowOffset += counter;
      }
      if (bestMatch >= 10) {
        checkParity |= 1 << (1 - x);
      }
      if (x != 1) {
        // Read off separator if not last
        rowOffset = row.getNextSet(rowOffset);
        rowOffset = row.getNextUnset(rowOffset);
      }
    }

    if (resultString.length() != 2) {
      throw NotFoundException.getNotFoundInstance();
    }

    if (Integer.parseInt(resultString.toString()) % 4 != checkParity) {
      throw NotFoundException.getNotFoundInstance();
    }
    
    return rowOffset;
  }

  /**
   * @param raw raw content of extension
   * @return formatted interpretation of raw content as a {@link Map} mapping
   *  one {@link ResultMetadataType} to appropriate value, or {@code null} if not known
   */
  private static Map<ResultMetadataType,Object> parseExtensionString(String raw) {
    if (raw.length() != 2) {
      return null;
    }
    Map<ResultMetadataType,Object> result = new EnumMap<ResultMetadataType,Object>(ResultMetadataType.class);
    result.put(ResultMetadataType.ISSUE_NUMBER, Integer.valueOf(raw));
    return result;
  }

}
