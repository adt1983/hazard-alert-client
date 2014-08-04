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
 * Model definition for AlertFilter.
 *
 * <p> This is the Java data model class that specifies how to parse/serialize into the JSON that is
 * transmitted over HTTP when working with the alertendpoint. For a detailed explanation see:
 * <a href="http://code.google.com/p/google-http-java-client/wiki/JSON">http://code.google.com/p/google-http-java-client/wiki/JSON</a>
 * </p>
 *
 * @author Google, Inc.
 */
@SuppressWarnings("javadoc")
public final class AlertFilter extends com.google.api.client.json.GenericJson {

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key @com.google.api.client.json.JsonString
  private java.util.List<java.lang.Long> certainty;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private Bounds exclude;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private Bounds include;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.util.List<java.lang.String> languages;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key @com.google.api.client.json.JsonString
  private java.lang.Long limit;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key @com.google.api.client.json.JsonString
  private java.lang.Long minEffective;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key @com.google.api.client.json.JsonString
  private java.lang.Long minExpires;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key @com.google.api.client.json.JsonString
  private java.util.List<java.lang.Long> senders;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key @com.google.api.client.json.JsonString
  private java.util.List<java.lang.Long> severity;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key @com.google.api.client.json.JsonString
  private java.util.List<java.lang.Long> status;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key @com.google.api.client.json.JsonString
  private java.util.List<java.lang.Long> urgency;

  /**
   * @return value or {@code null} for none
   */
  public java.util.List<java.lang.Long> getCertainty() {
    return certainty;
  }

  /**
   * @param certainty certainty or {@code null} for none
   */
  public AlertFilter setCertainty(java.util.List<java.lang.Long> certainty) {
    this.certainty = certainty;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public Bounds getExclude() {
    return exclude;
  }

  /**
   * @param exclude exclude or {@code null} for none
   */
  public AlertFilter setExclude(Bounds exclude) {
    this.exclude = exclude;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public Bounds getInclude() {
    return include;
  }

  /**
   * @param include include or {@code null} for none
   */
  public AlertFilter setInclude(Bounds include) {
    this.include = include;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.util.List<java.lang.String> getLanguages() {
    return languages;
  }

  /**
   * @param languages languages or {@code null} for none
   */
  public AlertFilter setLanguages(java.util.List<java.lang.String> languages) {
    this.languages = languages;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.lang.Long getLimit() {
    return limit;
  }

  /**
   * @param limit limit or {@code null} for none
   */
  public AlertFilter setLimit(java.lang.Long limit) {
    this.limit = limit;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.lang.Long getMinEffective() {
    return minEffective;
  }

  /**
   * @param minEffective minEffective or {@code null} for none
   */
  public AlertFilter setMinEffective(java.lang.Long minEffective) {
    this.minEffective = minEffective;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.lang.Long getMinExpires() {
    return minExpires;
  }

  /**
   * @param minExpires minExpires or {@code null} for none
   */
  public AlertFilter setMinExpires(java.lang.Long minExpires) {
    this.minExpires = minExpires;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.util.List<java.lang.Long> getSenders() {
    return senders;
  }

  /**
   * @param senders senders or {@code null} for none
   */
  public AlertFilter setSenders(java.util.List<java.lang.Long> senders) {
    this.senders = senders;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.util.List<java.lang.Long> getSeverity() {
    return severity;
  }

  /**
   * @param severity severity or {@code null} for none
   */
  public AlertFilter setSeverity(java.util.List<java.lang.Long> severity) {
    this.severity = severity;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.util.List<java.lang.Long> getStatus() {
    return status;
  }

  /**
   * @param status status or {@code null} for none
   */
  public AlertFilter setStatus(java.util.List<java.lang.Long> status) {
    this.status = status;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.util.List<java.lang.Long> getUrgency() {
    return urgency;
  }

  /**
   * @param urgency urgency or {@code null} for none
   */
  public AlertFilter setUrgency(java.util.List<java.lang.Long> urgency) {
    this.urgency = urgency;
    return this;
  }

  @Override
  public AlertFilter set(String fieldName, Object value) {
    return (AlertFilter) super.set(fieldName, value);
  }

  @Override
  public AlertFilter clone() {
    return (AlertFilter) super.clone();
  }

}
