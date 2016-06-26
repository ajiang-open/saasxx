package com.saasxx.framework.io;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.apache.commons.io.FilenameUtils;
import org.imgscalr.Scalr;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.MetadataException;
import com.drew.metadata.exif.ExifIFD0Directory;

/**
 * 图片处理工具类
 * 
 * @author lujijiang
 *
 */
public class Images {
	/**
	 * 智能剪裁，在最符合剪裁尺寸的基础上确保图片不变形
	 * 
	 * @param image
	 *            原始图片对象
	 * @param width
	 *            剪裁宽度
	 * @param height
	 *            剪裁高度
	 * @param quality
	 *            图片质量
	 * @return 剪裁后的图片
	 */
	public static BufferedImage smartCut(BufferedImage image, int width, int height, float quality) {
		if (width > 0 || height > 0) {
			if (width <= 0) {
				width = image.getWidth() * height / image.getHeight();
			} else if (height <= 0) {
				height = image.getHeight() * width / image.getWidth();
			}
		} else {
			width = image.getWidth();
			height = image.getHeight();
		}
		Scalr.Method method = Scalr.Method.AUTOMATIC;
		if (quality > 0) {
			if (quality <= 0.5) {
				method = Scalr.Method.SPEED;
			} else if (quality <= 0.75) {
				method = Scalr.Method.BALANCED;
			} else if (quality <= 0.9) {
				method = Scalr.Method.QUALITY;
			} else {
				method = Scalr.Method.ULTRA_QUALITY;
			}
		}
		BufferedImage newImage;
		if ((width - image.getWidth()) * 1.0 / image.getWidth() > (height - image.getHeight()) * 1.0
				/ image.getHeight()) {
			Scalr.Mode mode = Scalr.Mode.FIT_TO_WIDTH;
			image = Scalr.resize(image, method, mode, width, height);
			if (image.getHeight() > height) {
				newImage = Scalr.crop(image, 0, (image.getHeight() - height) / 2, width, height);
			} else {
				newImage = image;
			}
		} else {
			Scalr.Mode mode = Scalr.Mode.FIT_TO_HEIGHT;
			image = Scalr.resize(image, method, mode, width, height);
			if (image.getWidth() > width) {
				newImage = Scalr.crop(image, (image.getWidth() - width) / 2, 0, width, height);
			} else {
				newImage = image;
			}
		}
		return newImage;
	}

	/**
	 * 自动翻转图片
	 * 
	 * @param source
	 *            源图片文件
	 * @param target
	 *            目标图片文件
	 * @throws ImageProcessingException
	 * @throws IOException
	 * @throws MetadataException
	 */
	public static void autoRotate(File source, File target)
			throws ImageProcessingException, IOException, MetadataException {
		Metadata metadata = ImageMetadataReader.readMetadata(source);
		Directory directory = metadata.getFirstDirectoryOfType(ExifIFD0Directory.class);
		if (directory != null) {
			int orientation = directory.getInt(ExifIFD0Directory.TAG_ORIENTATION);
			BufferedImage image = ImageIO.read(source);
			switch (orientation) {
			case 3:
				image = Scalr.rotate(image, Scalr.Rotation.CW_180, Scalr.OP_ANTIALIAS);
				break;
			case 6:
				image = Scalr.rotate(image, Scalr.Rotation.CW_90, Scalr.OP_ANTIALIAS);
				break;
			case 8:
				image = Scalr.rotate(image, Scalr.Rotation.CW_270, Scalr.OP_ANTIALIAS);
				break;
			default:
				break;
			}
			ImageIO.write(image, FilenameUtils.getExtension(source.getName()), target);
		} else {
			throw new IOException("Image does not contain information of ExifIFD0");
		}
	}
}
