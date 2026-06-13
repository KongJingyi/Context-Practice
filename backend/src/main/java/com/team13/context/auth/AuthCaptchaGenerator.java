package com.team13.context.auth;

import lombok.Getter;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Locale;
import javax.imageio.ImageIO;

/**
 * 图形验证码：Java2D 生成 PNG Base64，兼容 H5 / 微信小程序 image 组件。
 */
public final class AuthCaptchaGenerator {

    private static final String CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    private AuthCaptchaGenerator() {
    }

    public static String randomCode(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(CHARS.charAt(RANDOM.nextInt(CHARS.length())));
        }
        return sb.toString();
    }

    public static String randomSmsCode() {
        return String.format(Locale.CHINA, "%06d", RANDOM.nextInt(1_000_000));
    }

    public static CaptchaImage toCaptchaImage(String code, int width, int height) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        try {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(new Color(224, 242, 254));
            g.fillRect(0, 0, width, height);

            for (int i = 0; i < 6; i++) {
                g.setColor(randomColor(100, 180));
                g.setStroke(new BasicStroke(1.2f));
                int x1 = RANDOM.nextInt(width);
                int y1 = RANDOM.nextInt(height);
                int x2 = RANDOM.nextInt(width);
                int y2 = RANDOM.nextInt(height);
                g.drawLine(x1, y1, x2, y2);
            }

            for (int i = 0; i < 30; i++) {
                g.setColor(randomColor(120, 200));
                g.fillOval(RANDOM.nextInt(width), RANDOM.nextInt(height), 2, 2);
            }

            int charWidth = width / (code.length() + 1);
            for (int i = 0; i < code.length(); i++) {
                char ch = code.charAt(i);
                int x = charWidth * (i + 1) - 6;
                int y = height / 2 + 8 + RANDOM.nextInt(5);
                double angle = Math.toRadians(-15 + RANDOM.nextInt(30));
                g.setFont(new Font("Arial", Font.BOLD, 22 + RANDOM.nextInt(4)));
                g.setColor(randomColor(20, 120));
                g.rotate(angle, x, y);
                g.drawString(String.valueOf(ch), x, y);
                g.rotate(-angle, x, y);
            }

            g.setColor(new Color(59, 130, 246, 80));
            g.drawRoundRect(0, 0, width - 1, height - 1, 8, 8);
        } finally {
            g.dispose();
        }

        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "png", baos);
            String base64 = Base64.getEncoder().encodeToString(baos.toByteArray());
            return new CaptchaImage("data:image/png;base64," + base64, code);
        } catch (Exception e) {
            throw new IllegalStateException("生成图形验证码失败", e);
        }
    }

    private static Color randomColor(int min, int max) {
        int range = Math.max(1, max - min);
        return new Color(min + RANDOM.nextInt(range), min + RANDOM.nextInt(range), min + RANDOM.nextInt(range));
    }

    @Getter
    public static final class CaptchaImage {
        private final String dataUrl;
        private final String code;

        public CaptchaImage(String dataUrl, String code) {
            this.dataUrl = dataUrl;
            this.code = code;
        }
    }
}
