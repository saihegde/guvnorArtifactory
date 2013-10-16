package gov.utah.dts.erep.guvnorartifactory.utils;

public interface EncryptionUtils {

    String encrypt(String plainText);
    
    String decrypt(String cipheredText);

}
