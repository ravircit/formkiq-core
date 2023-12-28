/**
 * MIT License
 * 
 * Copyright (c) 2018 - 2020 FormKiQ
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.formkiq.aws.dynamodb;

import static com.formkiq.aws.dynamodb.SiteIdKeyGenerator.createDatabaseKey;
import static software.amazon.awssdk.services.dynamodb.model.AttributeValue.fromBool;
import static software.amazon.awssdk.services.dynamodb.model.AttributeValue.fromN;
import static software.amazon.awssdk.services.dynamodb.model.AttributeValue.fromS;
import java.util.HashMap;
import java.util.Map;
import com.formkiq.graalvm.annotations.Reflectable;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

/**
 * Document Access Attributes Record.
 */
@Reflectable
public class DocumentAccessAttributesRecord
    implements DynamodbRecord<DocumentAccessAttributesRecord>, DbKeys {

  /** Access Attribute String Value. */
  @Reflectable
  private Boolean booleanValue;
  /** DocumentId. */
  @Reflectable
  private String documentId;
  /** Access Attribute Key. */
  @Reflectable
  private String key;
  /** Access Attribute Nunber Value. */
  @Reflectable
  private Double numberValue;
  /** Access Attribute String Value. */
  @Reflectable
  private String stringValue;

  /**
   * Set {@link Boolean} value.
   * 
   * @param value {@link Boolean}
   * @return {@link DocumentAccessAttributesRecord}
   */
  public DocumentAccessAttributesRecord booleanValue(final Boolean value) {
    this.booleanValue = value;
    return this;
  }

  /**
   * Set Document Id.
   * 
   * @param id {@link String}
   * @return {@link DocumentAccessAttributesRecord}
   */
  public DocumentAccessAttributesRecord documentId(final String id) {
    this.documentId = id;
    return this;
  }

  @Override
  public Map<String, AttributeValue> getAttributes(final String siteId) {

    String number = this.numberValue != null ? this.numberValue.toString() : null;
    Map<String, AttributeValue> attrs =
        new HashMap<>(Map.of(DbKeys.PK, fromS(pk(siteId)), DbKeys.SK, fromS(sk()), "documentId",
            fromS(this.documentId), "key", fromS(this.key), "stringValue", fromS(this.stringValue),
            "numberValue", fromN(number), "booleanValue", fromBool(this.booleanValue)));

    return attrs;
  }

  /**
   * Get {@link Boolean} value.
   * 
   * @return {@link Boolean}
   */
  public Boolean getBooleanValue() {
    return this.booleanValue;
  }

  /**
   * Get DocumentId.
   * 
   * @return {@link String}
   */
  public String getDocumentId() {
    return this.documentId;
  }

  @Override
  public DocumentAccessAttributesRecord getFromAttributes(final String siteId,
      final Map<String, AttributeValue> attrs) {

    DocumentAccessAttributesRecord record =
        new DocumentAccessAttributesRecord().documentId(ss(attrs, "documentId"))
            .key(ss(attrs, "key")).stringValue(ss(attrs, "stringValue"))
            .numberValue(nn(attrs, "numberValue")).booleanValue(bb(attrs, "booleanValue"));

    return record;
  }

  /**
   * Get Key.
   * 
   * @return {@link String}
   */
  public String getKey() {
    return this.key;
  }

  /**
   * Get {@link Double} value.
   * 
   * @return {@link Double}
   */
  public Double getNumberValue() {
    return this.numberValue;
  }

  /**
   * Get {@link String} value.
   * 
   * @return {@link String}
   */
  public String getStringValue() {
    return this.stringValue;
  }

  /**
   * Set Key.
   * 
   * @param accessAttributeKey {@link String}
   * @return {@link DocumentAccessAttributesRecord}
   */
  public DocumentAccessAttributesRecord key(final String accessAttributeKey) {
    this.key = accessAttributeKey;
    return this;
  }

  /**
   * Set {@link Double} value.
   * 
   * @param value {@link Double}
   * @return {@link DocumentAccessAttributesRecord}
   */
  public DocumentAccessAttributesRecord numberValue(final Double value) {
    this.numberValue = value;
    return this;
  }

  @Override
  public String pk(final String siteId) {
    if (this.documentId == null) {
      throw new IllegalArgumentException("'documentId' is required");
    }
    return createDatabaseKey(siteId, PREFIX_DOCS + this.documentId);
  }

  @Override
  public String pkGsi1(final String siteId) {
    return null;
  }

  @Override
  public String pkGsi2(final String siteId) {
    return null;
  }

  @Override
  public String sk() {
    return "accessAttributes";
  }

  @Override
  public String skGsi1() {
    return null;
  }

  @Override
  public String skGsi2() {
    return null;
  }

  /**
   * Set {@link String} value.
   * 
   * @param value {@link String}
   * @return {@link DocumentAccessAttributesRecord}
   */
  public DocumentAccessAttributesRecord stringValue(final String value) {
    this.stringValue = value;
    return this;
  }
}
