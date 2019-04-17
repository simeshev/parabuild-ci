/*
 * Parabuild CI licenses this file to You under the LGPL 2.1
 * (the "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *      https://www.gnu.org/licenses/lgpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.parabuild.ci.webservice.templ;

import org.apache.axis.description.ElementDesc;
import org.apache.axis.description.TypeDesc;
import org.apache.axis.encoding.Deserializer;
import org.apache.axis.encoding.Serializer;
import org.apache.axis.encoding.ser.BeanDeserializer;
import org.apache.axis.encoding.ser.BeanSerializer;

import javax.xml.namespace.QName;
import java.io.Serializable;

public class SystemProperty implements Serializable {

  private static final long serialVersionUID = -1181973163188363710L;
  private int propertyID;
  private String propertyName;
  private long propertyTimeStamp;
  private String propertyValue;
  private int propertyValueAsInt;


  public SystemProperty() {
  }


  public SystemProperty(
          final int propertyID,
          final String propertyName,
          final long propertyTimeStamp,
          final String propertyValue,
          final int propertyValueAsInt) {
    this.propertyID = propertyID;
    this.propertyName = propertyName;
    this.propertyTimeStamp = propertyTimeStamp;
    this.propertyValue = propertyValue;
    this.propertyValueAsInt = propertyValueAsInt;
  }


  /**
   * Gets the propertyID value for this SystemProperty.
   *
   * @return propertyID
   */
  public int getPropertyID() {
    return propertyID;
  }


  /**
   * Sets the propertyID value for this SystemProperty.
   *
   * @param propertyID
   */
  public void setPropertyID(final int propertyID) {
    this.propertyID = propertyID;
  }


  /**
   * Gets the propertyName value for this SystemProperty.
   *
   * @return propertyName
   */
  public String getPropertyName() {
    return propertyName;
  }


  /**
   * Sets the propertyName value for this SystemProperty.
   *
   * @param propertyName
   */
  public void setPropertyName(final String propertyName) {
    this.propertyName = propertyName;
  }


  /**
   * Gets the propertyTimeStamp value for this SystemProperty.
   *
   * @return propertyTimeStamp
   */
  public long getPropertyTimeStamp() {
    return propertyTimeStamp;
  }


  /**
   * Sets the propertyTimeStamp value for this SystemProperty.
   *
   * @param propertyTimeStamp
   */
  public void setPropertyTimeStamp(final long propertyTimeStamp) {
    this.propertyTimeStamp = propertyTimeStamp;
  }


  /**
   * Gets the propertyValue value for this SystemProperty.
   *
   * @return propertyValue
   */
  public String getPropertyValue() {
    return propertyValue;
  }


  /**
   * Sets the propertyValue value for this SystemProperty.
   *
   * @param propertyValue
   */
  public void setPropertyValue(final String propertyValue) {
    this.propertyValue = propertyValue;
  }


  /**
   * Gets the propertyValueAsInt value for this SystemProperty.
   *
   * @return propertyValueAsInt
   */
  public int getPropertyValueAsInt() {
    return propertyValueAsInt;
  }


  /**
   * Sets the propertyValueAsInt value for this SystemProperty.
   *
   * @param propertyValueAsInt
   */
  public void setPropertyValueAsInt(final int propertyValueAsInt) {
    this.propertyValueAsInt = propertyValueAsInt;
  }


  private Object __equalsCalc = null;


  public synchronized boolean equals(final Object obj) {
    if (!(obj instanceof SystemProperty)) return false;
    final SystemProperty other = (SystemProperty) obj;
    if (obj == null) return false;
    if (this == obj) return true;
    if (__equalsCalc != null) {
      return (__equalsCalc == obj);
    }
    __equalsCalc = obj;
    final boolean _equals;
    _equals = true &&
            this.propertyID == other.propertyID &&
            ((this.propertyName == null && other.propertyName == null) ||
                    (this.propertyName != null &&
                            this.propertyName.equals(other.propertyName))) &&
            this.propertyTimeStamp == other.propertyTimeStamp &&
            ((this.propertyValue == null && other.propertyValue == null) ||
                    (this.propertyValue != null &&
                            this.propertyValue.equals(other.propertyValue))) &&
            this.propertyValueAsInt == other.propertyValueAsInt;
    __equalsCalc = null;
    return _equals;
  }


  private boolean __hashCodeCalc = false;


  public synchronized int hashCode() {
    if (__hashCodeCalc) {
      return 0;
    }
    __hashCodeCalc = true;
    int _hashCode = 1;
    _hashCode += propertyID;
    if (propertyName != null) {
      _hashCode += propertyName.hashCode();
    }
    _hashCode += new Long(propertyTimeStamp).hashCode();
    if (propertyValue != null) {
      _hashCode += propertyValue.hashCode();
    }
    _hashCode += propertyValueAsInt;
    __hashCodeCalc = false;
    return _hashCode;
  }


  // Type metadata
  private static final TypeDesc typeDesc =
          new TypeDesc(SystemProperty.class, true);


  static {
    typeDesc.setXmlType(new QName("http://org.parabuild.ci", "SystemProperty"));
    ElementDesc elemField = new ElementDesc();
    elemField.setFieldName("propertyID");
    elemField.setXmlName(new QName("http://org.parabuild.ci", "propertyID"));
    elemField.setXmlType(new QName("http://www.w3.org/2001/XMLSchema", "int"));
    elemField.setNillable(false);
    typeDesc.addFieldDesc(elemField);
    elemField = new ElementDesc();
    elemField.setFieldName("propertyName");
    elemField.setXmlName(new QName("http://org.parabuild.ci", "propertyName"));
    elemField.setXmlType(new QName("http://www.w3.org/2001/XMLSchema", "string"));
    elemField.setNillable(true);
    typeDesc.addFieldDesc(elemField);
    elemField = new ElementDesc();
    elemField.setFieldName("propertyTimeStamp");
    elemField.setXmlName(new QName("http://org.parabuild.ci", "propertyTimeStamp"));
    elemField.setXmlType(new QName("http://www.w3.org/2001/XMLSchema", "long"));
    elemField.setNillable(false);
    typeDesc.addFieldDesc(elemField);
    elemField = new ElementDesc();
    elemField.setFieldName("propertyValue");
    elemField.setXmlName(new QName("http://org.parabuild.ci", "propertyValue"));
    elemField.setXmlType(new QName("http://www.w3.org/2001/XMLSchema", "string"));
    elemField.setNillable(true);
    typeDesc.addFieldDesc(elemField);
    elemField = new ElementDesc();
    elemField.setFieldName("propertyValueAsInt");
    elemField.setXmlName(new QName("http://org.parabuild.ci", "propertyValueAsInt"));
    elemField.setXmlType(new QName("http://www.w3.org/2001/XMLSchema", "int"));
    elemField.setNillable(false);
    typeDesc.addFieldDesc(elemField);
  }


  /**
   * Return type metadata object
   */
  public static TypeDesc getTypeDesc() {
    return typeDesc;
  }


  /**
   * Get Custom Serializer
   */
  public static Serializer getSerializer(
          final String mechType,
          final Class _javaType,
          final QName _xmlType) {
    return
            new BeanSerializer(
                    _javaType, _xmlType, typeDesc);
  }


  /**
   * Get Custom Deserializer
   */
  public static Deserializer getDeserializer(
          final String mechType,
          final Class _javaType,
          final QName _xmlType) {
    return
            new BeanDeserializer(
                    _javaType, _xmlType, typeDesc);
  }

}
