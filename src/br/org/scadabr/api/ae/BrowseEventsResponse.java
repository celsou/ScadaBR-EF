/**
 * BrowseEventsResponse.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package br.org.scadabr.api.ae;

public class BrowseEventsResponse  implements java.io.Serializable {
    private br.org.scadabr.api.vo.APIError[] errors;

    private br.org.scadabr.api.vo.EventDefinition[] eventsList;

    private br.org.scadabr.api.vo.ReplyBase replyBase;

    public BrowseEventsResponse() {
    }

    public BrowseEventsResponse(
           br.org.scadabr.api.vo.APIError[] errors,
           br.org.scadabr.api.vo.EventDefinition[] eventsList,
           br.org.scadabr.api.vo.ReplyBase replyBase) {
           this.errors = errors;
           this.eventsList = eventsList;
           this.replyBase = replyBase;
    }


    /**
     * Gets the errors value for this BrowseEventsResponse.
     * 
     * @return errors
     */
    public br.org.scadabr.api.vo.APIError[] getErrors() {
        return errors;
    }


    /**
     * Sets the errors value for this BrowseEventsResponse.
     * 
     * @param errors
     */
    public void setErrors(br.org.scadabr.api.vo.APIError[] errors) {
        this.errors = errors;
    }

    public br.org.scadabr.api.vo.APIError getErrors(int i) {
        return this.errors[i];
    }

    public void setErrors(int i, br.org.scadabr.api.vo.APIError _value) {
        this.errors[i] = _value;
    }


    /**
     * Gets the eventsList value for this BrowseEventsResponse.
     * 
     * @return eventsList
     */
    public br.org.scadabr.api.vo.EventDefinition[] getEventsList() {
        return eventsList;
    }


    /**
     * Sets the eventsList value for this BrowseEventsResponse.
     * 
     * @param eventsList
     */
    public void setEventsList(br.org.scadabr.api.vo.EventDefinition[] eventsList) {
        this.eventsList = eventsList;
    }

    public br.org.scadabr.api.vo.EventDefinition getEventsList(int i) {
        return this.eventsList[i];
    }

    public void setEventsList(int i, br.org.scadabr.api.vo.EventDefinition _value) {
        this.eventsList[i] = _value;
    }


    /**
     * Gets the replyBase value for this BrowseEventsResponse.
     * 
     * @return replyBase
     */
    public br.org.scadabr.api.vo.ReplyBase getReplyBase() {
        return replyBase;
    }


    /**
     * Sets the replyBase value for this BrowseEventsResponse.
     * 
     * @param replyBase
     */
    public void setReplyBase(br.org.scadabr.api.vo.ReplyBase replyBase) {
        this.replyBase = replyBase;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof BrowseEventsResponse)) return false;
        BrowseEventsResponse other = (BrowseEventsResponse) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.errors==null && other.getErrors()==null) || 
             (this.errors!=null &&
              java.util.Arrays.equals(this.errors, other.getErrors()))) &&
            ((this.eventsList==null && other.getEventsList()==null) || 
             (this.eventsList!=null &&
              java.util.Arrays.equals(this.eventsList, other.getEventsList()))) &&
            ((this.replyBase==null && other.getReplyBase()==null) || 
             (this.replyBase!=null &&
              this.replyBase.equals(other.getReplyBase())));
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
        if (getErrors() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getErrors());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getErrors(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        if (getEventsList() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getEventsList());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getEventsList(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        if (getReplyBase() != null) {
            _hashCode += getReplyBase().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(BrowseEventsResponse.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://ae.api.scadabr.org.br", ">BrowseEventsResponse"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("errors");
        elemField.setXmlName(new javax.xml.namespace.QName("http://ae.api.scadabr.org.br", "errors"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://vo.api.scadabr.org.br", "APIError"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        elemField.setMaxOccursUnbounded(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("eventsList");
        elemField.setXmlName(new javax.xml.namespace.QName("http://ae.api.scadabr.org.br", "eventsList"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://vo.api.scadabr.org.br", "EventDefinition"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        elemField.setMaxOccursUnbounded(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("replyBase");
        elemField.setXmlName(new javax.xml.namespace.QName("http://ae.api.scadabr.org.br", "replyBase"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://vo.api.scadabr.org.br", "ReplyBase"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
    }

    /**
     * Return type metadata object
     */
    public static org.apache.axis.description.TypeDesc getTypeDesc() {
        return typeDesc;
    }

    /**
     * Get Custom Serializer
     */
    public static org.apache.axis.encoding.Serializer getSerializer(
           java.lang.String mechType, 
           java.lang.Class _javaType,  
           javax.xml.namespace.QName _xmlType) {
        return 
          new  org.apache.axis.encoding.ser.BeanSerializer(
            _javaType, _xmlType, typeDesc);
    }

    /**
     * Get Custom Deserializer
     */
    public static org.apache.axis.encoding.Deserializer getDeserializer(
           java.lang.String mechType, 
           java.lang.Class _javaType,  
           javax.xml.namespace.QName _xmlType) {
        return 
          new  org.apache.axis.encoding.ser.BeanDeserializer(
            _javaType, _xmlType, typeDesc);
    }

}
