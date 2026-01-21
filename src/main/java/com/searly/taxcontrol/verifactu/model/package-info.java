@XmlSchema(
    namespace = "https://www2.agenciatributaria.gob.es/static_files/common/internet/dep/aplicaciones/es/aeat/tike/cont/ws/SuministroLR.xsd",
    elementFormDefault = XmlNsForm.QUALIFIED,
    xmlns = {
        @XmlNs(prefix = "SOAP-ENV", namespaceURI = "http://schemas.xmlsoap.org/soap/envelope/"),
        @XmlNs(prefix = "xsd", namespaceURI = "http://www.w3.org/2001/XMLSchema"),
        @XmlNs(prefix = "xsi", namespaceURI = "http://www.w3.org/2001/XMLSchema-instance")
    }
)
package com.searly.taxcontrol.verifactu.model;

import javax.xml.bind.annotation.XmlNs;
import javax.xml.bind.annotation.XmlNsForm;
import javax.xml.bind.annotation.XmlSchema; 