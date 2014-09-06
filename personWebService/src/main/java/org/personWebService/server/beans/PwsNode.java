/**
 * @author mchyzer
 * $Id$
 */
package org.personWebService.server.beans;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

import org.personWebService.server.util.PersonWsServerUtils;


/**
 * node of the representation
 */
public class PwsNode {

  /**
   * field name we are coming from (for debugging reasons)
   */
  private String fromFieldName;
  
  /**
   * link back up to the parent node
   */
  private PwsNode fromNode;
  
  /**
   * link back up to the parent node
   * @return the fromNode
   */
  public PwsNode getFromNode() {
    return this.fromNode;
  }
  
  /**
   * link back up to the parent node
   * @param fromNode1 the fromNode to set
   */
  public void setFromNode(PwsNode fromNode1) {
    this.fromNode = fromNode1;
  }

  /**
   * field name we are coming from (for debugging reasons)
   * @return the fromFieldName
   */
  public String getFromFieldName() {
    return this.fromFieldName;
  }
  
  /**
   * field name we are coming from (for debugging reasons)
   * @param fromFieldName1 the fromFieldName to set
   */
  public void setFromFieldName(String fromFieldName1) {
    this.fromFieldName = fromFieldName1;
  }

  /**
   * 
   * @param someInteger
   */
  public PwsNode(Long someInteger) {
    this.setPwsNodeType(PwsNodeType.integer);
    this.integer = someInteger;
  }
  
  /**
   * 
   * @param someInteger
   */
  public PwsNode(Integer someInteger) {
    this.setPwsNodeType(PwsNodeType.integer);
    this.integer = someInteger == null ? null : someInteger.longValue();
  }
  
  /**
   * construct with boolean
   * @param someBoolean
   */
  public PwsNode(Boolean someBoolean) {
    this.setPwsNodeType(PwsNodeType.bool);
    this.bool = someBoolean;
  }
  
  /**
   * construct with floating
   * @param someFloating
   */
  public PwsNode(Double someFloating) {
    this.setPwsNodeType(PwsNodeType.floating);
    this.floating = someFloating;
  }
  
  /**
   * 
   * @param someString
   */
  public PwsNode(String someString) {
    this.setPwsNodeType(PwsNodeType.string);
    this.string = someString;
  }
  
  /**
   * which type of node this is
   * @return the pwsNodeType
   */
  public PwsNodeType getPwsNodeType() {
    return this.pwsNodeType;
  }

  
  /**
   * which type of node this is
   * @param pwsNodeType1 the pwsNodeType to set
   */
  public void setPwsNodeType(PwsNodeType pwsNodeType1) {
    this.pwsNodeType = pwsNodeType1;
  }

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    PwsNode base = new PwsNode(PwsNodeType.object);
    base.assignField("someInteger", new PwsNode(45L));
    base.assignField("someFloat", new PwsNode(34.567));
    base.assignField("someFloatInt", new PwsNode(34D));
    base.assignField("someBoolTrue", new PwsNode(true));
    base.assignField("someBoolFalse", new PwsNode(false));
    base.assignField("someString", new PwsNode("some string"));
    base.assignField("nullString", new PwsNode((String)null));
    base.assignField("nullInteger", new PwsNode((Long)null));
    
    PwsNode sub = new PwsNode(PwsNodeType.object);
    sub.assignField("subInteger", new PwsNode(37L));
    sub.assignField("subString", new PwsNode("sub string"));

    base.assignField("sub", sub);

    PwsNode sub1 = new PwsNode(PwsNodeType.object);
    sub1.assignField("subInteger", new PwsNode(37L));
    sub1.assignField("subString", new PwsNode("sub string"));
    PwsNode sub2 = new PwsNode(PwsNodeType.object);
    sub2.assignField("subInteger", new PwsNode(37L));
    sub2.assignField("subString", new PwsNode("sub string"));
    
    PwsNode arraySub = new PwsNode(PwsNodeType.object);
    arraySub.setArrayType(true);
    
    arraySub.addArrayItem(sub1);
    arraySub.addArrayItem(sub2);
    
    base.assignField("arraySub", arraySub);

