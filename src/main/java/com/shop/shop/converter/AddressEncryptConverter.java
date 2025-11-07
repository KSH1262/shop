package com.shop.shop.converter;

import com.shop.shop.utill.AesEncryptor;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Converter
public class AddressEncryptConverter implements AttributeConverter<String, String> {

    private static AesEncryptor staticEncryptor;

    @Autowired
    public void setEncryptor(AesEncryptor encryptor) {
        AddressEncryptConverter.staticEncryptor = encryptor;
    }

    @Override
    public String convertToDatabaseColumn(String attribute) {
        return (attribute == null || attribute.isEmpty()) ? attribute : staticEncryptor.encrypt(attribute);
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        return (dbData == null || dbData.isEmpty()) ? dbData : staticEncryptor.decrypt(dbData);
    }
}
