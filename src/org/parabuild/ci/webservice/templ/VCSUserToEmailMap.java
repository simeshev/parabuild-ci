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

public class VCSUserToEmailMap implements Serializable {

  private static final long serialVersionUID = -7867541996618296028L;
  private int buildID;
  private boolean disabled;
  private String instantMessengerAddress;
  private byte instantMessengerType;
  private int mapID;
  private long timeStamp;
  private String userEmail;
  private String userName;


  public VCSUserToEmailMap() {
  }


  public VCSUserToEmailMap(
          final int buildID,
          final boolean disabled,
          final String instantMessengerAddress,
          final byte instantMessengerType,
          final int mapID,
          final long timeStamp,
          final String userEmail,
          final String userName) {
    this.buildID = buildID;
    this.disabled = disabled;
    this.instantMessengerAddress = instantMessengerAddress;
    this.instantMessengerType = instantMessengerType;
    this.mapID = mapID;
    this.timeStamp = timeStamp;
    this.userEmail = userEmail;
    this.userName = userName;
  }


  /**
   * Gets the buildID value for this VCSUserToEmailMap.
   *
   * @return buildID
   */
  public int getBuildID() {
    return buildID;
  }


  /**
   * Sets the buildID value for this VCSUserToEmailMap.
   *
   * @param buildID
   */
  public void setBuildID(final int buildID) {
    this.buildID = buildID;
  }


  /**
   * Gets the disabled value for this VCSUserToEmailMap.
   *
   * @return disabled
   */
  public boolean isDisabled() {
    return disabled;
  }


  /**
   * Sets the disabled value for this VCSUserToEmailMap.
   *
   * @param disabled
   */
  public void setDisabled(final boolean disabled) {
    this.disabled = disabled;
  }


  /**
   * Gets the instantMessengerAddress value for this VCSUserToEmailMap.
   *
   * @return instantMessengerAddress
   */
  public String getInstantMessengerAddress() {
    return instantMessengerAddress;
  }


  /**
   * Sets the instantMessengerAddress value for this VCSUserToEmailMap.
   *
   * @param instantMessengerAddress
   */
  public void setInstantMessengerAddress(final String instantMessengerAddress) {
    this.instantMessengerAddress = instantMessengerAddress;
  }


  /**
   * Gets the instantMessengerType value for this VCSUserToEmailMap.
   *
   * @return instantMessengerType
   */
  public byte getInstantMessengerType() {
    return instantMessengerType;
  }


  /**
   * Sets the instantMessengerType value for this VCSUserToEmailMap.
   *
   * @param instantMessengerType
   */
  public void setInstantMessengerType(final byte instantMessengerType) {
    this.instantMessengerType = instantMessengerType;
  }


  /**
   * Gets the mapID value for this VCSUserToEmailMap.
   *
   * @return mapID
   */
  public int getMapID() {
    return mapID;
  }


  /**
   * Sets the mapID value for this VCSUserToEmailMap.
   *
   * @param mapID
   */
  public void setMapID(final int mapID) {
    this.mapID = mapID;
  }


  /**
   * Gets the timeStamp value for this VCSUserToEmailMap.
   *
   * @return timeStamp
   */
  public long getTimeStamp() {
    return timeStamp;
  }


  /**
   * Sets the timeStamp value for this VCSUserToEmailMap.
   *
   * @param timeStamp
   */
  public void setTimeStamp(final long timeStamp) {
    this.timeStamp = timeStamp;
  }


  /**
   * Gets the userEmail value for this VCSUserToEmailMap.
   *
   * @return userEmail
   */
  public String getUserEmail() {
    return userEmail;
  }


  /**
   * Sets the userEmail value for this VCSUserToEmailMap.
   *
   * @param userEmail
   */
  public void setUserEmail(final String userEmail) {
    this.userEmail = userEmail;
  }


  /**
   * Gets the userName value for this VCSUserToEmailMap.
   *
   * @return userName
   */
  public String getUserName() {
    return userName;
  }


  /**
   * Sets the userName value for this VCSUserToEmailMap.
   *
   * @param userName
   */
  public void setUserName(final String userName) {
    this.userName = userName;
  }


  private Object __equalsCalc;


