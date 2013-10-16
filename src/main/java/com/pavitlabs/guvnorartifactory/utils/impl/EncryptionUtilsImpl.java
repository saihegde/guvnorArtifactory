package gov.utah.dts.erep.guvnorartifactory.utils.impl;

import gov.utah.dts.erep.guvnorartifactory.utils.EncryptionUtils;
import gov.utah.erep.services.crypto.CryptoService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public final class EncryptionUtilsImpl implements EncryptionUtils{

    @Autowired
    private CryptoService cryptoService;

    public final String decrypt(String cipherText) {
        return cryptoService.decrypt(cipherText);
    }

    public final String encrypt(String plainText) {
        return cryptoService.encrypt(plainText);
    }

}
