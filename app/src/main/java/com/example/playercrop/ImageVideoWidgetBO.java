package com.example.playercrop;

import androidx.annotation.Nullable;

import java.util.List;

public class ImageVideoWidgetBO implements IWidgetBO {

  private WidgetType type;

  private ImageWBO image;

  private VideoWBO video;

  private List<TextWBO> texts;

  private List<RegionWBO> regions;

  private ILinkBO link;

  private ILinkBO externalLink;

  private int position = -1;

  private boolean matchScreen = false;

  @Override
  public WidgetType getType() {
    return type;
  }

  public void setType(WidgetType type) {
    this.type = type;
  }

  public ImageWBO getImage() {
    return image;
  }

  public void setImage(ImageWBO image) {
    this.image = image;
  }

  public List<TextWBO> getTexts() {
    return texts;
  }

  public void setTexts(List<TextWBO> texts) {
    this.texts = texts;
  }

  public List<RegionWBO> getRegions() {
    return regions;
  }

  public void setRegions(List<RegionWBO> regions) {
    this.regions = regions;
  }

  public ILinkBO getLink() {
    return link;
  }

  public void setLink(ILinkBO link) {
    this.link = link;
  }

  public ILinkBO getExternalLink() {
    return externalLink;
  }

  public void setExternalLink(ILinkBO externalLink) {
    this.externalLink = externalLink;
  }

  public void setPosition(int position) {
    this.position = position;
  }

  public int getPosition() {
    return position;
  }

  @Nullable
  public VideoWBO getVideo() {
    return video;
  }

  public void setVideo(@Nullable VideoWBO video) {
    this.video = video;
  }

  public boolean isMatchScreen() {
    return matchScreen;
  }

  public void setMatchScreen(boolean matchScreen) {
    this.matchScreen = matchScreen;
  }
}
