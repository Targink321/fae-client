package cc.unknown.ui.auth;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class NameValuePair {
    private final String name;
    private final String value;

    @Override
    public String toString() {
        return name + "=" + value;
    }
}