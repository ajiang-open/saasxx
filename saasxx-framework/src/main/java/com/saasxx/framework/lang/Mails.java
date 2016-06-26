package com.saasxx.framework.lang;

import java.io.File;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.Address;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;

import org.springframework.mail.javamail.JavaMailSender;

import com.saasxx.framework.Lang;

/**
 * 邮件对象生成工具类
 * 
 * @author lujijiang
 *
 */
public class Mails {
	/**
	 * 邮件对象类
	 * 
	 * @author lujijiang
	 *
	 */
	public static class Mail {
		/**
		 * 用于匹配模板内容中的ID双括号
		 */
		private static final Pattern ID_PATTERN = Pattern
				.compile("\\{\\{.+?\\}\\}");

		Mail() {
		}

		/**
		 * 发信地址列表
		 */
		Set<String> fromSet = Lang.newSet();
		/**
		 * 收信地址列表
		 */
		Set<String> toSet = Lang.newSet();
		/**
		 * 抄送地址列表
		 */
		Set<String> ccSet = Lang.newSet();
		/**
		 * 匿名抄送地址列表
		 */
		Set<String> bccSet = Lang.newSet();
		/**
		 * 标题
		 */
		String subject;
		/**
		 * 正文
		 */
		String body;

		/**
		 * 附件列表
		 */
		Map<String, byte[]> attachmentMap = Lang.newMap();

		/**
		 * 添加发件人地址
		 * 
		 * @param addresses
		 * @return
		 */
		public Mail addFroms(String... addresses) {
			for (String address : addresses) {
				address = address.trim();
				if (!Lang.isEmpty(address)) {
					fromSet.add(address);
				}
			}
			return this;
		}

		/**
		 * 设置发件人地址
		 * 
		 * @param addresses
		 * @return
		 */
		public Mail setFroms(String... addresses) {
			fromSet.clear();
			addFroms(addresses);
			return this;
		}

		/**
		 * 添加收件人地址
		 * 
		 * @param addresses
		 * @return
		 */
		public Mail addTos(String... addresses) {
			for (String address : addresses) {
				address = address.trim();
				if (!Lang.isEmpty(address)) {
					toSet.add(address);
				}
			}
			return this;
		}

		/**
		 * 设置收件人地址
		 * 
		 * @param addresses
		 * @return
		 */
		public Mail setTos(String... addresses) {
			toSet.clear();
			addTos(addresses);
			return this;
		}

		/**
		 * 添加抄送人地址
		 * 
		 * @param addresses
		 * @return
		 */
		public Mail addCCs(String... addresses) {
			for (String address : addresses) {
				address = address.trim();
				if (!Lang.isEmpty(address)) {
					ccSet.add(address);
				}
			}
			return this;
		}

		/**
		 * 设置抄送人地址
		 * 
		 * @param addresses
		 * @return
		 */
		public Mail setCCs(String... addresses) {
			ccSet.clear();
			addCCs(addresses);
			return this;
		}

		/**
		 * 添加匿名抄送人地址
		 * 
		 * @param addresses
		 * @return
		 */
		public Mail addBCCs(String... addresses) {
			for (String address : addresses) {
				address = address.trim();
				if (!Lang.isEmpty(address)) {
					bccSet.add(address);
				}
			}
			return this;
		}

		/**
		 * 设置匿名抄送人地址
		 * 
		 * @param addresses
		 * @return
		 */
		public Mail setBCCs(String... addresses) {
			bccSet.clear();
			addBCCs(addresses);
			return this;
		}

		public Mail setSubject(String subject) {
			this.subject = subject;
			return this;
		}

		public Mail setBody(String body) {
			this.body = body;
			return this;
		}

		/**
		 * 添加附件内容
		 * 
		 * @param name
		 * @param data
		 * @return
		 */
		public Mail addAttachment(String name, byte[] data) {
			name = name.trim();
			attachmentMap.put(name, data);
			return this;
		}

		/**
		 * 删除附件内容
		 * 
		 * @param name
		 * @param data
		 * @return
		 */
		public Mail remoteAttachment(String name) {
			name = name.trim();
			attachmentMap.remove(name);
			return this;
		}

		/**
		 * 删除所有附件内容
		 * 
		 * @param name
		 * @param data
		 * @return
		 */
		public Mail remoteAttachments() {
			attachmentMap.clear();
			return this;
		}

		/**
		 * 获取附件ID
		 * 
		 * @param name
		 * @return
		 */
		public String getAttachmentId(String name) {
			name = name.trim();
			if (attachmentMap.containsKey(name)) {
				return hash(name);
			}
			throw new IllegalStateException(String.format(
					"The attachment which named %s is not exist", name));
		}

		/**
		 * 将名字hash化
		 * 
		 * @param name
		 * @return
		 */
		private String hash(String name) {
			return String.valueOf(10000000000L + name.hashCode());
		}

		/**
		 * 发送邮件
		 * 
		 * @param javaMailSender
		 * @return
		 * @throws MessagingException
		 */
		public Mail send(JavaMailSender javaMailSender, boolean html)
				throws MessagingException {
			MimeMessage mimeMessage = javaMailSender.createMimeMessage();
			mimeMessage.setSubject(subject);
			for (String address : fromSet) {
				mimeMessage
						.addFrom(new Address[] { new InternetAddress(address) });
			}
			for (String address : toSet) {
				mimeMessage.setRecipients(RecipientType.TO,
						new Address[] { new InternetAddress(address) });
			}
			for (String address : ccSet) {
				mimeMessage.setRecipients(RecipientType.CC,
						new Address[] { new InternetAddress(address) });
			}
			for (String address : bccSet) {
				mimeMessage.setRecipients(RecipientType.BCC,
						new Address[] { new InternetAddress(address) });
			}
			MimeMultipart mimeMultipart = new MimeMultipart("related");
			mimeMessage.setContent(mimeMultipart);
			// 处理正文
			MimeBodyPart bodyPart = new MimeBodyPart();
			if (html) {
				StringBuffer bodyBuffer = new StringBuffer();
				Matcher attachmentIdMatcher = ID_PATTERN.matcher(body);
				while (attachmentIdMatcher.find()) {
					String group = attachmentIdMatcher.group();
					group = group.replaceAll("^\\{\\{|\\}\\}$", "").trim();
					attachmentIdMatcher.appendReplacement(bodyBuffer,
							hash(group));
				}
				attachmentIdMatcher.appendTail(bodyBuffer);
				bodyPart.setContent(bodyBuffer.toString(),
						"text/html;charset=utf-8");
			} else {
				bodyPart.setContent(body, "text/plain;charset=utf-8");
			}
			mimeMultipart.addBodyPart(bodyPart);
			// 处理附件
			for (String name : attachmentMap.keySet()) {
				MimeBodyPart attachmentPart = new MimeBodyPart();
				attachmentPart.setFileName(new File(name).getName());
				attachmentPart.setHeader("Content-ID", "<" + hash(name) + ">");
				DataSource dataSource = new ByteArrayDataSource(
						attachmentMap.get(name), "application/octet-stream");
				DataHandler dataHandler = new DataHandler(dataSource);
				attachmentPart.setDataHandler(dataHandler);
				mimeMultipart.addBodyPart(attachmentPart);
			}

			// 发送邮件
			mimeMessage.saveChanges();
			javaMailSender.send(mimeMessage);
			return this;
		}
	}

	public static Mail create() {
		return new Mail();
	}
}
