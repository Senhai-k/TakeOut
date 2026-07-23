package com.takeout.dto.app;

public class AuthLoginResponse {

    private final Long userId;
    private final String nickname;
    private final String phone;
    private final String avatarText;
    private final String memberLevel;
    private final String token;
    private final String refreshToken;

    public AuthLoginResponse(Long userId, String nickname, String phone, String avatarText, String memberLevel, String token, String refreshToken) {
        this.userId = userId;
        this.nickname = nickname;
        this.phone = phone;
        this.avatarText = avatarText;
        this.memberLevel = memberLevel;
        this.token = token;
        this.refreshToken = refreshToken;
    }

    public Long getUserId() {
        return userId;
    }

    public String getNickname() {
        return nickname;
    }

    public String getPhone() {
        return phone;
    }

    public String getAvatarText() {
        return avatarText;
    }

    public String getMemberLevel() {
        return memberLevel;
    }

    public String getToken() {
        return token;
    }

    public String getRefreshToken(){return refreshToken;}
}
