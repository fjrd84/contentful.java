package com.contentful.java.cda;

public class CDAArrayError extends CDAResource {
  public static class Details {
    String type;
    String linkType;
    String id;

    public String getType() {
      return type;
    }

    public String getLinkType() {
      return linkType;
    }

    public String getId() {
      return id;
    }
  }

  Details details;

  public Details getDetails() {
    return details;
  }
}
