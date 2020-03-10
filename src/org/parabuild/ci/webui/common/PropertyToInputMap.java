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
package org.parabuild.ci.webui.common;

import org.parabuild.ci.util.IoUtils;
import org.parabuild.ci.util.StringUtils;
import viewtier.ui.AbstractInput;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * This map is used to simplify access to fields via their
 * corresponding property names
 */
public final class PropertyToInputMap implements Serializable {

  private static final long serialVersionUID = 2416070308102742329L; // NOPMD

  private final HashMap<String, AbstractInput> propertyNameToInputMap = new HashMap<String, AbstractInput>(5);
  private final HashMap<String, Object> propertyNameToPropertyObjectMap = new HashMap<String, Object>(5);
  private boolean loadUnmappedProperties = true;
  private final PropertyHandler propertyHandler;
  private boolean updateOnlyFromEditableFields = false;


  /**
   * Creates PropertyToInput map. Validation for acceptability of
   * the property name will not be done.
   */
  public PropertyToInputMap(final PropertyHandler propertyHandler) {
    this.propertyHandler = propertyHandler;
  }


  /**
   * Creates PropertyToInput map. If strict == true, property
   * names will be validated that are accepted by the map.
   */
  public PropertyToInputMap(final boolean loadUnmappedProperties, final PropertyHandler propertyHandler) {
    this.propertyHandler = propertyHandler;
    this.loadUnmappedProperties = loadUnmappedProperties;
  }


  /**
   * Binds property name to input
   *
   * @param propertyName to associate with input
   * @param input        propertyName will be associated with
   */
  public void bindPropertyNameToInput(final String propertyName, final AbstractInput input) {
    propertyNameToInputMap.put(propertyName, input);
  }


  private void setInputValue(final String propertyName, final String value) {
    final AbstractInput input = getInputFromMap(propertyName);
    if (input != null) {
      if (input instanceof EncryptingPassword) {
        // value contains encrypted password
        final EncryptingPassword ep = (EncryptingPassword) input;
        ep.setEncryptedValue(value);
      } else if (input instanceof CodeNameDropDown) { // see #777
        final CodeNameDropDown codeNameDropDown = (CodeNameDropDown) input;
        if (StringUtils.isValidInteger(value)) {
          try {
            codeNameDropDown.setCode(Integer.parseInt(value));
          } catch (final IllegalArgumentException e) { // in case this is something we did not expect
            IoUtils.ignoreExpectedException(e);
          }
        } else {
          codeNameDropDown.setSelection(value);
        }
      } else {
        input.setValue(value);
      }
    }
  }


  /**
   * Returns properties list updated with values from input
   * fields. If a property wasn't in the original list, it will
   * be added to the list.
   * <p/>
   * Process of making new properties and setting their values is
   * handled buy custom implementation of the PropertyHandler
   * interface.
   *
   * @see PropertyHandler
   */
  public List getUpdatedProperties() {

    final List result = new ArrayList(11);
    // traverse property name to input map
    for (final Iterator i = propertyNameToInputMap.entrySet().iterator(); i.hasNext(); ) {
      // get prop name and associated input
      final Map.Entry entry = (Map.Entry) i.next();
      final String propName = (String) entry.getKey();
      final AbstractInput input = (AbstractInput) entry.getValue();
      if (updateOnlyFromEditableFields && !input.isEditable()) {
        continue;
      }
      // lookup property value, if any
      Object property = propertyNameToPropertyObjectMap.get(propName);
      if (property == null) {
        // there is no property in the resulting list,
        // create new one and set values
        property = propertyHandler.makeProperty(propName);
      }
      // set value
      final String value;
      if (input instanceof EncryptingPassword) {
        final EncryptingPassword ep = (EncryptingPassword) input;
        value = ep.getEncryptedValue();
      } else if (input instanceof CodeNameDropDown) { // see #777
        final CodeNameDropDown codeNameDropDown = (CodeNameDropDown) input;
        value = Integer.toString(codeNameDropDown.getCode());
      } else {
        value = input.getValue();
      }
      propertyHandler.setPropertyValue(property, value);
      result.add(property);
    }

    // add properties that were listed but weren't bound/mapped
    for (final Iterator i = propertyNameToPropertyObjectMap.entrySet().iterator(); i.hasNext(); ) {
      final Map.Entry entry = (Map.Entry) i.next();
      if (!propertyNameToInputMap.containsKey(entry.getKey())) {
        result.add(entry.getValue());
      }
    }
    return result;
  }


  /**
   * Sets properties list and sets inputs values from this list
   *
   * @param props the properties list.
   */
  public void setProperties(final List props) {
    for (final Iterator iter = props.iterator(); iter.hasNext(); ) {
      final Object property = iter.next();
      final String propertyName = propertyHandler.getPropertyName(property);
      // don't load if it's unmapped
      if (!loadUnmappedProperties && getInputFromMap(propertyName) == null) {
        continue;
      }
      final String propertyValue = propertyHandler.getPropertyValue(property);
      propertyNameToPropertyObjectMap.put(propertyName, property);
      setInputValue(propertyName, propertyValue);
    }
  }


  private AbstractInput getInputFromMap(final String propertyName) {
    return propertyNameToInputMap.get(propertyName);
  }


  /**
   * Sets a a flag controlling updates from fields to
   * properties.
   *
   * @param updateOnlyFromEditableFields If true {@link #getUpdatedProperties()}
   *                                     will return values only for editable fields.
   */
  public void setUpdateOnlyFromEditableFields(final boolean updateOnlyFromEditableFields) {
    this.updateOnlyFromEditableFields = updateOnlyFromEditableFields;
  }


  /**
   * @noinspection PublicInnerClass
   */
  public interface PropertyHandler extends Serializable {

    /**
     * Creates new property using given propertyName and abstract
     * input as value source. Implementing class should assign
     * property name and value from property to the new
     * property.
     *
     * @return new property instance created using given propertyName and
     * abstract input as value source.
     */
    Object makeProperty(String propertyName);


    /**
     * Implementing class should set value of the property from
     * given String
     */
    void setPropertyValue(Object property, String propertyValue);


    /**
     * Implementing class should return string value from the
     * given property.
     */
    String getPropertyValue(Object property);


    /**
     * Implementing class should return string name from the
     * given property.
     */
    String getPropertyName(Object property);
  }


  public String toString() {
    return "PropertyToInputMap{" +
            "propertyNameToInputMap=" + propertyNameToInputMap +
            ", propertyNameToPropertyObjectMap=" + propertyNameToPropertyObjectMap +
            ", loadUnmappedProperties=" + loadUnmappedProperties +
            ", propertyHandler=" + propertyHandler +
            ", updateOnlyFromEditableFields=" + updateOnlyFromEditableFields +
            '}';
  }
}