    PwsNode arrayInteger = new PwsNode(PwsNodeType.integer);
    arrayInteger.setArrayType(true);
    arrayInteger.addArrayItem(new PwsNode(28L));
    arrayInteger.addArrayItem(new PwsNode(17L));
    arrayInteger.addArrayItem(new PwsNode(9L));

    base.assignField("arrayInteger", arrayInteger);

    PwsNode arrayString = new PwsNode(PwsNodeType.string);
    arrayString.setArrayType(true);
    arrayString.addArrayItem(new PwsNode("abc"));
    arrayString.addArrayItem(new PwsNode("123"));
    arrayString.addArrayItem(new PwsNode("true"));

    base.assignField("arrayString", arrayString);

    String json = base.toJson();

    //{"someInteger":45,"someFloat":34.567,"someFloatInt":34,"someBoolTrue":true,"someBoolFalse":false,"someString":"some string","sub":{"subInteger":37,"subString":"sub string"},"arraySub":[{"subInteger":37,"subString":"sub string"},{"subInteger":37,"subString":"sub string"}],"arrayInteger":[28,17,9],"arrayString":["abc","123","true"]}
    
    System.out.println(json);
    
    PwsNode anotherNode = PwsNode.fromJson(json);
    
    json = anotherNode.toJson();
    
    System.out.println("-------------");
    
