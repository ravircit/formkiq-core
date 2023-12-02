/**
 * MIT License
 * 
 * Copyright (c) 2018 - 2020 FormKiQ
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.formkiq.module.lambda.ocr.docx;

import java.io.File;
import java.io.IOException;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import com.formkiq.aws.dynamodb.objects.MimeType;
import com.formkiq.module.lambdaservices.AwsServiceCache;
import com.formkiq.module.ocr.FormatConverter;
import com.formkiq.module.ocr.FormatConverterResult;
import com.formkiq.module.ocr.OcrScanStatus;
import com.formkiq.module.ocr.OcrSqsMessage;

/**
 * DOCX {@link FormatConverter}.
 */
public class DocxFormatConverter implements FormatConverter {

  @Override
  public boolean isSupported(final OcrSqsMessage sqsMessage, final MimeType mineType) {
    return MimeType.MIME_DOCX.equals(mineType);
  }

  @Override
  public FormatConverterResult convert(final AwsServiceCache awsServices,
      final OcrSqsMessage sqsMessage, final File file) throws IOException {

    try {

      XWPFDocument doc = new XWPFDocument(OPCPackage.open(file));

      try (XWPFWordExtractor ext = new XWPFWordExtractor(doc)) {
        String text = ext.getText();
        return new FormatConverterResult().text(text).status(OcrScanStatus.SUCCESSFUL);
      }
    } catch (InvalidFormatException e) {
      throw new IOException(e);
    }
  }

}
