/*
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
/*
 * This code was generated by https://code.google.com/p/google-apis-client-generator/
 * (build: 2014-04-15 19:10:39 UTC)
 * on 2014-05-20 at 12:44:33 UTC 
 * Modify at your own risk.
 */

package com.appspot.hazard_alert.alertendpoint.model;

/**
 * Model definition for Bounds.
 *
 * <p> This is the Java data model class that specifies how to parse/serialize into the JSON that is
 * transmitted over HTTP when working with the alertendpoint. For a detailed explanation see:
 * <a href="http://code.google.com/p/google-http-java-client/wiki/JSON">http://code.google.com/p/google-http-java-client/wiki/JSON</a>
 * </p>
 *
 * @author Google, Inc.
 */
@SuppressWarnings("javadoc")
public final class Bounds extends com.google.api.client.json.GenericJson {

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key("ne_lat")
  private java.lang.Double neLat;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key("ne_lng")
  private java.lang.Double neLng;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key("sw_lat")
  private java.lang.Double swLat;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key("sw_lng")
  private java.lang.Double swLng;

  /**
   * @return value or {@code null} for none
   */
  public java.lang.Double getNeLat() {
    return neLat;
  }

  /**
   * @param neLat neLat or {@code null} for none
   */
  public Bounds setNeLat(java.lang.Double neLat) {
    this.neLat = neLat;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.lang.Double getNeLng() {
    return neLng;
  }

  /**
   * @param neLng neLng or {@code null} for none
   */
  public Bounds setNeLng(java.lang.Double neLng) {
    this.neLng = neLng;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.lang.Double getSwLat() {
    return swLat;
  }

  /**
   * @param swLat swLat or {@code null} for none
   */
  public Bounds setSwLat(java.lang.Double swLat) {
    this.swLat = swLat;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.lang.Double getSwLng() {
    return swLng;
  }

  /**
   * @param swLng swLng or {@code null} for none
   */
  public Bounds setSwLng(java.lang.Double swLng) {
    this.swLng = swLng;
    return this;
  }

  @Override
  public Bounds set(String fieldName, Object value) {
    return (Bounds) super.set(fieldName, value);
  }

  @Override
  public Bounds clone() {
    return (Bounds) super.clone();
  }

}