    System.out.println(json);
    
  }

  /**
   * convert from object to pws object
   * @param fromNode parent node
   * @param fromFieldName field name this is assigned to (for debugging)
   * @param value
   * @return the PwsObject
   */
  private static PwsNode convertFromJsonObject(PwsNode fromNode, String fromFieldName, Object value) {
    
    if (value == null) {
      PwsNode result = new PwsNode(PwsNodeType.object);
      result.setFromNode(fromNode);
      result.setFromFieldName(fromFieldName);
      return result;
    }
      
    if (value instanceof Boolean) {
      
      PwsNode result = new PwsNode((Boolean)value);
      result.setFromNode(fromNode);
      result.setFromFieldName(fromFieldName);
      return result;
    }

    if (value instanceof Double || value instanceof Float) {
      
      PwsNode result = new PwsNode(((Number)value).doubleValue());
      result.setFromNode(fromNode);
      result.setFromFieldName(fromFieldName);
      return result;
    }

    if (value instanceof Integer || value instanceof Long) {

      PwsNode result = new PwsNode(((Number)value).longValue());
      result.setFromNode(fromNode);
      result.setFromFieldName(fromFieldName);
      return result;
    } 

    if (value instanceof String) {

      PwsNode result = new PwsNode((String)value);
      result.setFromNode(fromNode);
      result.setFromFieldName(fromFieldName);
      return result;
    } 

    if (value instanceof JSONArray) {
      
      JSONArray jsonArray = (JSONArray)value;
      
      PwsNode pwsNode = new PwsNode();
      pwsNode.setArrayType(true);
      pwsNode.setFromNode(fromNode);
      pwsNode.setFromFieldName(fromFieldName);
      
      boolean foundType = false;
      
      for (int i=0;i<jsonArray.size();i++) {
        
        Object arrayObject = jsonArray.get(i);
        
        PwsNode arrayNode = convertFromJsonObject(fromNode, fromFieldName, arrayObject);
        if (!foundType) {
          pwsNode.setPwsNodeType(arrayNode.getPwsNodeType());
          foundType = true;
        }
        pwsNode.addArrayItem(arrayNode);
      }
      return pwsNode;
    } 

    if (value instanceof JSONObject) {
      JSONObject jsonObject = (JSONObject)value;
      PwsNode pwsNode = new PwsNode();
      pwsNode.setFromNode(fromNode);
      pwsNode.setFromFieldName(fromFieldName);
      pwsNode.setPwsNodeType(PwsNodeType.object);
      for (String key : (Set<String>)jsonObject.keySet()) {
        
        Object fieldValue = jsonObject.get(key);
        PwsNode fieldNode = convertFromJsonObject(pwsNode, key, fieldValue);
        pwsNode.assignField(key, fieldNode);

      }
      return pwsNode;
    }
    
    throw new RuntimeException(" value type not supported: " + value.getClass().getName());
  }
  
  /**
   * parse a json string into a PWS node
   * @param json 
   * @return the PwsNode or null if no json
   */
  public static PwsNode fromJson(String json) {
    if (!PersonWsServerUtils.isBlank(json)) {
      JSONObject jsonObject = (JSONObject) JSONSerializer.toJSON( json ); 
      PwsNode pwsNode = convertFromJsonObject(null, null, jsonObject);
      return pwsNode;
    }
    return null;
  }
  
  /**
   * convert this object to json
   * @return the json
   */
  public String toJson() {
    
    JSONObject jsonObject = this.toJsonObjectHelper();
    
    return jsonObject.toString();
    
  }

  /**
   * assign an object from the fromObject to this object
   * @param theFromNode
   */
  public void cloneNode(PwsNode theFromNode) {
    this.cloneNodeHelper(theFromNode);
  }

  /**
   * assign an object from the fromObject to this object
   * @param theFromNode
   */
  private void cloneNodeHelper(PwsNode theFromNode) {

    //copy over data
    this.arrayType = theFromNode.arrayType;
    this.bool = theFromNode.bool;
    this.floating = theFromNode.floating;
    //name and node might be different
    //this.fromFieldName = fromNode.fromFieldName
    //this.fromNode = theFromNode.fromNode;
    this.integer = theFromNode.integer;
    this.pwsNodeType = theFromNode.pwsNodeType;
    this.string = theFromNode.string;

    //we need to clone the field
    if (this.pwsNodeType == PwsNodeType.object && theFromNode.object != null && theFromNode.object.keySet() != null) {
      
      //go through each field in the object
      for (String fieldName : theFromNode.object.keySet()) {
        
        PwsNode field = theFromNode.object.get(fieldName);
        PwsNode clonedField = null;
        if (field != null) {
          clonedField = new PwsNode();
          clonedField.setFromFieldName(fieldName);
          clonedField.setFromNode(this);
          clonedField.cloneNode(field);
        }
        this.assignField(fieldName, clonedField);
      }
      
    }
    
    if (this.arrayType && theFromNode.array != null && theFromNode.array.size() > 0) {
      //we need to clone each item in the array
      for (PwsNode arrayElementNode : theFromNode.array) {
        PwsNode clonedField = null;
        if (arrayElementNode != null) {
          clonedField = new PwsNode();
          //should this be an array index???
          clonedField.setFromFieldName(this.getFromFieldName());
          clonedField.setFromNode(this);
          clonedField.cloneNode(arrayElementNode);
        }
        this.addArrayItem(clonedField);
      }
    }
    
  }

  /**
   * get the array if this is array type
   * @return the array
   */
  public List<PwsNode> getArray() {
    if (!this.isArrayType()) {
      throw new RuntimeException("Not array type");
    }
    return this.array;
  }
  
  /**
   * 
   * @return the json object
   */
  private JSONObject toJsonObjectHelper() {
    
    if (this.pwsNodeType != PwsNodeType.object) {
      throw new RuntimeException("expecting node type of object: " + this.pwsNodeType);
    }
    
    JSONObject jsonObject = new JSONObject();

    //lets go through the fields
    if (this.object != null && this.object.keySet() != null) {
      for (String fieldName : this.object.keySet()) {
        
        PwsNode field = this.object.get(fieldName);
  
        if (field.isArrayType()) {
  
          if (field.array == null) {
            jsonObject.element(fieldName, (Object)null);
          } else {
          
            JSONArray jsonArray = new JSONArray();
  
            for (PwsNode item : field.array) {
  
              if (item.isArrayType()) {
                throw new RuntimeException("Doesnt currently support array of arrays: " + fieldName);
              }
              
              switch (field.getPwsNodeType()) {
                case integer:
                  Long theInteger = item.getInteger();
                  jsonArray.add(theInteger);
                  break;
                case bool:
                  Boolean theBoolean = item.getBool();
                  jsonArray.add(theBoolean);
                  break;
                case floating:
                  Double theFloating = item.getFloating();
                  jsonArray.add(theFloating);
                  break;
                case string:
                  String theString = item.getString();
                  jsonArray.add(theString);
                  break;
                case object:
                  JSONObject jsonItem = item == null ? null : item.toJsonObjectHelper();
                  jsonArray.add(jsonItem);
                  break;
                default: 
                  throw new RuntimeException("Not expecting pws node type: " + field.getPwsNodeType());
              }
  
            }
  
            jsonObject.element(fieldName, jsonArray);
  
            
          }
          
        } else {
          switch (field.getPwsNodeType()) {
            case integer:
              Long theInteger = field.getInteger();
              if (theInteger != null) {
                jsonObject.element(fieldName, theInteger.longValue());
              } else {
                jsonObject.element(fieldName, (Object)null);
              }
              break;
            case bool:
              Boolean theBoolean = field.getBool();
              if (theBoolean != null) {
                jsonObject.element(fieldName, theBoolean.booleanValue());
              } else {
                jsonObject.element(fieldName, (Object)null);
              }
              break;
            case floating:
              Double theFloating = field.getFloating();
              if (theFloating != null) {
                jsonObject.element(fieldName, theFloating.doubleValue());
              } else {
                jsonObject.element(fieldName, (Object)null);
              }
              break;
            case string:
              String theString = field.getString();
              if (theString != null) {
                jsonObject.element(fieldName, theString);
              } else {
                jsonObject.element(fieldName, (Object)null);
              }
              break;
            case object:
              
              if (field.object == null) {
                jsonObject.element(fieldName, (Object)null);
              } else {
                JSONObject fieldObject = field.toJsonObjectHelper();
                jsonObject.element(fieldName, fieldObject);
              }
              break;
            default: 
              throw new RuntimeException("Not expecting pws node type: " + field.getPwsNodeType());
          }
        }      
        
      }
    }
    return jsonObject;
  }
  
  /**
   * this doesnt have to be called to assign a field, but if you want an empty object instead of null, call this
   */
  public void initObjectIfNull() {
    if (this.object == null) {
      this.object = new LinkedHashMap<String, PwsNode>();
    }
  }
  
  /**
   * if this is an array
   * @return the arrayType
   */
  public boolean isArrayType() {
    return this.arrayType;
  }

  
  /**
   * if this is an array
   * @param arrayType1 the arrayType to set
   */
  public void setArrayType(boolean arrayType1) {
    this.arrayType = arrayType1;
  }

  /**
   * type of node
   */
  public static enum PwsNodeType {
    
    /** scalar string value */
    string,
    
    /** scalar integer value */
    integer,
    
    /** scalar boolean value */
    bool,
    
    /** scalar floating point value */
    floating,
    
    /** has fields and nodes */
    object,
  }

  /**
   * if this is an array
   */
  private boolean arrayType;
  
  /**
   * node
   */
  public PwsNode() {
    
  }
  
  /**
   * node with type
   * @param pwsNodeType1
   */
  public PwsNode(PwsNodeType pwsNodeType1) {
    this.pwsNodeType = pwsNodeType1;
  }
  
  /**
   * which type of node this is
   */
  private PwsNodeType pwsNodeType;

  /**
   * if it is an object, these are the fields
   */
  private Map<String, PwsNode> object;

  /**
   * if it is an array, these are the objects in the array
   */
  private List<PwsNode> array;

  /**
   * add an array item
   * @param pwsNode to add
   */
  public void addArrayItem(PwsNode pwsNode) {
    if (!this.arrayType) {
      throw new RuntimeException("expecting node type of array");
    }
    
    if (pwsNode != null) {
      if (this.pwsNodeType != pwsNode.getPwsNodeType()) {
        throw new RuntimeException("expecting array type of " + this.pwsNodeType 
            + ", but assigning: " + pwsNode.getPwsNodeType());
      }
    }
    
    if (this.array == null) {
      this.array = new ArrayList<PwsNode>();
    }
    
    this.array.add(pwsNode);
  }

  /**
   * add an array item
   * @param index
   * @param pwsNode to add
   */
  public void assignArrayItem(int index, PwsNode pwsNode) {
    if (!this.arrayType) {
      throw new RuntimeException("expecting node type of array");
    }
    
    if (pwsNode != null) {
      if (this.pwsNodeType != pwsNode.getPwsNodeType()) {
        throw new RuntimeException("expecting array type of " + this.pwsNodeType 
            + ", but assigning: " + pwsNode.getPwsNodeType());
      }
    }
    
    if (this.array == null) {
      this.array = new ArrayList<PwsNode>();
    }

    int length = PersonWsServerUtils.length(this.array);
    if (length - 1 < index) {
      throw new RuntimeException("Trying to get index: " + index + ", but array is only length: " + length);
    }
    
    this.array.set(index, pwsNode);
  }

  /**
   * get the array length
   * @return the array length
   */
  public int getArrayLength() {
    if (!this.arrayType) {
      throw new RuntimeException("expecting node type of array");
    }

    return PersonWsServerUtils.length(this.array);
  }

  /**
   * retrieve an array index
   * @param index 0 indexed
   * @return the node
   */
  public PwsNode retrieveArrayItem(int index) {
    
    if (!this.arrayType) {
      throw new RuntimeException("expecting node type of array");
    }

    int length = PersonWsServerUtils.length(this.array);
    if (length - 1 < index) {
      throw new RuntimeException("Trying to get index: " + index + ", but array is only length: " + length);
    }
    
    return this.array.get(index);
  }
  
  /**
   * assign a field.  Note if null will not remove
   * @param fieldName
   * @param pwsNode
   */
  public void assignField(String fieldName, PwsNode pwsNode) {
    
    if (this.pwsNodeType != PwsNodeType.object) {
      throw new RuntimeException("expecting node type of object: " + this.pwsNodeType);
    }
    
    if (this.object == null) {
      this.object = new LinkedHashMap<String, PwsNode>();
    }
    
    this.object.put(fieldName, pwsNode);
    
  }

  /**
   * retrieve a field from an object
   * @param fieldName
   * @return the field or null if not there
   */
  public PwsNode retrieveField(String fieldName) {
    if (this.pwsNodeType != PwsNodeType.object) {
      throw new RuntimeException("expecting node type of object: " + this.pwsNodeType + ", " + fieldName);
    }

    if (this.object != null) {
      return this.object.get(fieldName);
    }
    return null;
    
  }

  /**
   * retrieve the field names from an object, might be null
   * @return the set of string field names
   */
  public Set<String> getFieldNames() {
    if (this.pwsNodeType != PwsNodeType.object) {
      throw new RuntimeException("expecting node type of object: " + this.pwsNodeType);
    }
    
    if (this.object != null) {
      return this.object.keySet();
    }
    return null;
    
  }
  
  /**
   * remove a field from the object
   * @param fieldName
   * @return the PwsNode that was removed or null if none
   */
  public PwsNode removeField(String fieldName) {
    if (this.pwsNodeType != PwsNodeType.object) {
      throw new RuntimeException("expecting node type of object: " + this.pwsNodeType);
    }
    
    if (this.object != null) {
      return this.object.remove(fieldName);
    }
    return null;
  }
  
  /**
   * string value 
   */
  private String string;
  
  /**
   * integer value
   */
  private Long integer;

  /**
   * boolean value
   */
  private Boolean bool;
  
  /**
   * floating value
   */
  private Double floating;
  
  /**
   * @return the bool
   */
  public Boolean getBool() {
    return this.bool;
  }

  /**
   * @param bool1 the bool to set
   */
  public void setBool(Boolean bool1) {
    if (this.pwsNodeType != PwsNodeType.bool) {
      throw new RuntimeException("expecting node type of bool: " + this.pwsNodeType);
    }
    this.bool = bool1;
  }

  
  /**
   * @return the floating
   */
  public Double getFloating() {
    return this.floating;
  }
  
  /**
   * @param floating1 the floating to set
   */
  public void setFloating(Double floating1) {
    if (this.pwsNodeType != PwsNodeType.floating) {
      throw new RuntimeException("expecting node type of floating: " + this.pwsNodeType);
    }
    this.floating = floating1;
  }

  /**
   * @return the string
   */
  public String getString() {
    return this.string;
  }

  
  /**
   * @param string1 the string to set
   */
  public void setString(String string1) {
    if (this.pwsNodeType != PwsNodeType.string) {
      throw new RuntimeException("expecting node type of string: " + this.pwsNodeType);
    }
    
    this.string = string1;
  }

  
  /**
   * @return the theInteger
   */
  public Long getInteger() {
    return this.integer;
  }

  
  /**
   * @param theInteger1 the theInteger to set
   */
  public void setInteger(Long theInteger1) {

    if (this.pwsNodeType != PwsNodeType.integer) {
      throw new RuntimeException("expecting node type of integer: " + this.pwsNodeType);
    }

    this.integer = theInteger1;
  }
  
  
  
}
