package wdllmh.checkit;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * 这个类包含一些图片处理的辅助函数，因为太多了独立封装一个函数
 */
public class ImageUtils {

    /**
     * 处理带有透明图层的图片，由于jpg不支持透明图层，需要用白色填充
     * @param image 输入的io类型图片
     * @return 处理过的图片
     */
    public static BufferedImage handleTransparency(BufferedImage image) {
        if (image.getColorModel().hasAlpha()) {
            BufferedImage opaqueImage = new BufferedImage(
                    image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = opaqueImage.createGraphics();
            g2d.setColor(Color.WHITE);
            g2d.fillRect(0, 0, image.getWidth(), image.getHeight());
            g2d.drawImage(image, 0, 0, null);
            g2d.dispose();
            return opaqueImage;
        }
        return image;
    }

    /**
     * 将图片尺寸缩小到长宽均符合要求
     * @param image 输入io类型图片
     * @param maxWidth 最大宽度
     * @param maxHeight 最大高度
     * @return 缩放过的io类型图片
     */
    public static BufferedImage resizeImage(BufferedImage image, int maxWidth, int maxHeight) {
        // 计算最佳缩放比例
        double ratio = Math.min(
                (double)maxWidth / image.getWidth(),
                (double)maxHeight / image.getHeight()
        );

        if (ratio >= 1.0) return image; // 不需要缩放

        int newWidth = (int) (image.getWidth() * ratio);
        int newHeight = (int) (image.getHeight() * ratio);

        BufferedImage resizedImage = new BufferedImage(newWidth, newHeight, image.getType());
        Graphics2D g2d = resizedImage.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2d.drawImage(image, 0, 0, newWidth, newHeight, null);
        g2d.dispose();
        return resizedImage;
    }


}
