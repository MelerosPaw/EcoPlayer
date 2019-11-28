package com.example.playercrop;

public enum WidgetTextStyle {
  NORMAL,
  BOLD,
  ITALIC;

  public static WidgetTextStyle getStyleFromValue(String value) {
    switch (value) {
      case "bold":
        return BOLD;
      case "italic":
        return ITALIC;
      default:
        return NORMAL;
    }
  }
}
