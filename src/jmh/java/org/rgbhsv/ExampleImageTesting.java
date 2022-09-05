package org.rgbhsv;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Objects;

public class ExampleImageTesting {

    static int size_image;
    static float[] argb_image;
    static float[] ahsv_image;

    static {
        try (var is = Objects.requireNonNull(JmhMain.class.getResourceAsStream("/example.png"), "failed to find example file")) {
            var image = ImageIO.read(is);

            int w = image.getWidth();
            int h = image.getHeight();

            int[] dataBuffInt = image.getRGB(0, 0, w, h, null, 0, w);
            //System.out.println(dataBuffInt);
            size_image = dataBuffInt.length;
            argb_image = new float[size_image * 4];
            ahsv_image = new float[size_image * 4];

            for (int i = 0; i < size_image; i++) {
                var c = new Color(dataBuffInt[i]);
                argb_image[0 + 4 * i] = c.getAlpha() / 255f;
                argb_image[1 + 4 * i] = c.getRed() / 255f;
                argb_image[2 + 4 * i] = c.getGreen() / 255f;
                argb_image[3 + 4 * i] = c.getBlue() / 255f;
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    //static float[] ahsv2 = new float[4 * size];
}
