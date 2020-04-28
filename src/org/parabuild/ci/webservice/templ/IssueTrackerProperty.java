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

public class IssueTrackerProperty implements Serializable {

  private static final long serialVersionUID = 7355854379445188326L;
  private int ID;
  private String name;
  private long timeStamp;
  private int trackerID;
  private String value;


  public IssueTrackerProperty() {
  }


  public IssueTrackerProperty(
          final int ID,
          final String name,
          final long timeStamp,
          final int trackerID,
          final String value,
          final int valueAsInteger) {
    this.ID = ID;
    this.name = name;
    this.timeStamp = timeStamp;
    this.trackerID = trackerID;
    this.value = value;
  }


  /**
   * Gets the ID value for this IssueTrackerProperty.
   *
   * @return ID
   */
  public int getID() {
    return ID;
  }


  /**
   * Sets the ID value for this IssueTrackerProperty.
   *
   * @param ID
   */
  public void setID(final int ID) {
    this.ID = ID;
  }


  /**
   * Gets the name value for this IssueTrackerProperty.
   *
   * @return name
   */
  public String getName() {
    return name;
  }


  /**
   * Sets the name value for this IssueTrackerProperty.
   *
   * @param name
   */
  public void setName(final String name) {
    this.name = name;
  }


  /**
   * Gets the timeStamp value for this IssueTrackerProperty.
   *
   * @return timeStamp
   */
  public long getTimeStamp() {
    return timeStamp;
  }


  /**
   * Sets the timeStamp value for this IssueTrackerProperty.
   *
   * @param timeStamp
   */
  public void setTimeStamp(final long timeStamp) {
    this.timeStamp = timeStamp;
  }


  /**
   * Gets the trackerID value for this IssueTrackerProperty.
   *
   * @return trackerID
   */
  public int getTrackerID() {
    return trackerID;
  }


  /**
   * Sets the trackerID value for this IssueTrackerProperty.
   *
   * @param trackerID
   */
  public void setTrackerID(final int trackerID) {
    this.trackerID = trackerID;
  }


  /**
   * Gets the value value for this IssueTrackerProperty.
   *
   * @return value
   */
  public String getValue() {
    return value;
  }


  /**
   * Sets the value value for this IssueTrackerProperty.
   *
   * @param value
   */
  public void setValue(final String value) {
    this.value = value;
  }


  private Object __equalsCalc;


  public synchronized boolean equals(final Object obj) {
    if (!(obj instanceof IssueTrackerProperty)) return false;
    final IssueTrackerProperty other = (IssueTrackerProperty) obj;
    if (obj == null) return false;
    if (this == obj) return true;
    if (__equalsCalc != null) {
      return (__equalsCalc == obj);
    }
    __equalsCalc = obj;
    final boolean _equals;
    _equals = true &&
            this.ID == other.ID &&
            ((this.name == null && other.name == null) ||
                    (this.name != null &&
                            this.name.equals(other.name))) &&
            this.timeStamp == other.timeStamp &&
            this.trackerID == other.trackerID &&
            ((this.value == null && other.value == null) ||
                    (this.value != null &&
                            this.value.equals(other.value)));
    __equalsCalc = null;
    return _equals;
  }


  private boolean __hashCodeCalc;


  public synchronized int hashCode() {
    if (__hashCodeCalc) {
      return 0;
    }
    __hashCodeCalc = true;
    int _hashCode = 1;
    _hashCode += ID;
    if (name != null) {
      _hashCode += name.hashCode();
    }
    _hashCode += new Long(timeStamp).hashCode();
    _hashCode += trackerID;
    if (value != null) {
      _hashCode += value.hashCode();
    }
    __hashCodeCalc = false;
    return _hashCode;
  }


  // Type metadata
  private static final TypeDesc typeDesc =
          new TypeDesc(IssueTrackerProperty.class, true);


  static {
    typeDesc.setXmlType(new QName("http://org.parabuild.ci", "IssueTrackerProperty"));
    ElementDesc elemField = new ElementDesc();
    elemField.setFieldName("ID");
    elemField.setXmlName(new QName("http://org.parabuild.ci", "ID"));
    elemField.setXmlType(new QName("http://www.w3.org/2001/XMLSchema", "int"));
    elemField.setNillable(false);
    typeDesc.addFieldDesc(elemField);
    elemField = new ElementDesc();
    elemField.setFieldName("name");
    elemField.setXmlName(new QName("http://org.parabuild.ci", "name"));
    elemField.setXmlType(new QName("http://www.w3.org/2001/XMLSchema", "string"));
    elemField.setNillable(true);
    typeDesc.addFieldDesc(elemField);
    elemField = new ElementDesc();
    elemField.setFieldName("timeStamp");
    elemField.setXmlName(new QName("http://org.parabuild.ci", "timeStamp"));
    elemField.setXmlType(new QName("http://www.w3.org/2001/XMLSchema", "long"));
    elemField.setNillable(false);
    typeDesc.addFieldDesc(elemField);
    elemField = new ElementDesc();
    elemField.setFieldName("trackerID");
    elemField.setXmlName(new QName("http://org.parabuild.ci", "trackerID"));
    elemField.setXmlType(new QName("http://www.w3.org/2001/XMLSchema", "int"));
    elemField.setNillable(false);
    typeDesc.addFieldDesc(elemField);
    elemField = new ElementDesc();
    elemField.setFieldName("value");
    elemField.setXmlName(new QName("http://org.parabuild.ci", "value"));
    elemField.setXmlType(new QName("http://www.w3.org/2001/XMLSchema", "string"));
    elemField.setNillable(true);
    typeDesc.addFieldDesc(elemField);
    elemField = new ElementDesc();
    elemField.setFieldName("valueAsInteger");
    elemField.setXmlName(new QName("http://org.parabuild.ci", "valueAsInteger"));
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