  public synchronized boolean equals(final Object obj) {
    if (!(obj instanceof VCSUserToEmailMap)) return false;
    final VCSUserToEmailMap other = (VCSUserToEmailMap) obj;
    if (obj == null) return false;
    if (this == obj) return true;
    if (__equalsCalc != null) {
      return (__equalsCalc == obj);
    }
    __equalsCalc = obj;
    final boolean _equals;
    _equals = true &&
            this.buildID == other.buildID &&
            this.disabled == other.disabled &&
            ((this.instantMessengerAddress == null && other.instantMessengerAddress == null) ||
                    (this.instantMessengerAddress != null &&
                            this.instantMessengerAddress.equals(other.instantMessengerAddress))) &&
            this.instantMessengerType == other.instantMessengerType &&
            this.mapID == other.mapID &&
            this.timeStamp == other.timeStamp &&
            ((this.userEmail == null && other.userEmail == null) ||
                    (this.userEmail != null &&
                            this.userEmail.equals(other.userEmail))) &&
            ((this.userName == null && other.userName == null) ||
                    (this.userName != null &&
                            this.userName.equals(other.userName)));
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
    _hashCode += buildID;
    _hashCode += (disabled ? Boolean.TRUE : Boolean.FALSE).hashCode();
    if (instantMessengerAddress != null) {
      _hashCode += instantMessengerAddress.hashCode();
    }
    _hashCode += instantMessengerType;
    _hashCode += mapID;
    _hashCode += new Long(timeStamp).hashCode();
    if (userEmail != null) {
      _hashCode += userEmail.hashCode();
    }
    if (userName != null) {
      _hashCode += userName.hashCode();
    }
    __hashCodeCalc = false;
    return _hashCode;
  }


  // Type metadata
  private static final TypeDesc typeDesc =
          new TypeDesc(VCSUserToEmailMap.class, true);


  static {
    typeDesc.setXmlType(new QName("http://org.parabuild.ci", "VCSUserToEmailMap"));
    ElementDesc elemField = new ElementDesc();
    elemField.setFieldName("buildID");
    elemField.setXmlName(new QName("http://org.parabuild.ci", "buildID"));
    elemField.setXmlType(new QName("http://www.w3.org/2001/XMLSchema", "int"));
    elemField.setNillable(false);
    typeDesc.addFieldDesc(elemField);
    elemField = new ElementDesc();
    elemField.setFieldName("disabled");
    elemField.setXmlName(new QName("http://org.parabuild.ci", "disabled"));
    elemField.setXmlType(new QName("http://www.w3.org/2001/XMLSchema", "boolean"));
    elemField.setNillable(false);
    typeDesc.addFieldDesc(elemField);
    elemField = new ElementDesc();
    elemField.setFieldName("instantMessengerAddress");
    elemField.setXmlName(new QName("http://org.parabuild.ci", "instantMessengerAddress"));
    elemField.setXmlType(new QName("http://www.w3.org/2001/XMLSchema", "string"));
    elemField.setNillable(true);
    typeDesc.addFieldDesc(elemField);
    elemField = new ElementDesc();
    elemField.setFieldName("instantMessengerType");
    elemField.setXmlName(new QName("http://org.parabuild.ci", "instantMessengerType"));
    elemField.setXmlType(new QName("http://www.w3.org/2001/XMLSchema", "byte"));
    elemField.setNillable(false);
    typeDesc.addFieldDesc(elemField);
    elemField = new ElementDesc();
    elemField.setFieldName("mapID");
    elemField.setXmlName(new QName("http://org.parabuild.ci", "mapID"));
    elemField.setXmlType(new QName("http://www.w3.org/2001/XMLSchema", "int"));
    elemField.setNillable(false);
    typeDesc.addFieldDesc(elemField);
    elemField = new ElementDesc();
    elemField.setFieldName("timeStamp");
    elemField.setXmlName(new QName("http://org.parabuild.ci", "timeStamp"));
    elemField.setXmlType(new QName("http://www.w3.org/2001/XMLSchema", "long"));
    elemField.setNillable(false);
    typeDesc.addFieldDesc(elemField);
    elemField = new ElementDesc();
    elemField.setFieldName("userEmail");
    elemField.setXmlName(new QName("http://org.parabuild.ci", "userEmail"));
    elemField.setXmlType(new QName("http://www.w3.org/2001/XMLSchema", "string"));
    elemField.setNillable(true);
    typeDesc.addFieldDesc(elemField);
    elemField = new ElementDesc();
    elemField.setFieldName("userName");
    elemField.setXmlName(new QName("http://org.parabuild.ci", "userName"));
    elemField.setXmlType(new QName("http://www.w3.org/2001/XMLSchema", "string"));
    elemField.setNillable(true);
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
