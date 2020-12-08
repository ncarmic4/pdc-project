package pdc.project;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.ByteBuffer;

public class Util {
    public static String[] imageNames = new String[] { "galaxy.jpg" };

    public static void sendImage(String path, String type, OutputStream out) throws IOException {
        BufferedImage image = ImageIO.read(new File(path));
        sendImage(image, type, out);
    }

    public static void sendImage(BufferedImage image, String type, OutputStream out) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, type, baos);

        byte[] size = ByteBuffer.allocate(4).putInt(baos.size()).array();
        out.write(size);
        baos.writeTo(out);
        baos.flush();
        out.flush();
    }

    public static BufferedImage receiveImage(InputStream in) throws IOException, InterruptedException {
        byte[] sizeArr = new byte[4];
        while (in.available() < 4) {
            Thread.sleep(100);
        }
        in.read(sizeArr);
        int size = ByteBuffer.wrap(sizeArr).asIntBuffer().get();
        byte[] imageArr = new byte[size];
        while (in.available() < size) {
            Thread.sleep(100);
        }
        in.read(imageArr);
        return ImageIO.read(new ByteArrayInputStream(imageArr));
    }
}
