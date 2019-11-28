package com.example.playercrop;

public enum WidgetType {

  IMAGE(1),
  TEXT(2),
  IMAGE_SLIDER(3),
  PRODUCT_SLIDER(4),
  VIDEO_LINK(5),
  HTML_LINK(6),
  EXTERNAL_LINK(100),
  PRODUCT_LINK(7),
  CATEGORY_LINK(8),
  CATEGORY_TREE(9),
  VIDEO(10),
  FOOTER(99),
  UNKNOWN(-1);

  private Integer code;

  WidgetType(Integer code) {
    this.code = code;
  }

  public static WidgetType getTypeByCode(Integer code) {
    switch (code) {
      case 1:
        return IMAGE;
      case 2:
        return TEXT;
      case 3:
        return IMAGE_SLIDER;
      case 4:
        return PRODUCT_SLIDER;
      case 5:
        return VIDEO_LINK;
      case 6:
        return HTML_LINK;
      case 100:
        return EXTERNAL_LINK;
      case 7:
        return PRODUCT_LINK;
      case 8:
        return CATEGORY_LINK;
      case 9:
        return CATEGORY_TREE;
      case 10:
        return VIDEO;
      case 99:
        return FOOTER;
      default:
        return UNKNOWN;
    }
  }

  public static WidgetType getTypeByString(String type) {
    switch (type) {
      case "image":
        return IMAGE;
      case "text":
        return TEXT;
      case "slider":
        return IMAGE_SLIDER;
      case "productSlider":
        return PRODUCT_SLIDER;
      case "videoLink":
        return VIDEO_LINK;
      case "htmlLink":
        return HTML_LINK;
      case "externalLink":
        return EXTERNAL_LINK;
      case "productLink":
        return PRODUCT_LINK;
      case "categoryLink":
        return CATEGORY_LINK;
      case "categoryTree":
        return CATEGORY_TREE;
      case "video":
        return VIDEO;
      default:
        return UNKNOWN;
    }
  }

  public Integer getCode() {
    return code;
  }
}
