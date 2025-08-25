package com.example.hoccode.util.constant;

public final class NotificationConstants {
    private NotificationConstants() {
    }
    public static final String URL_VERIFICATION_CUSTOMER = "http://localhost:8130/api/users/register/verify?code=";
    public final static class SEND_MAIL_SUBJECT {
        public final static String USER_REGISTER = "ĐƯỜNG DẪN XÁC NHẬN THÔNG TIN NGƯỜI DÙNG ĐĂNG KÝ";
        public final static String USER_FORGET_PASSWORD = "MÃ XÁC NHẬN LẤY LẠI TÀI KHOẢN NGƯỜI DÙNG";
        public static final String REQUEST_JOIN_FAMILY_TREE = "Yêu cầu tham gia sơ đồ {0}";
    }
    public final static class TEMPLATE_FILE_NAME {
        public final static String USER_FORGET_PASSWORD = "user_forget_password_email";
        public final static String VERIFY_USER = "verify_user_email";
        public static final String REQUEST_JOIN_FAMILY_TREE = "request_join_family";
    }
}