package com.saasxx.framework.data;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Random;

import javax.imageio.ImageIO;

import com.saasxx.framework.Lang;

/**
 * 图片验证码生成工具类
 * 
 * @author lujijiang
 *
 */
public class Captchas {
	public static class Captcha {
		int width;
		int height;
		int fontHeight;
		int size;
		int interferenceCount;
		int interferenceLength;
		char[] chars;

		/**
		 * 设置图片宽度
		 * 
		 * @param width
		 * @return
		 */
		public Captcha setWidth(int width) {
			this.width = width;
			return this;
		}

		/**
		 * 设置图片高度
		 * 
		 * @param height
		 * @return
		 */
		public Captcha setHeight(int height) {
			this.height = height;
			return this;
		}

		/**
		 * 设置字体高度
		 * 
		 * @param fontHeight
		 * @return
		 */
		public Captcha setFontHeight(int fontHeight) {
			this.fontHeight = fontHeight;
			return this;
		}

		/**
		 * 设置验证码长度
		 * 
		 * @param size
		 * @return
		 */
		public Captcha setSize(int size) {
			this.size = size;
			return this;
		}

		/**
		 * 设置验证码可选字符
		 * 
		 * @param chars
		 * @return
		 */
		public Captcha setChars(char[] chars) {
			this.chars = chars;
			return this;
		}

		/**
		 * 设置验证码可选字符
		 * 
		 * @param chars
		 * @return
		 */
		public Captcha setChars(String chars) {
			return setChars(chars.toCharArray());
		}

		/**
		 * 设置干扰线数量
		 * 
		 * @param interferenceCount
		 * @return
		 */
		public Captcha setInterferenceCount(int interferenceCount) {
			this.interferenceCount = interferenceCount;
			return this;
		}

		/**
		 * 设置干扰线长度
		 * 
		 * @param interferenceLength
		 * @return
		 */
		public Captcha setInterferenceLength(int interferenceLength) {
			this.interferenceLength = interferenceLength;
			return this;
		}

		Captcha() {
		}

		/**
		 * 生成图片验证码
		 * 
		 * @param os
		 *            输出流
		 * @return 验证码
		 * @throws IOException
		 */
		public String generate(OutputStream os) {
			try {
				int width = this.width;
				int height = this.height;
				int fontHeight = this.fontHeight;
				int size = this.size;
				int interferenceCount = this.interferenceCount;
				int interferenceLength = this.interferenceLength;

				if (width == 0) {
					width = 200;
				}
				if (height == 0) {
					height = 50;
				}
				if (fontHeight == 0) {
					fontHeight = (int) (height * 0.8);
				}
				if (size == 0) {
					size = 4;
				}
				if (interferenceCount == 0) {
					interferenceCount = size * 20;
				}
				if (interferenceLength == 0) {
					interferenceLength = fontHeight / 2;
				}

				// 定义图像buffer
				BufferedImage bufferedImage = new BufferedImage(width, height,
						BufferedImage.TYPE_INT_RGB);
				Graphics2D graphics2d = bufferedImage.createGraphics();

				// 创建一个随机数生成器类
				Random random = new Random();
				// 将图像填充为白色
				graphics2d.setColor(Color.WHITE);
				graphics2d.fillRect(0, 0, width, height);
				// 创建字体，字体的大小应该根据图片的高度来定。
				Font font = new Font("Fixedsys", Font.BOLD, fontHeight);
				// 设置字体。
				graphics2d.setFont(font);
				// 画边框。
				graphics2d.setColor(Color.BLACK);
				graphics2d.drawRect(0, 0, width - 1, height - 1);

				// 随机产生4条干扰线，使图象中的认证码不易被其它程序探测到。
				graphics2d.setColor(Color.BLACK);
				for (int i = 0; i < interferenceCount; i++) {
					int x = random.nextInt(width);
					int y = random.nextInt(height);
					int xl = random.nextInt(interferenceLength);
					int yl = random.nextInt(interferenceLength);
					graphics2d.drawLine(x, y, x + xl, y + yl);
				}

				// randomCode用于保存随机产生的验证码，以便用户登录后进行验证。
				StringBuilder codeBuilder = new StringBuilder();

				// 随机产生codeCount数字的验证码。
				for (int i = 0; i < size; i++) {
					// 得到随机产生的验证码数字。
					String c = String.valueOf(chars[random
							.nextInt(chars.length)]);
					// 产生随机的颜色分量来构造颜色值，这样输出的每位数字的颜色值都将不同。
					int red = random.nextInt(200);
					int green = random.nextInt(200);
					int blue = random.nextInt(200);
					// 用随机产生的颜色将验证码绘制到图像中。
					graphics2d.setColor(new Color(red, green, blue));
					int x = (i + 1) * width / (size + 2)
							+ random.nextInt(width / 2 / (size + 2));
					int y = height * 8 / 10 + random.nextInt(height * 2 / 10);
					graphics2d.drawString(c, x, y);
					// 将产生的四个随机数组合在一起。
					codeBuilder.append(c);
				}
				ImageIO.write(bufferedImage, "jpeg", os);
				return codeBuilder.toString();
			} catch (Exception e) {
				throw Lang.unchecked(e);
			}

		}
	}

	/**
	 * 创建一个图片验证码
	 * 
	 * @return
	 */
	public static Captcha create() {
		return new Captcha();
	}

	public static void main(String[] args) throws IOException {
		FileOutputStream os = new FileOutputStream(
				"/Users/lujijiang/Desktop/1.jpg");
		String code = create().setChars("1234567890").setWidth(200)
				.setHeight(50).setSize(6).generate(os);
		os.close();
		System.out.println(code);
	}
}
