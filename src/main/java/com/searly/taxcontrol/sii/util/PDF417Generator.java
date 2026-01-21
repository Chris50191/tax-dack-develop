package com.searly.taxcontrol.sii.util;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.pdf417.PDF417Writer;
import com.google.zxing.qrcode.QRCodeWriter;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

public class PDF417Generator {
  public static void main(String[] args) throws IOException {
    String content = "<TED version=\"1.0\"><DD><RE>76703286-2</RE><TD>39</TD><F>3919</F><FE>2025-05-25</FE><RR>66666666-6</RR><RSR>SII Boleta</RSR><MNT>500</MNT><IT1>Monto Total</IT1><CAF version=\"1.0\"><DA><RE>76703286-2</RE><RS>COMERCIAL W.ENZO LIMITADA</RS><TD>39</TD><RNG><D>1</D><H>5000</H></RNG><FA>2020-12-29</FA><RSAPK><M>ue9vDF1nKLxKTNhkOHl98jD8/RgDCTwhHG8OZMJScrzbnvkC8kyFEQ+xxy5admQT82cEHGeWYnKtxH81w1TXOQ==</M><E>Aw==</E></RSAPK><IDK>300</IDK></DA><FRMA algoritmo=\"SHA1withRSA\">Ic3iCTcCfrkc4IgtVlfPy2/zbgZ9ldujNU6IYxyCjqD0fRLCyHxdsjkqBC5IWnRWi2M79UWgFOF6xME6OBa4iA==</FRMA></CAF><TSTED>2025-05-25T18:30:58</TSTED></DD><FRMT algoritmo=\"SHA1withRSA\">OFsuM8pLXkfg65bExf1xPEQjnNTXXCCsBEf1ufl2OhanRsAxQnCx5QL7YigFDQc5RrNjbtzRrY2r6H8JozUugw==</FRMT></TED>";
    final InputStream inputStream = generate(content);

    File tempFile = getFile(inputStream);
  }

  public static File getFile(InputStream inputStream) throws IOException {
    File tempFile = File.createTempFile("pdf417_temp", ".png");
    try (OutputStream outputStream = new FileOutputStream(tempFile)) {
      byte[] buffer = new byte[8192];
      int bytesRead;
      while ((bytesRead = inputStream.read(buffer)) != -1) {
        outputStream.write(buffer, 0, bytesRead);
      }
    }
    System.out.println("PDF417码已生成并保存为 " + tempFile);
    return tempFile;
  }

  public static InputStream generate(String content) {
    int width = 300;
    int height = 200;

    Map<EncodeHintType, Object> hints = new HashMap<>();
    hints.put(EncodeHintType.MARGIN, 1);

    try {
      PDF417Writer writer = new PDF417Writer();
      BitMatrix bitMatrix = writer.encode(content, BarcodeFormat.PDF_417, width, height, hints);
      BufferedImage bufferedImage = toBufferedImage(bitMatrix);

      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      ImageIO.write(bufferedImage, "png", baos);
      return new ByteArrayInputStream(baos.toByteArray());
    } catch (WriterException | IOException e) {
      e.printStackTrace();
      return null;
    }
  }

  private static BufferedImage toBufferedImage(BitMatrix matrix) {
    int width = matrix.getWidth();
    int height = matrix.getHeight();
    BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    for (int x = 0; x < width; x++) {
      for (int y = 0; y < height; y++) {
        image.setRGB(x, y, matrix.get(x, y) ? 0xFF000000 : 0xFFFFFFFF);
      }
    }
    return image;
  }
}