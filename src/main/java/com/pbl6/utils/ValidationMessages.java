package com.pbl6.utils;

/**
 * Constants cho validation error messages
 * Tất cả messages đều bằng tiếng Việt và mô tả cụ thể
 */
public class ValidationMessages {
    
    // Common messages
    public static final String FIELD_REQUIRED = "Trường này không được để trống";
    public static final String FIELD_INVALID = "Giá trị không hợp lệ";
    
    // User related
    public static final String NAME_REQUIRED = "Tên không được để trống";
    public static final String NAME_LENGTH = "Tên phải có từ 2-100 ký tự";
    public static final String EMAIL_REQUIRED = "Email không được để trống";
    public static final String EMAIL_INVALID = "Định dạng email không hợp lệ";
    public static final String PHONE_REQUIRED = "Số điện thoại không được để trống";
    public static final String PHONE_INVALID = "Số điện thoại phải có 10 chữ số và bắt đầu bằng 0, 3, 5, 7, 8, 9";
    public static final String PASSWORD_REQUIRED = "Mật khẩu không được để trống";
    public static final String PASSWORD_TOO_SHORT = "Mật khẩu phải có ít nhất 6 ký tự";
    public static final String PASSWORD_TOO_LONG = "Mật khẩu không được vượt quá 50 ký tự";
    public static final String GENDER_INVALID = "Giới tính phải là 'Nam' hoặc 'Nữ'";
    public static final String BIRTH_REQUIRED = "Ngày sinh không được để trống";
    
    // Product related
    public static final String PRODUCT_NAME_REQUIRED = "Tên sản phẩm không được để trống";
    public static final String PRODUCT_NAME_LENGTH = "Tên sản phẩm phải có từ 1-255 ký tự";
    public static final String PRODUCT_DESCRIPTION_LENGTH = "Mô tả sản phẩm không được vượt quá 2000 ký tự";
    public static final String PRODUCT_SLUG_REQUIRED = "Slug không được để trống";
    public static final String PRODUCT_SLUG_LENGTH = "Slug phải có từ 1-255 ký tự";
    public static final String PRODUCT_CATEGORY_REQUIRED = "Danh mục không được để trống";
    public static final String PRODUCT_VARIANT_REQUIRED = "Biến thể sản phẩm không được để trống";
    public static final String PRODUCT_PRICE_REQUIRED = "Giá sản phẩm không được để trống";
    public static final String PRODUCT_PRICE_POSITIVE = "Giá sản phẩm phải lớn hơn 0";
    public static final String PRODUCT_SKU_REQUIRED = "Mã SKU không được để trống";
    public static final String PRODUCT_SKU_LENGTH = "Mã SKU phải có từ 1-100 ký tự";
    public static final String PRODUCT_ATTRIBUTE_REQUIRED = "Thuộc tính biến thể không được để trống";
    public static final String PRODUCT_ATTRIBUTE_CODE_REQUIRED = "Mã thuộc tính không được để trống";
    public static final String PRODUCT_ATTRIBUTE_VALUE_REQUIRED = "Giá trị thuộc tính không được để trống";
    
    // Address related
    public static final String ADDRESS_LINE_REQUIRED = "Địa chỉ cụ thể không được để trống";
    public static final String ADDRESS_WARD_REQUIRED = "Phường/Xã không được để trống";
    public static final String ADDRESS_DISTRICT_REQUIRED = "Quận/Huyện không được để trống";
    public static final String ADDRESS_PROVINCE_REQUIRED = "Tỉnh/Thành phố không được để trống";
    
    // Order related
    public static final String ORDER_USER_ID_REQUIRED = "ID người dùng không được để trống";
    public static final String ORDER_STORE_ID_REQUIRED = "ID cửa hàng không được để trống";
    public static final String ORDER_FULLNAME_REQUIRED = "Họ tên không được để trống";
    public static final String ORDER_FULLNAME_LENGTH = "Họ tên phải có từ 2-100 ký tự";
    public static final String ORDER_ITEMS_REQUIRED = "Danh sách sản phẩm không được để trống";
    public static final String ORDER_PAYMENT_METHOD_REQUIRED = "Phương thức thanh toán không được để trống";
    
    // Cart related
    public static final String CART_VARIANT_ID_REQUIRED = "ID biến thể sản phẩm không được để trống";
    public static final String CART_QUANTITY_REQUIRED = "Số lượng không được để trống";
    public static final String CART_QUANTITY_POSITIVE = "Số lượng phải lớn hơn hoặc bằng 0";
    
    // Review related
    public static final String REVIEW_RATING_REQUIRED = "Đánh giá sao không được để trống";
    public static final String REVIEW_RATING_RANGE = "Đánh giá sao phải từ 1-5";
    public static final String REVIEW_CONTENT_REQUIRED = "Nội dung đánh giá không được để trống";
    public static final String REVIEW_CONTENT_LENGTH = "Nội dung đánh giá phải có từ 1-1000 ký tự";
    public static final String REVIEW_PRODUCT_ID_REQUIRED = "ID sản phẩm không được để trống";
    
    // Category related
    public static final String CATEGORY_NAME_REQUIRED = "Tên danh mục không được để trống";
    public static final String CATEGORY_SLUG_REQUIRED = "Slug danh mục không được để trống";
    public static final String CATEGORY_TYPE_REQUIRED = "Loại danh mục không được để trống";
    public static final String CATEGORY_ACTIVE_REQUIRED = "Trạng thái danh mục không được để trống";
    
    // Pagination related
    public static final String PAGE_MIN = "Số trang phải lớn hơn hoặc bằng 1";
    public static final String SIZE_MIN = "Kích thước trang phải lớn hơn hoặc bằng 1";
    public static final String SIZE_MAX = "Kích thước trang không được vượt quá 100";
    public static final String PRICE_MIN = "Giá phải là số không âm";
    
    // Filter related
    public static final String ORDER_FIELD_INVALID = "Trường sắp xếp không hợp lệ. Chỉ chấp nhận: id, price, create_at, rating, sold";
    public static final String ORDER_DIRECTION_INVALID = "Hướng sắp xếp không hợp lệ. Chỉ chấp nhận: asc, desc";
    
    // Media related
    public static final String MEDIA_FILE_REQUIRED = "File media không được để trống";
    public static final String MEDIA_TYPE_REQUIRED = "Loại media không được để trống";
    public static final String MEDIA_TYPE_INVALID = "Loại media phải là 'image' hoặc 'video'";
    public static final String MEDIA_ORDER_POSITIVE = "Thứ tự sắp xếp phải là số không âm";
    
    // Token related
    public static final String TOKEN_REQUIRED = "Token không được để trống";
    
    // Store related
    public static final String STORE_ID_REQUIRED = "ID cửa hàng không được để trống";
}
