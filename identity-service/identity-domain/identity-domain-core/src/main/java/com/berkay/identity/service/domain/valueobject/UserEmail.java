package com.berkay.identity.service.domain.valueobject;

import com.berkay.identity.service.domain.exception.IdentityDomainException;
import java.util.Objects;
import java.util.regex.Pattern;

public class UserEmail {
    // OWASP Validation Regex standartlarına yakın, katı bir email kontrolü
    // [\\w!#$%&'*+/=?{|}~^-]+`: Harf, rakam ve izin verilen özel karakterlerle başlamalı.
    // (?:\\.[...]+)*: Nokta (.) kontrolü. Nokta kullanılabilir ama nokta ile başlayamaz, nokta ile bitemez ve iki nokta yan yana gelemez
    // @: Mutlaka bir @ işareti olmalı.
    // (?:[a-zA-Z0-9-]+\\.)+: Domain isimleri ve alt domainler (subdomains). Harf, rakam ve tire (-) içerebilir. Sonunda mutlaka nokta olmalı (Regex mantığı gereği bir sonraki grupla birleşir). Örn: gmail., mail.google.
    // [a-zA-Z]{2,6}$: TLD (Top Level Domain) kısıtlaması. .com, .net, .org, .tr gibi uzantıların 2 ile 6 karakter arasında olmasını zorunlu kılar.

    private static final String EMAIL_REGEX = "^[\\w!#$%&'*+/=?`{|}~^-]+(?:\\.[\\w!#$%&'*+/=?`{|}~^-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,6}$";
    private static final Pattern PATTERN = Pattern.compile(EMAIL_REGEX);

    private final String value;

    public UserEmail(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IdentityDomainException("Email cannot be empty!");
        }
        if (!PATTERN.matcher(value).matches()) {
            throw new IdentityDomainException("Invalid email format! Email: " + value);
        }
        this.value = value;
    }

    public String getValue() { return value; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserEmail userEmail = (UserEmail) o;
        return Objects.equals(value, userEmail.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}