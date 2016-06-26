package com.saasxx.framework.testcase.lang;

import java.io.IOException;

import org.apache.commons.net.whois.WhoisClient;

public class WhoisClientTest {

	public static void main(String[] args) {
		final String name = "team";
		final String suffix = ".com";
		final WhoisClient whoisClient = new WhoisClient();
		for (char c = 'a'; c <= 'z'; c++) {
			final char c1 = c;
			new Thread() {
				public void run() {
					for (char c = 'a'; c <= 'z'; c++) {
						StringBuilder host = new StringBuilder();
						host.append(c1);
						host.append(c);
						host.append(name);
						host.append(suffix);
						try {
							whoisClient.connect(host.toString());
							System.out.println(host);
						} catch (IOException e) {
						}
					}
				}
			}.start();
		}
	}

}
