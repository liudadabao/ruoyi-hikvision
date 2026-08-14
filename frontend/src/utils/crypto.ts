import * as CryptoJSModule from 'crypto-js';

const CryptoJS = ('default' in CryptoJSModule ? CryptoJSModule.default : CryptoJSModule) as typeof CryptoJSModule;

/**
 * 随机生成32位的字符串
 * @returns {string}
 */
const generateRandomString = (): string => {
  const array = new Uint8Array(32);
  crypto.getRandomValues(array);
  return Array.from(array, b => b.toString(16).padStart(2, '0'))
    .join('')
    .slice(0, 32);
};

/**
 * 随机生成aes 密钥
 * @returns {string}
 */
export const generateAesKey = (): CryptoJSModule.lib.WordArray => {
  return CryptoJS.enc.Utf8.parse(generateRandomString());
};

/**
 * 加密base64
 * @returns {string}
 */
export const encryptBase64 = (str: CryptoJSModule.lib.WordArray): string => {
  return CryptoJS.enc.Base64.stringify(str);
};

/**
 * 解密base64
 */
export const decryptBase64 = (str: string) => {
  return CryptoJS.enc.Base64.parse(str);
};

/**
 * 使用密钥对数据进行加密
 * @param message
 * @param aesKey
 * @returns {string}
 */
export const encryptWithAes = (message: string, aesKey: CryptoJSModule.lib.WordArray): string => {
  const encrypted = CryptoJS.AES.encrypt(message, aesKey, {
    mode: CryptoJS.mode.ECB,
    padding: CryptoJS.pad.Pkcs7
  });
  return encrypted.toString();
};

/**
 * 使用密钥对数据进行解密
 * @param message
 * @param aesKey
 * @returns {string}
 */
export const decryptWithAes = (message: string, aesKey: CryptoJSModule.lib.WordArray): string => {
  const decrypted = CryptoJS.AES.decrypt(message, aesKey, {
    mode: CryptoJS.mode.ECB,
    padding: CryptoJS.pad.Pkcs7
  });
  return decrypted.toString(CryptoJS.enc.Utf8);
};
