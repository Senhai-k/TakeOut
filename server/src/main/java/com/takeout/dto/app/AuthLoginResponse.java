package com.takeout.dto.app;

public class AuthLoginResponse {

    private final Long userId;
    private final String nickname;
    private final String phone;
    private final String avatarText;
    private final String memberLevel;
    private final String token;

    public AuthLoginResponse(Long userId, String nickname, String phone, String avatarText, String memberLevel, String token) {
        this.userId = userId;
        this.nickname = nickname;
        this.phone = phone;
        this.avatarText = avatarText;
        this.memberLevel = memberLevel;
        this.token = token;
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
}
