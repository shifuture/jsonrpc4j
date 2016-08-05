package com.googlecode.jsonrpc4j;

import org.apache.log4j.Logger;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import javax.crypto.Cipher;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.*;

/**
 * 描述：RAS加密解密工具类
 */
public class RSAUtils {

	static Logger logger = Logger.getLogger(RSAUtils.class);

	/**
	 * 公钥
	 */
	private static final String DEFAULT_PUBLIC_KEY = "--USE YOUR PUBLIC KEY--";

	/**
	 * 私钥
	 */
	public static final String DEFAULT_PRIVATE_KEY = "--USE YOUR PRIVATE KEY--";

	/**
	 * 加密返回的JSON数据
	 */
	private static final String RESPONSE_PUBLIC_KEY = "--USE YOUR RESPONSE_PUBLIC_KEY--";
	public static final String RESPONSE_PRIVATE_KEY = "--USE YOUR RESPONSE_PRIVATE_KEY--";

	/**
	 * 使用模和指数生成RSA公钥
	 * 注意：【此代码用了默认补位方式，为RSA/None/PKCS1Padding，不同JDK默认的补位方式可能不同，如Android默认是RSA
	 * /None/NoPadding】
	 * 
	 * @param modulus
	 *            模
	 * @param exponent
	 *            指数
	 * @return
	 */
	public static RSAPublicKey getPublicKey(String modulus, String exponent) {
		try {
			BigInteger b1 = new BigInteger(modulus);
			BigInteger b2 = new BigInteger(exponent);
			KeyFactory keyFactory = KeyFactory.getInstance("RSA");
			RSAPublicKeySpec keySpec = new RSAPublicKeySpec(b1, b2);
			return (RSAPublicKey) keyFactory.generatePublic(keySpec);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 使用模和指数生成RSA私钥
	 * 注意：【此代码用了默认补位方式，为RSA/None/PKCS1Padding，不同JDK默认的补位方式可能不同，如Android默认是RSA
	 * /None/NoPadding】
	 * 
	 * @param modulus
	 *            模
	 * @param exponent
	 *            指数
	 * @return
	 */
	public static RSAPrivateKey getPrivateKey(String modulus, String exponent) {
		try {
			BigInteger b1 = new BigInteger(modulus);
			BigInteger b2 = new BigInteger(exponent);
			KeyFactory keyFactory = KeyFactory.getInstance("RSA");
			RSAPrivateKeySpec keySpec = new RSAPrivateKeySpec(b1, b2);
			return (RSAPrivateKey) keyFactory.generatePrivate(keySpec);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 公钥加密
	 * 
	 * @param data
	 * @param publicKey
	 * @return
	 * @throws Exception
	 */
	public static String encryptByPublicKey(String data, RSAPublicKey publicKey) throws Exception {
		Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
		Cipher cipher = Cipher.getInstance("RSA/None/PKCS1PADDING", "BC");
		cipher.init(Cipher.ENCRYPT_MODE, publicKey);
		// 模长
		int key_len = publicKey.getModulus().bitLength() / 8;
		// 加密数据长度 <= 模长-11
		byte[][] arrays = splitArray(data.getBytes("UTF-8"), key_len - 11);// 将明文按117个字节拆分
		StringBuffer sb = new StringBuffer();// 密文
		// 如果明文长度大于模长-11则要分组加密
		BASE64Encoder base64Encoder = new BASE64Encoder();
		for (byte[] s : arrays) {
			sb.append(base64Encoder.encode(cipher.doFinal(s)));// base64 加密
		}
		return sb.toString();
	}

	/**
	 * 私钥解密
	 * 
	 * @param data
	 * @param privateKey
	 * @return
	 * @throws Exception
	 */
	public static String decryptByPrivateKey(String data, RSAPrivateKey privateKey) throws Exception {
		Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
		Cipher cipher = Cipher.getInstance("RSA/None/PKCS1PADDING", "BC");
		cipher.init(Cipher.DECRYPT_MODE, privateKey);
		// 模长
		int key_len = privateKey.getModulus().bitLength() / 8;
	 	BASE64Decoder base64Decoder = new BASE64Decoder();

		byte[] bcd = base64Decoder.decodeBuffer(data);
		// 如果密文长度大于模长则要分组解密
		StringBuffer sb = new StringBuffer();
		byte[][] arrays = splitArray(bcd, key_len);
		for (byte[] arr : arrays) {
			sb.append(new String(cipher.doFinal(arr), "utf-8"));
		}
		return sb.toString();
	}

	/**
	 * 拆分数组
	 */
	private static byte[][] splitArray(byte[] data, int len) {
		int x = data.length / len;
		int y = data.length % len;
		int z = 0;
		if (y != 0) {
			z = 1;
		}
		byte[][] arrays = new byte[x + z][];
		byte[] arr;
		for (int i = 0; i < x + z; i++) {
			arr = new byte[len];
			if (i == x + z - 1 && y != 0) {
//				arr = new byte[y];
				System.arraycopy(data, i * len, arr, 0, y);
			} else {
				
				System.arraycopy(data, i * len, arr, 0, len);
			}
			arrays[i] = arr;
		}
		return arrays;
	}

	/**
	 * 描述：加载公钥
	 * 
	 * @param publicKeyStr
	 * @return
	 * @throws Exception
	 */
	private static RSAPublicKey loadPublicKey(String publicKeyStr) throws Exception {
		try {
			BASE64Decoder base64Decoder = new BASE64Decoder();
			byte[] buffer = base64Decoder.decodeBuffer(publicKeyStr);
			KeyFactory keyFactory = KeyFactory.getInstance("RSA");
			X509EncodedKeySpec keySpec = new X509EncodedKeySpec(buffer);
			return (RSAPublicKey) keyFactory.generatePublic(keySpec);
		} catch (IOException e) {
			throw new Exception("公钥数据内容读取错误");
		} catch (NoSuchAlgorithmException e) {
			throw new Exception("无此算法");
		} catch (InvalidKeySpecException e) {
			throw new Exception("公钥非法");
		} catch (NullPointerException e) {
			throw new Exception("私钥数据为空");
		}
	}

	/**
	 * 描述：加载私钥
	 * 
	 * @param privateKey
	 * @return
	 * @throws Exception
	 */
	private static RSAPrivateKey loadPrivateKey(String privateKey) throws Exception {
		try {
			BASE64Decoder base64Decoder = new BASE64Decoder();
			byte[] buffer = base64Decoder.decodeBuffer(privateKey);
			PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(buffer);
			KeyFactory keyFactory = KeyFactory.getInstance("RSA");
			return (RSAPrivateKey) keyFactory.generatePrivate(keySpec);
		} catch (IOException e) {
			throw new Exception("私钥数据内容读取错误");
		} catch (NoSuchAlgorithmException e) {
			throw new Exception("无此算法");
		} catch (InvalidKeySpecException e) {
			throw new Exception("私钥非法");
		} catch (NullPointerException e) {
			throw new Exception("私钥数据为空");
		}
	}

	/**
	 * 描述：加密
	 * 
	 * @param str
	 * @return
	 */
	public static String RSAEncode(String str) {
		try {
			RSAPublicKey publicKey = loadPublicKey(DEFAULT_PUBLIC_KEY);
			// 模
			String modulus = publicKey.getModulus().toString();
			// 公钥指数
			String public_exponent = publicKey.getPublicExponent().toString();
			RSAPublicKey pubKey = RSAUtils.getPublicKey(modulus, public_exponent);
			BASE64Encoder base64Encoder = new BASE64Encoder();
			str = base64Encoder.encodeBuffer(str.getBytes("UTF-8"));// 先对明文进行base64加密
			// 加密后的密文
			String mi = RSAUtils.encryptByPublicKey(str, pubKey);
			return mi;
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * 描述：解密
	 * 
	 * @param str
	 * @return
	 */
	public static String RSADecode(String str) {
		/**
		 * 为ios请求做特殊处理，ios请求时由(null)=开头并且=后边的内容urlencode。所以先截取并urldecode
		 */
		if (!"".equals(str)) {
			if (str.indexOf("(null)=") != -1) {// ios以(null)=开头
				try {
					str = java.net.URLDecoder.decode(str.substring(7), "utf-8");
					logger.info("IOS请求报文【" + str + "】");
				} catch (UnsupportedEncodingException e) {
					logger.error(e);
					return null;
				}
			} else {
				logger.info("Android请求报文【" + str + "】");
			}
		}
		try {
			RSAPublicKey publicKey = loadPublicKey(DEFAULT_PUBLIC_KEY);
			RSAPrivateKey privateKey = loadPrivateKey(DEFAULT_PRIVATE_KEY);
			String private_exponent = privateKey.getPrivateExponent().toString();
			String modulus = publicKey.getModulus().toString();
			RSAPrivateKey priKey = RSAUtils.getPrivateKey(modulus, private_exponent);
			String ming = RSAUtils.decryptByPrivateKey(str, priKey);
			BASE64Decoder base64Decoder = new BASE64Decoder();
			byte[] b = base64Decoder.decodeBuffer(ming.trim());// rsa解密后再base64解密
			ming = new String(b, "utf-8");
			logger.info("解密后请求报文【" + ming.trim() + "】");
			return ming;
		} catch (Exception e) {
			logger.error(e);
			return null;
		}
	}

	// ------------------------------------------------------
	/**
	 * 描述：加密
	 * 
	 * @param str
	 * @return
	 */
	public static String RSAResponseEncode(String str) {
		try {
			RSAPublicKey publicKey = loadPublicKey(RESPONSE_PUBLIC_KEY);
			// 模
			String modulus = publicKey.getModulus().toString();
			// 公钥指数
			String public_exponent = publicKey.getPublicExponent().toString();
			RSAPublicKey pubKey = RSAUtils.getPublicKey(modulus, public_exponent);
			BASE64Encoder base64Encoder = new BASE64Encoder();
			str = base64Encoder.encodeBuffer(str.getBytes("UTF-8"));// 先对明文进行base64加密
			// 加密后的密文
			String mi = RSAUtils.encryptByPublicKey(str, pubKey);
			return mi;
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * 描述：解密
	 * 
	 * @param str
	 * @return
	 */
	public static String RSAResponseDecode(String str) {
		/**
		 * 为ios请求做特殊处理，ios请求时由(null)=开头并且=后边的内容urlencode。所以先截取并urldecode
		 */
		if (!"".equals(str)) {
			if (str.indexOf("(null)=") != -1) {// ios以(null)=开头
				try {
					str = java.net.URLDecoder.decode(str.substring(7), "utf-8");
					logger.info("IOS请求报文【" + str + "】");
				} catch (UnsupportedEncodingException e) {
					logger.error(e);
					return null;
				}
			} else {
				logger.info("Android请求报文【" + str + "】");
			}
		}
		try {
			RSAPublicKey publicKey = loadPublicKey(RESPONSE_PUBLIC_KEY);
			RSAPrivateKey privateKey = loadPrivateKey(RESPONSE_PRIVATE_KEY);
			String private_exponent = privateKey.getPrivateExponent().toString();
			String modulus = publicKey.getModulus().toString();
			RSAPrivateKey priKey = RSAUtils.getPrivateKey(modulus, private_exponent);
			String ming = RSAUtils.decryptByPrivateKey(str, priKey);
			BASE64Decoder base64Decoder = new BASE64Decoder();
			byte[] b = base64Decoder.decodeBuffer(ming.trim());// rsa解密后再base64解密
			ming = new String(b, "utf-8");
			logger.info("解密后请求报文【" + ming.trim() + "】");
			return ming;
		} catch (Exception e) {
			logger.error(e);
			return null;
		}
	}
}
